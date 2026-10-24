package com.qoder.clawagent.chat

import com.qoder.clawagent.model.Agent
import com.qoder.clawagent.model.ChatMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The only thing a conversation needs from a model: a stream of reply chunks.
 * The production adapter is OpenRouter; tests provide an in-memory adapter.
 */
fun interface ReplySource {
    fun stream(agent: Agent, history: List<ChatMessage>): Flow<String>
}

/**
 * A reply cannot start because the adapter is not configured. The message is
 * written for the person using the app, not for a log.
 */
class ReplyUnavailableException(message: String) : IllegalStateException(message)

enum class ReplyPhase {
    IDLE,
    WAITING,
    STREAMING,
}

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val phase: ReplyPhase = ReplyPhase.IDLE,
) {
    val isBusy: Boolean get() = phase != ReplyPhase.IDLE
}

enum class SendResult {
    SENT,
    EMPTY,
    BUSY,
}

/**
 * Owns one agent's conversation: turns, streaming, retry and clearing.
 *
 * Callers observe [state] and call [send], [retry] or [clear]; they never see
 * the reply job or the message buffer. Tests cross the same interface.
 */
class ChatConversation(
    private val agent: Agent,
    private val replies: ReplySource,
    private val scope: CoroutineScope,
    initialMessages: List<ChatMessage> = emptyList(),
    private val nextId: () -> Long,
    private val now: () -> Long = System::currentTimeMillis,
    private val formatTime: (Long) -> String = ::defaultTimeLabel,
    private val onSettled: (ChatMessage) -> Unit = {},
) {

    private val _state = MutableStateFlow(ChatState(initialMessages))
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var replyJob: Job? = null
    private var generation = 0

    fun send(raw: String): SendResult {
        val text = raw.trim()
        if (text.isEmpty()) return SendResult.EMPTY
        if (_state.value.isBusy) return SendResult.BUSY

        _state.update { current ->
            current.copy(
                messages = current.messages + ChatMessage(
                    id = nextId(),
                    fromUser = true,
                    text = text,
                    time = formatTime(now()),
                ),
            )
        }
        startReply()
        return SendResult.SENT
    }

    fun retry() {
        if (_state.value.isBusy) return

        val messages = _state.value.messages
        val lastUserIndex = messages.indexOfLast { it.fromUser }
        if (lastUserIndex < 0) return

        var end = messages.size
        while (end - 1 > lastUserIndex && messages[end - 1].isError) {
            end -= 1
        }
        _state.update { it.copy(messages = it.messages.take(end)) }
        startReply()
    }

    fun clear() {
        generation += 1
        replyJob?.cancel()
        replyJob = null
        _state.value = ChatState()
    }

    fun close() {
        generation += 1
        replyJob?.cancel()
        replyJob = null
    }

    private fun startReply() {
        val history = _state.value.messages
        val startedAt = now()
        val replyGeneration = ++generation
        _state.update { it.copy(phase = ReplyPhase.WAITING) }

        replyJob = scope.launch {
            var replyId: Long? = null
            val buffer = StringBuilder()
            try {
                replies.stream(agent, history).collect { chunk ->
                    if (replyGeneration != generation) return@collect
                    if (chunk.isBlank()) return@collect
                    buffer.append(chunk)

                    val id = replyId
                    if (id == null) {
                        val message = ChatMessage(
                            id = nextId(),
                            fromUser = false,
                            text = buffer.toString(),
                            time = formatTime(now()),
                        )
                        replyId = message.id
                        _state.update {
                            it.copy(
                                messages = it.messages + message,
                                phase = ReplyPhase.STREAMING,
                            )
                        }
                    } else {
                        updateMessage(id) { it.copy(text = buffer.toString()) }
                    }
                }

                if (replyGeneration != generation) return@launch

                val id = replyId
                if (id == null || buffer.isBlank()) {
                    appendError("模型没有返回内容。请重试，或在「模型设置」中换一个模型。")
                } else {
                    val elapsed = ((now() - startedAt) / 1000).coerceAtLeast(1).toInt()
                    updateMessage(id) { it.copy(processed = "已处理 ${elapsed}秒") }
                    _state.value.messages.firstOrNull { it.id == id }?.let(onSettled)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                appendError(describeReplyFailure(error))
            } finally {
                if (replyGeneration == generation) {
                    _state.update { it.copy(phase = ReplyPhase.IDLE) }
                }
            }
        }
    }

    private fun appendError(text: String) {
        val message = ChatMessage(
            id = nextId(),
            fromUser = false,
            text = text,
            time = formatTime(now()),
            isError = true,
        )
        _state.update { it.copy(messages = it.messages + message) }
        onSettled(message)
    }

    private fun updateMessage(id: Long, transform: (ChatMessage) -> ChatMessage) {
        _state.update { current ->
            val index = current.messages.indexOfFirst { it.id == id }
            if (index < 0) {
                current
            } else {
                current.copy(
                    messages = current.messages.toMutableList().apply {
                        this[index] = transform(this[index])
                    },
                )
            }
        }
    }
}

private fun defaultTimeLabel(epochMillis: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMillis))
