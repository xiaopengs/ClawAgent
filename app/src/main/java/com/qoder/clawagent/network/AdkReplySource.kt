package com.qoder.clawagent.network

import com.google.adk.kt.agents.Instruction
import com.google.adk.kt.agents.LlmAgent
import com.google.adk.kt.agents.RunConfig
import com.google.adk.kt.agents.StreamingMode
import com.google.adk.kt.events.Event
import com.google.adk.kt.models.openai.ChatCompletions
import com.google.adk.kt.runners.InMemoryRunner
import com.google.adk.kt.sessions.SessionKey
import com.google.adk.kt.tools.FunctionTool
import com.google.adk.kt.tools.ToolContext
import com.google.adk.kt.types.Blob
import com.google.adk.kt.types.Content
import com.google.adk.kt.types.FunctionDeclaration
import com.google.adk.kt.types.GenerateContentConfig
import com.google.adk.kt.types.HttpOptions
import com.google.adk.kt.types.Part
import com.google.adk.kt.types.Role
import com.google.adk.kt.types.Schema
import com.google.adk.kt.types.Type
import com.qoder.clawagent.chat.ReplySource
import com.qoder.clawagent.chat.ReplyUnavailableException
import com.qoder.clawagent.chat.agentInstruction
import com.qoder.clawagent.model.Agent
import com.qoder.clawagent.model.AttachmentKind
import com.qoder.clawagent.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

/**
 * ADK-backed reply source.
 *
 * Each turn creates a fresh in-memory ADK session and replays the visible
 * conversation into it. That keeps [ChatConversation] as the single source of
 * truth (clear/retry behave identically for both engines) while still giving
 * the agent ADK's runner, instruction and tool pipeline.
 *
 * The model is still OpenRouter through ADK's OpenAI-compatible Chat
 * Completions backend, so the app keeps one credential and one model setting.
 */
class AdkReplySource(
    private val settingsStore: OpenRouterSettingsStore,
) : ReplySource {

    override fun stream(agent: Agent, history: List<ChatMessage>): Flow<String> {
        val config = settingsStore.load()
        if (config.apiKey.isBlank()) {
            throw ReplyUnavailableException(
                "尚未配置 OpenRouter API Key。请点击右上角 ··· →「模型设置」填入 Key。",
            )
        }
        val turns = history
            .filter { !it.isError && it.hasPayload() }
            .takeLast(MAX_HISTORY_MESSAGES)
            .map { it.copy(text = it.text.take(MAX_MESSAGE_CHARS)) }
        val current = turns.lastOrNull()
        if (current == null || !current.fromUser) {
            throw ReplyUnavailableException("没有可发送的用户消息。")
        }

        return flow {
            val model = ChatCompletions(
                name = config.model,
                apiKey = config.apiKey,
                httpOptions = HttpOptions(
                    baseUrl = config.baseUrl.trimEnd('/'),
                    timeout = 90.seconds,
                ),
            )
            val adkAgent = LlmAgent(
                name = ADK_AGENT_NAME,
                model = model,
                description = agent.description.take(400),
                instruction = Instruction(agentInstruction(agent)),
                tools = listOf(LocalTimeTool()),
                generateContentConfig = GenerateContentConfig(
                    temperature = 0.7f,
                    maxOutputTokens = MAX_TOKENS,
                ),
            )
            val runner = InMemoryRunner(agent = adkAgent, appName = APP_NAME)
            try {
                val session = runner.sessionService.createSession(
                    SessionKey(appName = APP_NAME, userId = USER_ID, id = null),
                )
                val sessionId = session.key.id
                    ?: error("ADK 没有返回 session id")
                turns.dropLast(1).forEach { turn ->
                    runner.sessionService.appendEvent(
                        session,
                        Event(
                            author = if (turn.fromUser) Role.USER else ADK_AGENT_NAME,
                            content = turn.toAdkContent(
                                role = if (turn.fromUser) Role.USER else Role.MODEL,
                            ),
                        ),
                    )
                }

                var emittedPartial = false
                var finalText: String? = null
                runner.runAsync(
                    userId = USER_ID,
                    sessionId = sessionId,
                    newMessage = current.toAdkContent(role = Role.USER),
                    runConfig = RunConfig(streamingMode = StreamingMode.SSE),
                ).collect { event ->
                    event.errorMessage?.let { message ->
                        throw IllegalStateException("ADK Agent 运行失败：$message")
                    }
                    if (event.author != ADK_AGENT_NAME) return@collect
                    val text = event.content?.text().orEmpty()
                    if (text.isEmpty()) return@collect
                    if (event.partial) {
                        emittedPartial = true
                        emit(text)
                    } else {
                        finalText = text
                    }
                }
                // Some compatible endpoints do not stream deltas. ADK still
                // delivers the final aggregate, so use it when nothing streamed.
                if (!emittedPartial && !finalText.isNullOrBlank()) {
                    emit(finalText)
                }
            } finally {
                runner.close()
            }
        }.flowOn(Dispatchers.IO)
    }

    private companion object {
        const val APP_NAME = "claw_agent"
        const val ADK_AGENT_NAME = "claw_agent"
        const val USER_ID = "local_user"
        const val MAX_HISTORY_MESSAGES = 12
        const val MAX_MESSAGE_CHARS = 6000
        const val MAX_TOKENS = 1024
    }
}

private fun ChatMessage.hasPayload(): Boolean =
    text.isNotBlank() || attachments.isNotEmpty()

private fun ChatMessage.toAdkContent(role: String): Content {
    val fileContext = attachments
        .filter { it.kind == AttachmentKind.FILE }
        .joinToString("\n\n") { attachment ->
            val body = attachment.text?.take(8000)?.let { "\n$it" }.orEmpty()
            "[文件：${attachment.name} (${attachment.mimeType})]$body"
        }
    val text = listOf(text, fileContext).filter { it.isNotBlank() }.joinToString("\n\n")
    val parts = buildList {
        if (text.isNotBlank()) add(Part(text = text))
        attachments
            .filter { it.kind == AttachmentKind.IMAGE }
            .take(4)
            .forEach { image ->
                val payload = image.dataUrl?.substringAfter("base64,", missingDelimiterValue = "")
                if (!payload.isNullOrBlank()) {
                    runCatching { Base64.getDecoder().decode(payload) }
                        .getOrNull()
                        ?.let { bytes ->
                            add(
                                Part(
                                    inlineData = Blob(
                                        mimeType = image.mimeType.ifBlank { "image/jpeg" },
                                        displayName = image.name,
                                        data = bytes,
                                    ),
                                ),
                            )
                        }
                }
            }
    }
    return Content(role = role, parts = parts)
}

/**
 * A deliberately small tool that proves the ADK tool loop is wired. It needs
 * no network and no extra permission, so it also works in a debug APK.
 */
private class LocalTimeTool : FunctionTool(    name = "get_local_time",
    description = "返回设备当前的本地日期和时间。用户询问时间时调用。",
) {
    override fun declaration(): FunctionDeclaration = FunctionDeclaration(
        name = name,
        description = description,
        parameters = Schema(type = Type.OBJECT),
    )

    override suspend fun execute(context: ToolContext, args: Map<String, Any?>): Any =
        mapOf(
            "local_time" to LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())),
        )
}
