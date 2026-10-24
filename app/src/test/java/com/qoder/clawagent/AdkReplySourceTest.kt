package com.qoder.clawagent

import com.qoder.clawagent.chat.ReplyEngine
import com.qoder.clawagent.chat.ReplyUnavailableException
import com.qoder.clawagent.data.SampleData
import com.qoder.clawagent.model.ChatMessage
import com.qoder.clawagent.network.AdkReplySource
import com.qoder.clawagent.network.InMemoryOpenRouterSettingsStore
import com.qoder.clawagent.network.OpenRouterConfig
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Properties

class AdkReplySourceTest {

    @Test(expected = ReplyUnavailableException::class)
    fun `missing key fails before starting ADK`() {
        val source = AdkReplySource(
            InMemoryOpenRouterSettingsStore(
                OpenRouterConfig(apiKey = "", model = "openrouter/free", baseUrl = BASE_URL),
            ),
        )
        runBlocking {
            source.stream(SampleData.seedAgents().first(), userTurn("你好")).toList()
        }
    }

    @Test
    fun `blank history is rejected`() = runBlocking {
        val source = AdkReplySource(InMemoryOpenRouterSettingsStore(config()))
        val error = runCatching { source.stream(SampleData.seedAgents().first(), emptyList()).toList() }
            .exceptionOrNull()
        assertTrue(error is ReplyUnavailableException)
    }

    private fun config(): OpenRouterConfig {
        val local = Properties().apply {
            runCatching {
                rootProjectFile("local.properties").reader().use(::load)
            }
        }
        return OpenRouterConfig(
            apiKey = local.getProperty("OPENROUTER_API_KEY").orEmpty(),
            model = local.getProperty("OPENROUTER_MODEL") ?: "openrouter/free",
            baseUrl = local.getProperty("OPENROUTER_BASE_URL") ?: BASE_URL,
            engine = ReplyEngine.ADK,
        )
    }

    private fun rootProjectFile(name: String) = java.io.File("../$name")

    private fun userTurn(text: String) = listOf(
        ChatMessage(id = 1, fromUser = true, text = text, time = "10:00"),
    )
    private companion object {
        const val BASE_URL = "https://openrouter.ai/api/v1/"
    }
}
