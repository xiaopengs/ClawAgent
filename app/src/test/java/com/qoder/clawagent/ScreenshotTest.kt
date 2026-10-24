package com.qoder.clawagent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
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

class ScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material.Light.NoActionBar",
        showSystemUi = false,
    )

    @Test
    fun home() {
        paparazzi.snapshot {
            ClawTheme {
                Box(Modifier.fillMaxSize().background(Bg)) {
                    HomeScreen(vm = AppViewModel(), onOpenExtensions = {})
                }
            }
        }
    }

    @Test
    fun chat() {
        paparazzi.snapshot {
            ClawTheme {
                Box(Modifier.fillMaxSize().background(Bg)) {
                    ChatScreen(vm = AppViewModel(), agentId = "heizhua", onOpenExtensions = {})
                }
            }
        }
    }

    @Test
    fun extensionsSkills() {
        paparazzi.snapshot {
            ClawTheme {
                Box(Modifier.fillMaxSize().background(Bg)) {
                    HomeScreen(vm = AppViewModel(), onOpenExtensions = {})
                    ExtensionsOverlay(visible = true, initialTab = 1, vm = AppViewModel(), onDismiss = {})
                }
            }
        }
    }

    @Test
    fun extensionsPlugins() {
        paparazzi.snapshot {
            ClawTheme {
                Box(Modifier.fillMaxSize().background(Bg)) {
                    ExtensionsOverlay(visible = true, initialTab = 0, vm = AppViewModel(), onDismiss = {})
                }
            }
        }
    }

    @Test
    fun extensionsMcp() {
        paparazzi.snapshot {
            ClawTheme {
                Box(Modifier.fillMaxSize().background(Bg)) {
                    ExtensionsOverlay(visible = true, initialTab = 2, vm = AppViewModel(), onDismiss = {})
                }
            }
        }
    }

    @Test
    fun newAgent() {
        paparazzi.snapshot {
            ClawTheme {
                Box(Modifier.fillMaxSize().background(Bg)) {
                    NewAgentScreen(vm = AppViewModel())
                }
            }
        }
    }

    @Test
    fun cozeTemplates() {
        paparazzi.snapshot {
            ClawTheme {
                Box(Modifier.fillMaxSize().background(Bg)) {
                    CozeTemplatesScreen(vm = AppViewModel())
                }
            }
        }
    }

    @Test
    fun agentForm() {
        paparazzi.snapshot {
            ClawTheme {
                Box(Modifier.fillMaxSize().background(Bg)) {
                    AgentFormScreen(vm = AppViewModel(), templateId = null)
                }
            }
        }
    }
}
