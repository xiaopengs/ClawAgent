package com.qoder.clawagent.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qoder.clawagent.AppViewModel
import com.qoder.clawagent.chat.ReplyPhase
import com.qoder.clawagent.chat.SendResult
import com.qoder.clawagent.ui.theme.Bg
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Line
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.White

@Composable
fun ChatScreen(
    vm: AppViewModel,
    agentId: String,
    onOpenExtensions: (Int) -> Unit,
) {
    val agent = vm.agentById(agentId)
    if (agent == null) {
        LaunchedEffect(Unit) { vm.goBack() }
        Box(Modifier.fillMaxSize().background(Bg))
        return
    }

    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val chatState by vm.chatState(agentId).collectAsState()
    val messages = chatState.messages
    val isTyping = chatState.phase == ReplyPhase.WAITING
    var input by rememberSaveable { mutableStateOf("") }
    var panelExpanded by rememberSaveable { mutableStateOf(true) }
    var moreMenuOpen by rememberSaveable { mutableStateOf(false) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }

    val send: () -> Unit = {
        val text = input.trim()
        if (text.isNotEmpty()) {
            when (vm.sendMessage(agentId, text)) {
                SendResult.SENT -> input = ""
                SendResult.BUSY -> Toast.makeText(context, "正在等待上一条回复", Toast.LENGTH_SHORT).show()
                SendResult.EMPTY -> Unit
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .imePadding(),
    ) {
        ChatHeader(
            agent = agent,
            moreMenuOpen = moreMenuOpen,
            onMoreMenuChange = { moreMenuOpen = it },
            onBack = { vm.goBack() },
            onClear = { vm.clearChat(agentId) },
            onOpenSettings = { settingsOpen = true },
            onCloudClick = {
                Toast.makeText(context, "当前运行在云端工作区", Toast.LENGTH_SHORT).show()
            },
        )

        ChatThread(
            agent = agent,
            messages = messages,
            isTyping = isTyping,
            onProcessedClick = { message ->
                Toast.makeText(
                    context,
                    "本次任务共处理 ${message.processed ?: "1秒"}",
                    Toast.LENGTH_SHORT,
                ).show()
            },
            onLinkClick = {
                Toast.makeText(context, "话题追踪工作台即将上线", Toast.LENGTH_SHORT).show()
            },
            onRetry = { vm.retryLast(agentId) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 4.dp),
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        clip = false,
                        ambientColor = Color(0x14000000),
                        spotColor = Color(0x0D000000),
                    )
                    .background(White, RoundedCornerShape(28.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, Line, CircleShape)
                        .clickable {
                            panelExpanded = !panelExpanded
                            if (panelExpanded) keyboard?.hide()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (panelExpanded) Icons.Outlined.Close else Icons.Outlined.Add,
                        contentDescription = if (panelExpanded) "收起功能面板" else "展开功能面板",
                        tint = Ink,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = input,
                    onValueChange = { if (it.length <= 2000) input = it },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) panelExpanded = false
                        },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 15.5.sp, color = Ink),
                    cursorBrush = SolidColor(Ink),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { send() }),
                    decorationBox = { innerTextField ->
                        Box {
                            if (input.isEmpty()) {
                                Text(
                                    text = "发送消息或长按说话开始任务",
                                    color = Muted,
                                    fontSize = 14.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                if (input.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Ink)
                            .clickable { send() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowUpward,
                            contentDescription = "发送",
                            tint = White,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "语音任务即将上线", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(38.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.GraphicEq,
                            contentDescription = "语音",
                            tint = Ink,
                            modifier = Modifier.size(23.dp),
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = panelExpanded,
                enter = expandVertically(tween(200)) + fadeIn(tween(160)),
                exit = shrinkVertically(tween(180)) + fadeOut(tween(100)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ActionTile(
                            icon = Icons.Outlined.Image,
                            label = "图片",
                            modifier = Modifier.weight(1f),
                        ) {
                            Toast.makeText(context, "从相册选择图片（演示）", Toast.LENGTH_SHORT).show()
                        }
                        ActionTile(
                            icon = Icons.AutoMirrored.Outlined.InsertDriveFile,
                            label = "文件",
                            modifier = Modifier.weight(1f),
                        ) {
                            Toast.makeText(context, "选择文件（演示）", Toast.LENGTH_SHORT).show()
                        }
                        ActionTile(
                            icon = Icons.Outlined.HeadsetMic,
                            label = "旁听",
                            modifier = Modifier.weight(1f),
                        ) {
                            Toast.makeText(context, "旁听模式即将上线", Toast.LENGTH_SHORT).show()
                        }
                        ActionTile(
                            icon = Icons.Outlined.Extension,
                            label = "插件",
                            modifier = Modifier.weight(1f),
                        ) {
                            keyboard?.hide()
                            onOpenExtensions(0)
                        }
                        ActionTile(
                            icon = Icons.Outlined.BarChart,
                            label = "技能",
                            modifier = Modifier.weight(1f),
                        ) {
                            keyboard?.hide()
                            onOpenExtensions(1)
                        }
                    }
                }
            }
        }
    }

    if (settingsOpen) {
        OpenRouterSettingsDialog(
            initial = vm.openRouterConfig(),
            onSave = vm::saveOpenRouterConfig,
            onDismiss = { settingsOpen = false },
        )
    }
}

@Composable
private fun ActionTile(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xFFEAE9E6))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Ink,
            modifier = Modifier.size(23.dp),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            color = Ink,
            fontSize = 12.5.sp,
        )
    }
}
