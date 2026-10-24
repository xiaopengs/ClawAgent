package com.qoder.clawagent

import com.qoder.clawagent.chat.ChatConversation
import com.qoder.clawagent.chat.ReplyPhase
import com.qoder.clawagent.chat.ReplySource
import com.qoder.clawagent.chat.ReplyUnavailableException
import com.qoder.clawagent.chat.SendResult
import com.qoder.clawagent.model.Agent
import com.qoder.clawagent.model.AvatarStyle
import com.qoder.clawagent.model.ChatMessage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ChatConversationTest {

    private val agent = Agent(
        id = "test",
        name = "测试助手",
        avatar = AvatarStyle.SPARK,
        subtitle = "",
        timeLabel = "",
        description = "负责回答测试问题。",
    )

    private fun conversation(
        replies: ReplySource,
        scope: CoroutineScope,
        initialMessages: List<ChatMessage> = emptyList(),
        onSettled: (ChatMessage) -> Unit = {},
    ): ChatConversation {
        var id = 0L
        return ChatConversation(
            agent = agent,
            replies = replies,
            scope = scope,
            initialMessages = initialMessages,
            nextId = { ++id },
            now = { 1_000L },
            formatTime = { "10:00" },
            onSettled = onSettled,
        )
    }

    @Test
    fun `send streams a reply into one assistant message`() = runTest {
        val conversation = conversation(
            replies = { _, _ ->
                flow {
                    emit("你")
                    emit("好")
                }
            },
            scope = this,
        )

        assertEquals(SendResult.SENT, conversation.send(" 在吗 "))
        advanceUntilIdle()

        val state = conversation.state.value
        assertEquals(ReplyPhase.IDLE, state.phase)
        assertEquals(listOf(true, false), state.messages.map { it.fromUser })
        assertEquals("在吗", state.messages[0].text)
        assertEquals("你好", state.messages[1].text)
        assertEquals("已处理 1秒", state.messages[1].processed)
    }

    @Test
    fun `reply source receives the new user turn as history`() = runTest {
        var seen: List<ChatMessage> = emptyList()
        val conversation = conversation(
            replies = { _, history ->
                seen = history
                flow { emit("好") }
            },
            scope = this,
        )

        conversation.send("在吗")
        advanceUntilIdle()

        assertEquals(1, seen.size)
        assertEquals("在吗", seen.single().text)
        assertTrue(seen.single().fromUser)
    }

    @Test
    fun `conversation is busy while chunks are still arriving`() = runTest {
        val release = CompletableDeferred<Unit>()
        val conversation = conversation(
            replies = { _, _ ->
                flow {
                    emit("你")
                    release.await()
                    emit("好")
                }
            },
            scope = this,
        )

        assertEquals(SendResult.SENT, conversation.send("在吗"))
        runCurrent()
        assertEquals(ReplyPhase.STREAMING, conversation.state.value.phase)
        assertEquals("你", conversation.state.value.messages.last().text)
        assertEquals(SendResult.BUSY, conversation.send("再来一条"))

        release.complete(Unit)
        advanceUntilIdle()
        assertEquals(ReplyPhase.IDLE, conversation.state.value.phase)
        assertEquals("你好", conversation.state.value.messages.last().text)
        assertEquals(2, conversation.state.value.messages.size)
    }

    @Test
    fun `retry removes the trailing error and asks again`() = runTest {
        var attempts = 0
        val conversation = conversation(
            replies = { _, _ ->
                attempts += 1
                if (attempts == 1) {
                    flow { throw IOException("HTTP 429 Too Many Requests") }
                } else {
                    flow { emit("恢复") }
                }
            },
            scope = this,
        )

        conversation.send("在吗")
        advanceUntilIdle()
        val failed = conversation.state.value.messages.last()
        assertTrue(failed.isError)
        assertTrue(failed.text.contains("频繁"))

        conversation.retry()
        advanceUntilIdle()

        val messages = conversation.state.value.messages
        assertEquals(2, messages.size)
        assertEquals("在吗", messages[0].text)
        assertEquals("恢复", messages[1].text)
        assertFalse(messages[1].isError)
    }

    @Test
    fun `clear cancels an in-flight reply and empties the conversation`() = runTest {
        val release = CompletableDeferred<Unit>()
        val conversation = conversation(
            replies = { _, _ ->
                flow {
                    release.await()
                    emit("迟到的回复")
                }
            },
            scope = this,
        )

        conversation.send("在吗")
        runCurrent()
        conversation.clear()
        release.complete(Unit)
        advanceUntilIdle()

        assertTrue(conversation.state.value.messages.isEmpty())
        assertEquals(ReplyPhase.IDLE, conversation.state.value.phase)
    }

    @Test
    fun `missing configuration is reported as an actionable error`() = runTest {
        val conversation = conversation(
            replies = { _, _ -> throw ReplyUnavailableException("尚未配置 OpenRouter API Key。请点击右上角 ··· →「模型设置」填入 Key。") },
            scope = this,
        )

        conversation.send("在吗")
        advanceUntilIdle()

        val error = conversation.state.value.messages.last()
        assertTrue(error.isError)
        assertTrue(error.text.contains("模型设置"))
    }

    @Test
    fun `blank input is not added to the conversation`() = runTest {
        val conversation = conversation(
            replies = { _, _ -> flow { emit("不应调用") } },
            scope = this,
        )

        assertEquals(SendResult.EMPTY, conversation.send("   "))
        assertTrue(conversation.state.value.messages.isEmpty())
    }
}
