package com.qoder.clawagent.network

import android.content.Context
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage as SdkChatMessage
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.RetryStrategy
import com.qoder.clawagent.BuildConfig
import com.qoder.clawagent.chat.ReplyEngine
import com.qoder.clawagent.chat.ReplySource
import com.qoder.clawagent.chat.ReplyUnavailableException
import com.qoder.clawagent.chat.agentInstruction
import com.qoder.clawagent.model.Agent
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.time.Duration.Companion.seconds

data class OpenRouterConfig(
    val apiKey: String,
    val model: String,
    val baseUrl: String,
    val engine: ReplyEngine = ReplyEngine.OPENROUTER,
)

enum class PromptRole { SYSTEM, USER, ASSISTANT }

data class PromptMessage(
    val role: PromptRole,
    val content: String,
)

interface OpenRouterSettingsStore {
    fun load(): OpenRouterConfig
    fun save(config: OpenRouterConfig)
}

/**
 * Default store used by previews/tests. It never touches Android shared preferences.
 */
class InMemoryOpenRouterSettingsStore(
    initial: OpenRouterConfig = OpenRouterConfig(
        apiKey = BuildConfig.OPENROUTER_API_KEY,
        model = BuildConfig.OPENROUTER_MODEL,
        baseUrl = BuildConfig.OPENROUTER_BASE_URL,
    ),
) : OpenRouterSettingsStore {

    private var config: OpenRouterConfig = initial

    override fun load(): OpenRouterConfig = config

    override fun save(config: OpenRouterConfig) {
        this.config = config
    }
}

class PrefsOpenRouterSettingsStore(context: Context) : OpenRouterSettingsStore {

    private val prefs = context.applicationContext
        .getSharedPreferences("openrouter_settings", Context.MODE_PRIVATE)

    override fun load(): OpenRouterConfig = OpenRouterConfig(
        apiKey = prefs.stringOrDefault(KEY_API_KEY, BuildConfig.OPENROUTER_API_KEY),
        model = prefs.stringOrDefault(KEY_MODEL, BuildConfig.OPENROUTER_MODEL),
        baseUrl = prefs.stringOrDefault(KEY_BASE_URL, BuildConfig.OPENROUTER_BASE_URL),
        engine = prefs.stringOrDefault(KEY_ENGINE, ReplyEngine.OPENROUTER.name)
            .let { stored -> ReplyEngine.entries.firstOrNull { it.name == stored } ?: ReplyEngine.OPENROUTER },
    )

    override fun save(config: OpenRouterConfig) {
        prefs.edit()
            .putString(KEY_API_KEY, config.apiKey)
            .putString(KEY_MODEL, config.model)
            .putString(KEY_BASE_URL, config.baseUrl)
            .putString(KEY_ENGINE, config.engine.name)
            .apply()
    }

    private fun android.content.SharedPreferences.stringOrDefault(key: String, default: String): String =
        if (contains(key)) getString(key, default).orEmpty() else default

    private companion object {
        const val KEY_API_KEY = "api_key"
        const val KEY_MODEL = "model"
        const val KEY_BASE_URL = "base_url"
        const val KEY_ENGINE = "engine"
    }
}

/**
 * Thin adapter around the OpenAI-compatible Kotlin SDK.
 * OpenRouter exposes the same /chat/completions contract, so only host + token change.
 */
class OpenRouterClient {

    fun stream(messages: List<PromptMessage>, config: OpenRouterConfig): Flow<String> = flow {
        val client = createClient(config)
        try {
            val request = ChatCompletionRequest(
                model = ModelId(config.model),
                messages = messages.map { it.toSdkMessage() },
                temperature = 0.7,
                maxTokens = MAX_TOKENS,
            )
            client.chatCompletions(request).collect { chunk ->
                val text = chunk.choices.firstOrNull()?.delta?.content
                if (!text.isNullOrBlank()) {
                    emit(text)
                }
            }
        } finally {
            runCatching { client.close() }
        }
    }.flowOn(Dispatchers.IO)

    private fun createClient(config: OpenRouterConfig): OpenAI {
        val baseUrl = config.baseUrl.trim().let { if (it.endsWith("/")) it else "$it/" }
        return OpenAI(
            OpenAIConfig(
                token = config.apiKey,
                host = OpenAIHost(baseUrl = baseUrl),
                timeout = Timeout(socket = 90.seconds),
                logging = LoggingConfig(logLevel = LogLevel.None),
                retry = RetryStrategy(maxRetries = 1),
                engine = OkHttp.create(),
            ),
        )
    }

    private fun PromptMessage.toSdkMessage(): SdkChatMessage = when (role) {
        PromptRole.SYSTEM -> SdkChatMessage.System(content)
        PromptRole.USER -> SdkChatMessage.User(content)
        PromptRole.ASSISTANT -> SdkChatMessage.Assistant(content)
    }

    private companion object {
        const val MAX_TOKENS = 1024
    }
}

class OpenRouterRepository(
    private val settingsStore: OpenRouterSettingsStore,
) : ReplySource {

    private val client = OpenRouterClient()

    fun config(): OpenRouterConfig = settingsStore.load()

    fun saveConfig(apiKey: String, model: String, engine: ReplyEngine) {
        val current = settingsStore.load()
        settingsStore.save(
            current.copy(
                apiKey = apiKey.trim(),
                model = model.trim().ifBlank { current.model },
                engine = engine,
            ),
        )
    }

    override fun stream(agent: Agent, history: List<com.qoder.clawagent.model.ChatMessage>): Flow<String> {
        val config = settingsStore.load()
        if (config.apiKey.isBlank()) {
            throw ReplyUnavailableException(
                "尚未配置 OpenRouter API Key。请点击右上角 ··· →「模型设置」填入 Key。",
            )
        }
        return client.stream(buildMessages(agent, history), config)
    }

    internal fun buildMessages(agent: Agent, history: List<com.qoder.clawagent.model.ChatMessage>): List<PromptMessage> {
        val systemPrompt = agentInstruction(agent)
        val turns = history
            .filter { !it.isError && it.text.isNotBlank() }
            .takeLast(MAX_HISTORY_MESSAGES)
            .map { message ->
                PromptMessage(
                    role = if (message.fromUser) PromptRole.USER else PromptRole.ASSISTANT,
                    content = message.text.take(MAX_MESSAGE_CHARS),
                )
            }
            .toList()
        return buildList {
            add(PromptMessage(PromptRole.SYSTEM, systemPrompt))
            addAll(turns)
        }
    }

    private companion object {
        const val MAX_HISTORY_MESSAGES = 12
        const val MAX_MESSAGE_CHARS = 6000
    }
}
