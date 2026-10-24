package com.qoder.clawagent

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.qoder.clawagent.model.AgentKind
import com.qoder.clawagent.model.AppScreen
import com.qoder.clawagent.model.AvatarStyle
import com.qoder.clawagent.model.ChatAttachment
import com.qoder.clawagent.model.ChatMessage
import com.qoder.clawagent.model.DriveFile
import com.qoder.clawagent.model.Plan
import com.qoder.clawagent.model.Project
import com.qoder.clawagent.model.ScheduleTask
import com.qoder.clawagent.model.ThirdPartyAgent
import com.qoder.clawagent.model.TopicTrackingItem
import com.qoder.clawagent.network.AdkReplySource
import com.qoder.clawagent.network.InMemoryOpenRouterSettingsStore
import com.qoder.clawagent.network.LocalEndpointProbe
import com.qoder.clawagent.network.LocalProbeResult
import com.qoder.clawagent.network.LocalReplySource
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
    private val local = LocalReplySource()

    /** One seam, three runtimes; the Agent kind decides where a turn goes. */
    private val replies = ReplySource { agent, history ->
        if (agent.kind == AgentKind.LOCAL) {
            local.stream(agent, history)
        } else {
            when (openRouter.config().engine) {
                ReplyEngine.OPENROUTER -> openRouter.stream(agent, history)
                ReplyEngine.ADK -> adk.stream(agent, history)
            }
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

    val scheduleTasks = mutableStateListOf<ScheduleTask>().apply {
        addAll(SampleData.scheduleTasks)
    }

    val driveFiles = mutableStateListOf<DriveFile>().apply {
        addAll(SampleData.driveFiles)
    }

    val topics = mutableStateListOf<TopicTrackingItem>().apply {
        addAll(SampleData.topicItems)
    }

    var plan by mutableStateOf(Plan.FREE)
        private set

    var notificationsEnabled by mutableStateOf(true)
        private set

    private var messageId = 100L
    private var agentCounter = 0
    private var projectCounter = 0
    private var taskCounter = 0
    private var driveCounter = 0
    private var topicCounter = 0
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

    fun seededAgentIds(): Set<String> = SampleData.seedAgents().map { it.id }.toSet()

    fun canDeleteAgent(agent: Agent): Boolean =
        agent.id !in seededAgentIds()

    fun chatState(agentId: String): StateFlow<ChatState> =
        conversations[agentId]?.state ?: emptyChat.asStateFlow()

    fun sendMessage(
        agentId: String,
        raw: String,
        attachments: List<ChatAttachment> = emptyList(),
    ): SendResult {
        val conversation = conversations[agentId] ?: return SendResult.EMPTY
        val result = conversation.send(raw, attachments)
        if (result == SendResult.SENT) {
            updateAgentPreview(agentId, previewOf(raw.trim(), attachments))
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
        val agent = newAgent(
            name = name,
            workdir = workdir,
            description = description,
            extensions = extensions,
            kind = AgentKind.COZE,
            avatar = AvatarStyle.SPARK,
            subtitle = "暂无消息，快去聊聊吧",
            extraGreeting = if (extensions.isEmpty()) {
                ""
            } else {
                "已为你启用 ${extensions.joinToString("、")}。"
            },
        )
        return agent
    }

    fun createThirdPartyAgent(
        preset: ThirdPartyAgent,
        workdir: String,
        name: String = preset.name,
    ): Agent = newAgent(
        name = name,
        workdir = workdir,
        description = preset.description,
        extensions = emptyList(),
        kind = AgentKind.THIRD_PARTY,
        avatar = preset.avatar,
        accent = preset.iconBg,
        subtitle = "云电脑 Agent 已就绪",
        command = preset.command,
        extraGreeting = "已接入云电脑 Agent 预设「${preset.command}」。当前对话可生成执行计划与命令；真实 CLI 进程需要连接云电脑 worker。",
    )

    fun createLocalAgent(
        name: String,
        endpoint: String,
        model: String,
        apiKey: String,
        workdir: String,
    ): Agent = newAgent(
        name = name,
        workdir = workdir,
        description = "连接本地 OpenAI 兼容模型服务。",
        extensions = emptyList(),
        kind = AgentKind.LOCAL,
        avatar = AvatarStyle.GLASSES,
        subtitle = "本地端点已配置",
        localEndpoint = endpoint.trim(),
        localModel = model.trim(),
        localApiKey = apiKey.trim(),
        extraGreeting = "已连接本地模型「${model.trim()}」。消息会直接发送到 ${endpoint.trim()}，不会经过 OpenRouter。",
    )

    private fun newAgent(
        name: String,
        workdir: String,
        description: String,
        extensions: List<String>,
        kind: AgentKind,
        avatar: AvatarStyle,
        subtitle: String,
        accent: Long? = null,
        command: String? = null,
        localEndpoint: String? = null,
        localModel: String? = null,
        localApiKey: String? = null,
        extraGreeting: String = "",
    ): Agent {
        agentCounter += 1
        val id = "custom-$agentCounter-${System.currentTimeMillis()}"
        val color = accent ?: avatarPalette[(agentCounter - 1) % avatarPalette.size]
        val agent = Agent(
            id = id,
            name = name.trim(),
            avatar = avatar,
            avatarColor = color,
            subtitle = subtitle,
            timeLabel = "刚刚",
            description = description.trim().ifBlank { "工作目录：${workdir.ifBlank { name }}" },
            kind = kind,
            workdir = workdir.trim(),
            extensions = extensions,
            localEndpoint = localEndpoint,
            localModel = localModel,
            localApiKey = localApiKey,
            command = command,
        )
        agents.add(0, agent)
        val greeting = buildString {
            append("你好，我是${agent.name}。")
            if (extraGreeting.isNotBlank()) {
                append(extraGreeting)
                append(" ")
            }
            append("告诉我你想完成什么，我会在这里持续跟进。")
        }
        conversations[id] = conversationFor(
            agent,
            listOf(
                ChatMessage(
                    id = ++messageId,
                    fromUser = false,
                    text = greeting,
                    time = clockNow(),
                    processed = "已处理 1秒",
                ),
            ),
        )
        return agent
    }

    fun saveAgent(updated: Agent) {
        val index = agents.indexOfFirst { it.id == updated.id }
        if (index < 0) return
        val existing = agents[index]
        if (updated.name.isBlank()) return
        val sanitized = updated.copy(
            name = updated.name.trim(),
            description = updated.description.trim(),
            kind = existing.kind,
            visibleInList = existing.visibleInList,
            subtitle = existing.subtitle,
            timeLabel = existing.timeLabel,
            localEndpoint = if (existing.kind == AgentKind.LOCAL) updated.localEndpoint else existing.localEndpoint,
            localModel = if (existing.kind == AgentKind.LOCAL) updated.localModel else existing.localModel,
            localApiKey = if (existing.kind == AgentKind.LOCAL) updated.localApiKey else existing.localApiKey,
            command = existing.command,
        )
        agents[index] = sanitized
        conversations[sanitized.id]?.updateAgent(sanitized)
        projectIdForAgent(sanitized.id)?.let { projectId ->
            val projectIndex = projects.indexOfFirst { it.id == projectId }
            if (projectIndex >= 0) {
                projects[projectIndex] = projects[projectIndex].copy(name = sanitized.name)
            }
        }
    }

    fun deleteAgent(agentId: String) {
        val agent = agentById(agentId) ?: return
        if (!canDeleteAgent(agent)) return
        conversations.remove(agentId)?.close()
        agents.removeAll { it.id == agentId }
        backStack.removeAll { it is AppScreen.Chat && it.agentId == agentId }
        backStack.removeAll { it is AppScreen.AgentSettings && it.agentId == agentId }
        if (backStack.isEmpty()) backStack.add(AppScreen.Home)
    }

    fun toggleExtension(id: String) {
        addedExtensions[id] = !(addedExtensions[id] ?: false)
    }

    // ---- Projects -----------------------------------------------------------

    fun projectById(id: String): Project? = projects.firstOrNull { it.id == id }

    fun createProject(name: String, isWebsite: Boolean): Project {
        projectCounter += 1
        val id = "project-${projectCounter}-${System.currentTimeMillis()}"
        val project = Project(
            id = id,
            name = name.trim(),
            subtitle = "项目工作区已创建，点击开始对话",
            timeLabel = "刚刚",
            isWebsite = isWebsite,
        )
        projects.add(0, project)
        ensureProjectAgent(project)
        return project
    }

    fun renameProject(projectId: String, name: String) {
        val index = projects.indexOfFirst { it.id == projectId }
        if (index < 0 || name.isBlank()) return
        projects[index] = projects[index].copy(name = name.trim())
        val agent = agentById(projectAgentId(projectId))
        if (agent != null) {
            agents[agents.indexOfFirst { it.id == agent.id }] = agent.copy(name = name.trim())
        }
    }

    fun deleteProject(projectId: String) {
        val project = projectById(projectId) ?: return
        val agentId = projectAgentId(project.id)
        conversations.remove(agentId)?.close()
        agents.removeAll { it.id == agentId }
        projects.removeAll { it.id == projectId }
        backStack.removeAll { it is AppScreen.ProjectDetail && it.projectId == projectId }
        if (backStack.isEmpty()) backStack.add(AppScreen.Home)
    }

    /**
     * Project chats use a hidden Agent so every conversation still runs through
     * the same [ChatConversation]. The agent is created on first open.
     */
    fun ensureProjectAgent(project: Project): Agent {
        val id = projectAgentId(project.id)
        agentById(id)?.let { return it }
        val agent = Agent(
            id = id,
            name = project.name,
            avatar = if (project.isWebsite) AvatarStyle.TERMINAL else AvatarStyle.SPARK,
            avatarColor = if (project.isWebsite) 0xFFD7E4D4 else 0xFFE7DCC8,
            subtitle = "暂无消息，快去聊聊吧",
            timeLabel = project.timeLabel,
            description = "协作项目「${project.name}」的专属工作区。",
            workdir = project.name,
            visibleInList = false,
        )
        agents.add(agent)
        conversations[id] = conversationFor(
            agent,
            listOf(
                ChatMessage(
                    id = ++messageId,
                    fromUser = false,
                    text = "这里是「${project.name}」项目工作区。你可以让我整理需求、拆解任务，或继续推进这个项目。",
                    time = clockNow(),
                    processed = "已处理 1秒",
                ),
            ),
        )
        return agent
    }

    private fun projectAgentId(projectId: String): String = "$PROJECT_AGENT_PREFIX$projectId"

    private fun projectIdForAgent(agentId: String): String? =
        if (agentId.startsWith(PROJECT_AGENT_PREFIX)) agentId.removePrefix(PROJECT_AGENT_PREFIX) else null

    // ---- Schedule -----------------------------------------------------------

    fun addScheduleTask(title: String, timeLabel: String, repeatLabel: String, agentName: String) {
        if (title.isBlank()) return
        taskCounter += 1
        scheduleTasks.add(
            0,
            ScheduleTask(
                id = "task-${taskCounter}-${System.currentTimeMillis()}",
                title = title.trim(),
                timeLabel = timeLabel.trim().ifBlank { "每天 09:00" },
                repeatLabel = repeatLabel.trim().ifBlank { "每天" },
                agentName = agentName.ifBlank { agents.firstOrNull()?.name.orEmpty() },
            ),
        )
    }

    fun toggleScheduleTask(id: String) {
        val index = scheduleTasks.indexOfFirst { it.id == id }
        if (index >= 0) {
            scheduleTasks[index] = scheduleTasks[index].copy(done = !scheduleTasks[index].done)
        }
    }

    fun deleteScheduleTask(id: String) {
        scheduleTasks.removeAll { it.id == id }
    }

    // ---- Drive --------------------------------------------------------------

    fun addDriveFile(file: DriveFile) {
        driveFiles.add(0, file)
    }

    fun deleteDriveFile(id: String) {
        driveFiles.removeAll { it.id == id }
    }

    fun newDriveFile(
        name: String,
        mimeType: String,
        sizeLabel: String,
        uri: String?,
        previewText: String?,
    ): DriveFile {
        driveCounter += 1
        return DriveFile(
            id = "drive-local-${driveCounter}-${System.currentTimeMillis()}",
            name = name,
            mimeType = mimeType,
            sizeLabel = sizeLabel,
            addedLabel = "刚刚",
            uri = uri,
            previewText = previewText,
        )
    }

    // ---- Topics -------------------------------------------------------------

    fun toggleTopicFollow(id: String) {
        val index = topics.indexOfFirst { it.id == id }
        if (index >= 0) {
            topics[index] = topics[index].copy(followed = !topics[index].followed)
        }
    }

    fun addTopic(title: String, summary: String): TopicTrackingItem {
        topicCounter += 1
        val item = TopicTrackingItem(
            id = "topic-local-${topicCounter}-${System.currentTimeMillis()}",
            title = title.trim(),
            summary = summary.trim().ifBlank { "手动添加的追踪话题。" },
            source = "手动添加",
            updatedLabel = "刚刚",
        )
        topics.add(0, item)
        return item
    }

    fun deleteTopic(id: String) {
        topics.removeAll { it.id == id }
    }

    // ---- Account ------------------------------------------------------------

    fun setNotifications(enabled: Boolean) {
        notificationsEnabled = enabled
    }

    fun upgradeToPro() {
        plan = Plan.PRO
    }

    suspend fun probeLocalEndpoint(endpoint: String, apiKey: String): LocalProbeResult =
        LocalEndpointProbe.probe(endpoint, apiKey)

    fun modelLabel(agent: Agent): String =
        if (agent.kind == AgentKind.LOCAL) {
            agent.localModel.orEmpty().ifBlank { "未指定" }
        } else {
            openRouter.config().model
        }

    fun engineLabel(agent: Agent): String = when {
        agent.kind == AgentKind.LOCAL -> "本地 OpenAI 兼容端点"
        agent.kind == AgentKind.THIRD_PARTY && openRouter.config().engine == ReplyEngine.ADK ->
            "ADK Agent · 云电脑预设"
        openRouter.config().engine == ReplyEngine.ADK -> "ADK Agent"
        else -> "OpenRouter 直连"
    }

    // ---- Internal -----------------------------------------------------------

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
                    else -> previewOf(message.text, emptyList())
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
        projectIdForAgent(agentId)?.let { projectId ->
            val projectIndex = projects.indexOfFirst { it.id == projectId }
            if (projectIndex >= 0) {
                projects[projectIndex] = projects[projectIndex].copy(
                    subtitle = preview,
                    timeLabel = "刚刚",
                )
            }
        }
    }

    private fun previewOf(text: String, attachments: List<ChatAttachment>): String {
        val raw = text.replace('\n', ' ').trim().ifBlank {
            val images = attachments.count { it.kind == com.qoder.clawagent.model.AttachmentKind.IMAGE }
            val files = attachments.size - images
            when {
                images > 0 && files > 0 -> "发送了 $images 张图片和 $files 个文件"
                images > 0 -> "发送了 $images 张图片"
                files > 0 -> "发送了 $files 个文件"
                else -> "暂无消息，快去聊聊吧"
            }
        }
        return if (raw.length > 24) raw.take(24) + "…" else raw
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

    private companion object {
        const val PROJECT_AGENT_PREFIX = "project-agent-"
    }
}
