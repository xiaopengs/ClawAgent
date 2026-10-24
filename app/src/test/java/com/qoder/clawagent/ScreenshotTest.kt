package com.qoder.clawagent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.qoder.clawagent.ui.screens.AgentFormScreen
import com.qoder.clawagent.ui.screens.ChatScreen
import com.qoder.clawagent.ui.screens.CozeTemplatesScreen
import com.qoder.clawagent.ui.screens.ExtensionsOverlay
import com.qoder.clawagent.ui.screens.HomeScreen
import com.qoder.clawagent.ui.screens.NewAgentScreen
import com.qoder.clawagent.ui.theme.Bg
import com.qoder.clawagent.ui.theme.ClawTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w393dp-h851dp-440dpi")
class ScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun snapshot(name: String, content: @Composable () -> Unit) {
        composeRule.setContent {
            ClawTheme {
                Box(Modifier.fillMaxSize().background(Bg)) {
                    content()
                }
            }
        }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    }

    @Test
    fun home() = snapshot("home") {
        HomeScreen(vm = AppViewModel(), onOpenExtensions = {})
    }

    @Test
    fun chat() = snapshot("chat") {
        ChatScreen(vm = AppViewModel(), agentId = "heizhua", onOpenExtensions = {})
    }

    @Test
    fun extensionsSkills() = snapshot("skills") {
        HomeScreen(vm = AppViewModel(), onOpenExtensions = {})
        ExtensionsOverlay(visible = true, initialTab = 1, vm = AppViewModel(), onDismiss = {})
    }

    @Test
    fun extensionsPlugins() = snapshot("plugins") {
        ExtensionsOverlay(visible = true, initialTab = 0, vm = AppViewModel(), onDismiss = {})
    }

    @Test
    fun extensionsMcp() = snapshot("mcp") {
        ExtensionsOverlay(visible = true, initialTab = 2, vm = AppViewModel(), onDismiss = {})
    }

    @Test
    fun newAgent() = snapshot("new-agent") {
        NewAgentScreen(vm = AppViewModel())
    }

    @Test
    fun cozeTemplates() = snapshot("templates") {
        CozeTemplatesScreen(vm = AppViewModel())
    }

    @Test
    fun agentForm() = snapshot("form") {
        AgentFormScreen(vm = AppViewModel(), templateId = null)
    }
}
