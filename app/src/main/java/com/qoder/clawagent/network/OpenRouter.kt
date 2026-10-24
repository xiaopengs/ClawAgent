package com.qoder.clawagent.network

import android.content.Context
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage as SdkChatMessage
import com.aallam.openai.api.chat.ImagePart
import com.aallam.openai.api.chat.TextPart
import com.aallam.openai.api.chat.userMessage
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
import com.qoder.clawagent.model.AttachmentKind
import com.qoder.clawagent.model.ChatMessage
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
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
    /** Data URLs forwarded as image parts on the next user turn. */
    val images: List<String> = emptyList(),
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
        PromptRole.USER -> if (images.isEmpty()) {
            SdkChatMessage.User(content)
        } else {
            val textContent = content
            userMessage {
                contentParts = buildList {
                    if (textContent.isNotBlank()) add(TextPart(textContent))
                    images.forEach { add(ImagePart(it)) }
                }
            }
        }
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

    internal fun buildMessages(agent: Agent, history: List<ChatMessage>): List<PromptMessage> =
        buildPromptMessages(agent, history)

    private companion object {
        const val MAX_HISTORY_MESSAGES = 12
        const val MAX_MESSAGE_CHARS = 6000
    }
}

/**
 * Shared prompt construction for direct, ADK and local OpenAI-compatible
 * runtimes. Files are flattened into labelled text; images remain data URLs so
 * both transports can attach them as multimodal parts.
 */
internal fun buildPromptMessages(agent: Agent, history: List<ChatMessage>): List<PromptMessage> {
    val turns = history
        .takeLast(20)
        .mapNotNull { message ->
            if (message.isError) return@mapNotNull null
            val fileContext = message.attachments
                .filter { it.kind == AttachmentKind.FILE }
                .joinToString("\n\n") { attachment ->
                    val body = attachment.text
                        ?.take(MAX_ATTACHMENT_CHARS)
                        ?.let { "\n$it" }
                        .orEmpty()
                    "[文件：${attachment.name} (${attachment.mimeType})]$body"
                }
            val content = listOf(message.text.take(MAX_MESSAGE_CHARS), fileContext)
                .filter { it.isNotBlank() }
                .joinToString("\n\n")
            val images = message.attachments
                .filter { it.kind == AttachmentKind.IMAGE && !it.dataUrl.isNullOrBlank() }
                .mapNotNull { it.dataUrl }
                .take(MAX_IMAGES)
            if (content.isBlank() && images.isEmpty()) {
                null
            } else {
                PromptMessage(
                    role = if (message.fromUser) PromptRole.USER else PromptRole.ASSISTANT,
                    content = content,
                    images = images,
                )
            }
        }
        .takeLast(MAX_HISTORY_MESSAGES)
    return buildList {
        add(PromptMessage(PromptRole.SYSTEM, agentInstruction(agent)))
        addAll(turns)
    }
}

private const val MAX_HISTORY_MESSAGES = 12
private const val MAX_MESSAGE_CHARS = 6000
private const val MAX_ATTACHMENT_CHARS = 8000
private const val MAX_IMAGES = 4

data class LocalProbeResult(
    val models: List<String> = emptyList(),
    val error: String? = null,
)

/**
 * Probes a local OpenAI-compatible server (Ollama, LM Studio, vLLM, ...) before
 * an Agent is created. It only performs a GET /models and never stores the key.
 */
object LocalEndpointProbe {
    suspend fun probe(endpoint: String, apiKey: String = ""): LocalProbeResult = withContext(Dispatchers.IO) {
        val base = endpoint.trim().trimEnd('/')
        if (!base.startsWith("http://") && !base.startsWith("https://")) {
            return@withContext LocalProbeResult(error = "地址需以 http:// 或 https:// 开头")
        }
        val connection = try {
            (URL("$base/models").openConnection() as HttpURLConnection).apply {
                connectTimeout = 8_000
                readTimeout = 8_000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                if (apiKey.isNotBlank()) {
                    setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
                }
            }
        } catch (error: Exception) {
            return@withContext LocalProbeResult(error = "地址格式无效：${error.message.orEmpty().take(80)}")
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                val reason = when (code) {
                    401, 403 -> "鉴权失败，请检查 Local API Key"
                    404 -> "未找到 /models，请确认 Base URL 以 /v1 结尾"
                    else -> "服务返回 HTTP $code"
                }
                return@withContext LocalProbeResult(error = reason)
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }.take(200_000)
            val ids = Regex("\"id\"\\s*:\\s*\"([^\"]+)\"")
                .findAll(body)
                .map { it.groupValues[1] }
                .distinct()
                .take(60)
                .toList()
            if (ids.isEmpty()) {
                LocalProbeResult(error = "已连通，但没有发现模型；请先在本地服务中加载模型")
            } else {
                LocalProbeResult(models = ids)
            }
        } catch (error: Exception) {
            LocalProbeResult(error = "连接失败：${error.message.orEmpty().take(100)}")
        } finally {
            connection.disconnect()
        }
    }
}

/** Replies through a model server on the user's own machine or LAN. */
class LocalReplySource : ReplySource {
    private val client = OpenRouterClient()

    override fun stream(agent: Agent, history: List<ChatMessage>): Flow<String> {
        val endpoint = agent.localEndpoint.orEmpty().trim()
        val model = agent.localModel.orEmpty().trim()
        if (endpoint.isBlank() || model.isBlank()) {
            throw ReplyUnavailableException("本地 Agent 缺少 Endpoint 或模型，请在 Agent 设置中补全。")
        }
        if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
            throw ReplyUnavailableException("本地 Endpoint 必须以 http:// 或 https:// 开头。")
        }
        val config = OpenRouterConfig(
            apiKey = agent.localApiKey.orEmpty().ifBlank { "local" },
            model = model,
            baseUrl = endpoint,
        )
        return client.stream(buildPromptMessages(agent, history), config)
    }
}
