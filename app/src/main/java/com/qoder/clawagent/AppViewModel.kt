package com.qoder.clawagent

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qoder.clawagent.chat.ChatConversation
import com.qoder.clawagent.chat.ChatState
import com.qoder.clawagent.chat.ReplyEngine
import com.qoder.clawagent.chat.ReplySource
import com.qoder.clawagent.chat.SendResult
import com.qoder.clawagent.data.SampleData
import com.qoder.clawagent.model.Agent
import com.qoder.clawagent.model.AppScreen
import com.qoder.clawagent.model.AvatarStyle
import com.qoder.clawagent.model.ChatMessage
import com.qoder.clawagent.model.Project
import com.qoder.clawagent.network.AdkReplySource
import com.qoder.clawagent.network.InMemoryOpenRouterSettingsStore
import com.qoder.clawagent.network.OpenRouterRepository
import com.qoder.clawagent.network.OpenRouterSettingsStore
import com.qoder.clawagent.network.PrefsOpenRouterSettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppViewModel(
    private val settingsStore: OpenRouterSettingsStore = InMemoryOpenRouterSettingsStore(),
) : ViewModel() {

    private val openRouter = OpenRouterRepository(settingsStore)
    private val adk = AdkReplySource(settingsStore)

    /** One seam, two runtimes; the settings dialog decides which one answers. */
    private val replies = ReplySource { agent, history ->
        when (openRouter.config().engine) {
            ReplyEngine.OPENROUTER -> openRouter.stream(agent, history)
            ReplyEngine.ADK -> adk.stream(agent, history)
        }
    }
    private val conversations = mutableMapOf<String, ChatConversation>()
    private val emptyChat = MutableStateFlow(ChatState())

    val backStack = mutableStateListOf<AppScreen>(AppScreen.Home)

    val agents = mutableStateListOf<Agent>().apply {
        addAll(SampleData.seedAgents())
    }

    val projects = mutableStateListOf<Project>().apply {
        addAll(SampleData.seedProjects())
    }

    val addedExtensions = mutableStateMapOf<String, Boolean>()

    private var messageId = 100L
    private var agentCounter = 0
    private val avatarPalette = listOf(
        0xFFE7DCC8,
        0xFFD9E4F2,
        0xFFEBD9E8,
        0xFFD7E4D4,
        0xFFE3DDEF,
    )

    init {
        agents.forEach { agent ->
            conversations[agent.id] = conversationFor(agent, SampleData.initialMessages(agent))
        }
    }

    fun currentScreen(): AppScreen = backStack.lastOrNull() ?: AppScreen.Home

    fun navigate(screen: AppScreen) {
        if (backStack.lastOrNull() != screen) {
            backStack.add(screen)
        }
    }

    fun goBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun goHome() {
        while (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun switchTab(title: String) {
        val last = backStack.lastOrNull()
        if (last is AppScreen.Tab) {
            backStack[backStack.lastIndex] = AppScreen.Tab(title)
        } else {
            backStack.add(AppScreen.Tab(title))
        }
    }

    fun agentById(id: String): Agent? = agents.firstOrNull { it.id == id }

    fun chatState(agentId: String): StateFlow<ChatState> =
        conversations[agentId]?.state ?: emptyChat.asStateFlow()

    fun sendMessage(agentId: String, raw: String): SendResult {
        val conversation = conversations[agentId] ?: return SendResult.EMPTY
        val result = conversation.send(raw)
        if (result == SendResult.SENT) {
            updateAgentPreview(agentId, previewOf(raw.trim()))
        }
        return result
    }

    fun retryLast(agentId: String) {
        conversations[agentId]?.retry()
    }

    fun openRouterConfig() = openRouter.config()

    fun saveOpenRouterConfig(apiKey: String, model: String, engine: ReplyEngine) {
        openRouter.saveConfig(apiKey = apiKey, model = model, engine = engine)
    }

    fun clearChat(agentId: String) {
        conversations[agentId]?.clear()
        updateAgentPreview(agentId, "暂无消息，快去聊聊吧")
    }

    fun createAgent(
        name: String,
        workdir: String,
        description: String,
        extensions: List<String>,
    ): Agent {
        agentCounter += 1
        val id = "custom-$agentCounter-${System.currentTimeMillis()}"
        val color = avatarPalette[(agentCounter - 1) % avatarPalette.size]
        val extensionLine = if (extensions.isEmpty()) {
            ""
        } else {
            "已为你启用 ${extensions.joinToString("、")}。"
        }
        val agent = Agent(
            id = id,
            name = name.trim(),
            avatar = AvatarStyle.SPARK,
            avatarColor = color,
            subtitle = "暂无消息，快去聊聊吧",
            timeLabel = "刚刚",
            description = description.trim().ifBlank { "工作目录：${workdir.ifBlank { name }}" },
        )
        agents.add(0, agent)
        conversations[id] = conversationFor(
            agent,
            listOf(
                ChatMessage(
                    id = ++messageId,
                    fromUser = false,
                    text = "你好，我是${agent.name}。${extensionLine}告诉我你想完成什么，我会在这里持续跟进。",
                    time = clockNow(),
                    processed = "已处理 1秒",
                ),
            ),
        )
        return agent
    }

    fun toggleExtension(id: String) {
        addedExtensions[id] = !(addedExtensions[id] ?: false)
    }

    private fun conversationFor(agent: Agent, initialMessages: List<ChatMessage>): ChatConversation =
        ChatConversation(
            agent = agent,
            replies = replies,
            scope = viewModelScope,
            initialMessages = initialMessages,
            nextId = { ++messageId },
            formatTime = { clockNow() },
            onSettled = { message ->
                val preview = when {
                    message.isError && message.text.contains("API Key") ->
                        "请先配置 OpenRouter API Key"
                    message.isError -> "请求失败，点此重试"
                    else -> previewOf(message.text)
                }
                updateAgentPreview(agent.id, preview)
            },
        )

    private fun updateAgentPreview(agentId: String, preview: String) {
        val index = agents.indexOfFirst { it.id == agentId }
        if (index >= 0) {
            agents[index] = agents[index].copy(
                subtitle = preview,
                timeLabel = "刚刚",
            )
        }
    }

    private fun previewOf(text: String): String {
        return text.replace('\n', ' ').trim().let {
            if (it.length > 24) it.take(24) + "…" else it
        }
    }

    override fun onCleared() {
        conversations.values.forEach { it.close() }
        conversations.clear()
        super.onCleared()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppViewModel(PrefsOpenRouterSettingsStore(context.applicationContext)) as T
        }
    }

    private fun clockNow(): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}
