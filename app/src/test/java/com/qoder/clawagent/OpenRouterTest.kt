package com.qoder.clawagent

import com.qoder.clawagent.chat.ReplyEngine
import com.qoder.clawagent.model.Agent
import com.qoder.clawagent.model.AvatarStyle
import com.qoder.clawagent.model.ChatMessage
import com.qoder.clawagent.network.InMemoryOpenRouterSettingsStore
import com.qoder.clawagent.network.OpenRouterClient
import com.qoder.clawagent.network.OpenRouterConfig
import com.qoder.clawagent.network.OpenRouterRepository
import com.qoder.clawagent.network.PromptMessage
import com.qoder.clawagent.network.PromptRole
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

class OpenRouterRepositoryTest {

    private val agent = Agent(
        id = "test",
        name = "测试助手",
        avatar = AvatarStyle.SPARK,
        subtitle = "",
        timeLabel = "",
        description = "负责回答测试问题。",
    )

    @Test
    fun `buildMessages puts system prompt first and skips errors`() {
        val repository = OpenRouterRepository(
            InMemoryOpenRouterSettingsStore(
                OpenRouterConfig(apiKey = "test-key", model = "test-model", baseUrl = "https://example.com/v1/"),
            ),
        )
        val history = listOf(
            ChatMessage(id = 1, fromUser = false, text = "你好", time = "10:00"),
            ChatMessage(id = 2, fromUser = true, text = "帮我总结", time = "10:01"),
            ChatMessage(id = 3, fromUser = false, text = "网络错误", time = "10:02", isError = true),
        )

        val messages = repository.buildMessages(agent, history)

        assertEquals(PromptRole.SYSTEM, messages.first().role)
        assertTrue(messages.first().content.contains("测试助手"))
        assertEquals(listOf(PromptRole.ASSISTANT, PromptRole.USER), messages.drop(1).map { it.role })
        assertEquals(3, messages.size)
    }

    @Test
    fun `saveConfig persists the selected engine`() {
        val store = InMemoryOpenRouterSettingsStore(
            OpenRouterConfig(apiKey = "test-key", model = "test-model", baseUrl = "https://example.com/v1/"),
        )
        val repository = OpenRouterRepository(store)

        repository.saveConfig(apiKey = "new-key", model = "new-model", engine = ReplyEngine.ADK)

        val saved = repository.config()
        assertEquals(ReplyEngine.ADK, saved.engine)
        assertEquals("new-key", saved.apiKey)
        assertEquals("new-model", saved.model)
    }
}

class OpenRouterLiveTest {

    @Test
    fun `live OpenRouter streaming smoke test`() = runBlocking {
        assumeTrue(
            "Set OPENROUTER_LIVE_TEST=1 to run the live test",
            System.getenv("OPENROUTER_LIVE_TEST") == "1",
        )
        val config = OpenRouterConfig(
            apiKey = BuildConfig.OPENROUTER_API_KEY,
            model = BuildConfig.OPENROUTER_MODEL,
            baseUrl = BuildConfig.OPENROUTER_BASE_URL,
        )
        assumeTrue("OPENROUTER_API_KEY is blank", config.apiKey.isNotBlank())

        val reply = OpenRouterClient()
            .stream(
                messages = listOf(
                    PromptMessage(PromptRole.SYSTEM, "你是测试助手，只输出一个词。"),
                    PromptMessage(PromptRole.USER, "请回复：成功"),
                ),
                config = config,
            )
            .take(30)
            .toList()
            .joinToString("")

        println("OpenRouter model=${config.model} reply=$reply")
        assertTrue(reply.isNotBlank())
    }
}
