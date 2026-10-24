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
import com.qoder.clawagent.ui.screens.AgentSettingsScreen
import com.qoder.clawagent.ui.screens.ChatScreen
import com.qoder.clawagent.ui.screens.CozeTemplatesScreen
import com.qoder.clawagent.ui.screens.DriveScreen
import com.qoder.clawagent.ui.screens.ExtensionsOverlay
import com.qoder.clawagent.ui.screens.HomeScreen
import com.qoder.clawagent.ui.screens.LocalAgentScreen
import com.qoder.clawagent.ui.screens.NewAgentScreen
import com.qoder.clawagent.ui.screens.ProfileScreen
import com.qoder.clawagent.ui.screens.ProjectDetailScreen
import com.qoder.clawagent.ui.screens.ScheduleScreen
import com.qoder.clawagent.ui.screens.ThirdPartyAgentsScreen
import com.qoder.clawagent.ui.screens.TopicTrackingScreen
import com.qoder.clawagent.ui.screens.UpgradeScreen
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

    @Test
    fun profile() = snapshot("profile") {
        ProfileScreen(vm = AppViewModel(), embedded = true)
    }

    @Test
    fun schedule() = snapshot("schedule") {
        ScheduleScreen(vm = AppViewModel(), onOpenExtensions = {})
    }

    @Test
    fun drive() = snapshot("drive") {
        DriveScreen(vm = AppViewModel(), onOpenExtensions = {})
    }

    @Test
    fun thirdPartyAgents() = snapshot("third-party") {
        ThirdPartyAgentsScreen(vm = AppViewModel())
    }

    @Test
    fun localAgent() = snapshot("local-agent") {
        LocalAgentScreen(vm = AppViewModel())
    }

    @Test
    fun upgrade() = snapshot("upgrade") {
        UpgradeScreen(vm = AppViewModel())
    }

    @Test
    fun topicTracking() = snapshot("topics") {
        TopicTrackingScreen(vm = AppViewModel())
    }

    @Test
    fun projectDetail() = snapshot("project-detail") {
        ProjectDetailScreen(vm = AppViewModel(), projectId = "inspiration-site")
    }

    @Test
    fun agentSettings() {
        val vm = AppViewModel()
        val local = vm.createLocalAgent(
            name = "本地 Llama",
            endpoint = "http://10.0.2.2:11434/v1",
            model = "llama3.2",
            apiKey = "",
            workdir = "local",
        )
        snapshot("agent-settings") {
            AgentSettingsScreen(vm = vm, agentId = local.id)
        }
    }
}
