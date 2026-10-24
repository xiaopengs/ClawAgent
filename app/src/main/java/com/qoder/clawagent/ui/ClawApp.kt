package com.qoder.clawagent.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qoder.clawagent.AppViewModel
import com.qoder.clawagent.model.AppScreen
import com.qoder.clawagent.ui.screens.AgentFormScreen
import com.qoder.clawagent.ui.screens.AgentSettingsScreen
import com.qoder.clawagent.ui.screens.ChatScreen
import com.qoder.clawagent.ui.screens.CozeTemplatesScreen
import com.qoder.clawagent.ui.screens.ExtensionsOverlay
import com.qoder.clawagent.ui.screens.HomeScreen
import com.qoder.clawagent.ui.screens.LocalAgentScreen
import com.qoder.clawagent.ui.screens.NewAgentScreen
import com.qoder.clawagent.ui.screens.ProfileScreen
import com.qoder.clawagent.ui.screens.ProjectDetailScreen
import com.qoder.clawagent.ui.screens.TabScreen
import com.qoder.clawagent.ui.screens.ThirdPartyAgentsScreen
import com.qoder.clawagent.ui.screens.TopicTrackingScreen
import com.qoder.clawagent.ui.screens.UpgradeScreen
import com.qoder.clawagent.ui.theme.Bg

@Composable
fun ClawApp(vm: AppViewModel = viewModel()) {
    val current = vm.backStack.lastOrNull() ?: AppScreen.Home
    var extensionsVisible by rememberSaveable { mutableStateOf(false) }
    var extensionsTab by rememberSaveable { mutableStateOf(1) }

    val openExtensions: (Int) -> Unit = { tab ->
        extensionsTab = tab
        extensionsVisible = true
    }

    BackHandler(enabled = extensionsVisible) {
        extensionsVisible = false
    }
    BackHandler(enabled = !extensionsVisible && vm.backStack.size > 1) {
        vm.goBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg),
    ) {
        AnimatedContent(
            targetState = current,
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(130))
            },
            label = "main-screen",
            modifier = Modifier.fillMaxSize(),
        ) { screen ->
            when (screen) {
                AppScreen.Home -> HomeScreen(
                    vm = vm,
                    onOpenExtensions = openExtensions,
                )

                is AppScreen.Chat -> ChatScreen(
                    vm = vm,
                    agentId = screen.agentId,
                    onOpenExtensions = openExtensions,
                )

                AppScreen.NewAgent -> NewAgentScreen(vm)

                AppScreen.CozeTemplates -> CozeTemplatesScreen(vm)

                is AppScreen.AgentForm -> AgentFormScreen(
                    vm = vm,
                    templateId = screen.templateId,
                )

                is AppScreen.ProjectDetail -> ProjectDetailScreen(
                    vm = vm,
                    projectId = screen.projectId,
                )

                is AppScreen.AgentSettings -> AgentSettingsScreen(
                    vm = vm,
                    agentId = screen.agentId,
                )

                is AppScreen.TopicTracking -> TopicTrackingScreen(
                    vm = vm,
                    focusTopicId = screen.topicId,
                )

                AppScreen.ThirdPartyAgents -> ThirdPartyAgentsScreen(vm)

                AppScreen.LocalAgent -> LocalAgentScreen(vm)

                AppScreen.Profile -> ProfileScreen(vm = vm)

                AppScreen.Upgrade -> UpgradeScreen(vm)

                is AppScreen.Tab -> TabScreen(
                    vm = vm,
                    title = screen.title,
                    onOpenExtensions = openExtensions,
                )
            }
        }

        ExtensionsOverlay(
            visible = extensionsVisible,
            initialTab = extensionsTab,
            vm = vm,
            onDismiss = { extensionsVisible = false },
        )
    }
}
