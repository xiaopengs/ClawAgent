package com.qoder.clawagent

import com.qoder.clawagent.chat.ReplyEngine
import com.qoder.clawagent.model.Agent
import com.qoder.clawagent.model.AvatarStyle
import com.qoder.clawagent.model.ChatMessage
import com.qoder.clawagent.network.AdkReplySource
import com.qoder.clawagent.network.InMemoryOpenRouterSettingsStore
import com.qoder.clawagent.network.OpenRouterConfig
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AdkLiveTest {

    @Test(timeout = 180_000)
    fun `live ADK agent streams a reply through OpenRouter`() = runBlocking {
        assumeTrue(
            "Set OPENROUTER_LIVE_TEST=1 to run the live test",
            System.getenv("OPENROUTER_LIVE_TEST") == "1",
        )
        val config = OpenRouterConfig(
            apiKey = BuildConfig.OPENROUTER_API_KEY,
            model = BuildConfig.OPENROUTER_MODEL,
            baseUrl = BuildConfig.OPENROUTER_BASE_URL,
            engine = ReplyEngine.ADK,
        )
        assumeTrue("OPENROUTER_API_KEY is blank", config.apiKey.isNotBlank())

        val agent = Agent(
            id = "live",
            name = "测试助手",
            avatar = AvatarStyle.SPARK,
            subtitle = "",
            timeLabel = "",
            description = "只输出一个词。",
        )
        val history = listOf(
            ChatMessage(id = 1, fromUser = true, text = "请回复：ADK 成功", time = "10:00"),
        )

        val reply = AdkReplySource(InMemoryOpenRouterSettingsStore(config))
            .stream(agent, history)
            .toList()
            .joinToString("")

        println("ADK model=${config.model} reply=$reply")
        assertTrue(reply.isNotBlank())
    }
}
