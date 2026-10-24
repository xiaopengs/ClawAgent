package com.qoder.clawagent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qoder.clawagent.AppViewModel
import com.qoder.clawagent.BuildConfig
import com.qoder.clawagent.model.AppScreen
import com.qoder.clawagent.model.Plan
import com.qoder.clawagent.ui.ClawBottomBar
import com.qoder.clawagent.ui.ProfileMiniAvatar
import com.qoder.clawagent.ui.ScreenHeader
import com.qoder.clawagent.ui.theme.Bg
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Line
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.Purple
import com.qoder.clawagent.ui.theme.White

@Composable
fun ProfileScreen(
    vm: AppViewModel,
    embedded: Boolean = false,
    onOpenExtensions: (Int) -> Unit = {},
) {
    var modelSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var aboutOpen by rememberSaveable { mutableStateOf(false) }
    var helpOpen by rememberSaveable { mutableStateOf(false) }
    val isPro = vm.plan == Plan.PRO
    val agentCount = vm.agents.count { it.visibleInList }
    val followedTopics = vm.topics.count { it.followed }

    Box(Modifier.fillMaxSize().background(Bg)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            if (embedded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 4.dp)
                        .height(50.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("更多", color = Ink, fontSize = 29.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = if (isPro) "Pro" else "Free",
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isPro) Color(0xFFF0EBFF) else Color(0xFFEDEBE7))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        color = if (isPro) Purple else Muted,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                ScreenHeader(title = "个人进阶", onBack = { vm.goBack() })
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 4.dp,
                    bottom = if (embedded) 110.dp else 28.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(White)
                            .padding(17.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProfileMiniAvatar(54.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("爪爪用户", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (isPro) "Pro 会员 · 本地演示状态" else "Free 套餐 · 本地工作区",
                                color = Muted,
                                fontSize = 12.5.sp,
                            )
                        }
                        Text(
                            text = if (isPro) "已进阶" else "个人进阶",
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF0EBFF))
                                .clickable { vm.navigate(AppScreen.Upgrade) }
                                .padding(horizontal = 11.dp, vertical = 7.dp),
                            color = Purple,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(White)
                            .padding(vertical = 15.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        ProfileStat(value = agentCount.toString(), label = "Agent")
                        ProfileStat(value = vm.projects.size.toString(), label = "项目")
                        ProfileStat(value = followedTopics.toString(), label = "关注话题")
                        ProfileStat(value = vm.driveFiles.size.toString(), label = "云盘文件")
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF1ECFF))
                            .clickable { vm.navigate(AppScreen.Upgrade) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(White.copy(alpha = 0.72f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Diamond,
                                contentDescription = null,
                                tint = Purple,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = if (isPro) "爪爪 Pro · 已解锁" else "升级爪爪 Pro",
                                color = Ink,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = if (isPro) "无限 Agent、云电脑长任务与定时运行" else "解锁无限 Agent、云电脑任务与更长上下文",
                                color = Muted,
                                fontSize = 11.5.sp,
                                lineHeight = 17.sp,
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = Muted,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }

                item { ProfileSectionLabel("工作区") }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(White),
                    ) {
                        ProfileRow(
                            icon = Icons.Outlined.Tune,
                            title = "模型设置",
                            subtitle = "${vm.openRouterConfig().model} · ${engineTitle(vm)}",
                            onClick = { modelSettingsOpen = true },
                        )
                        ProfileDivider()
                        ProfileRow(
                            icon = Icons.Outlined.TrackChanges,
                            title = "话题追踪",
                            subtitle = "已关注 $followedTopics 个话题",
                            onClick = { vm.navigate(AppScreen.TopicTracking()) },
                        )
                        ProfileDivider()
                        ProfileRow(
                            icon = Icons.Outlined.CloudQueue,
                            title = "三方精选 Agent",
                            subtitle = "Claude Code、Codex CLI、Gemini CLI、OpenCode",
                            onClick = { vm.navigate(AppScreen.ThirdPartyAgents) },
                        )
                        ProfileDivider()
                        ProfileRow(
                            icon = Icons.Outlined.Computer,
                            title = "接入本地 Agent",
                            subtitle = "Ollama、LM Studio、vLLM 等 OpenAI 兼容端点",
                            onClick = { vm.navigate(AppScreen.LocalAgent) },
                        )
                    }
                }

                item { ProfileSectionLabel("偏好") }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(White),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { vm.setNotifications(!vm.notificationsEnabled) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = null,
                                tint = Ink,
                                modifier = Modifier.size(21.dp),
                            )
                            Spacer(Modifier.width(13.dp))
                            Column(Modifier.weight(1f)) {
                                Text("任务通知", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(3.dp))
                                Text("定时任务完成后在会话内提醒", color = Muted, fontSize = 11.5.sp)
                            }
                            Switch(
                                checked = vm.notificationsEnabled,
                                onCheckedChange = { vm.setNotifications(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = White,
                                    checkedTrackColor = Ink,
                                    uncheckedThumbColor = White,
                                    uncheckedTrackColor = Line,
                                ),
                            )
                        }
                    }
                }

                item { ProfileSectionLabel("关于") }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(White),
                    ) {
                        ProfileRow(
                            icon = Icons.AutoMirrored.Outlined.HelpOutline,
                            title = "帮助与反馈",
                            subtitle = "附件、语音、本地端点的使用说明",
                            onClick = { helpOpen = true },
                        )
                        ProfileDivider()
                        ProfileRow(
                            icon = Icons.Outlined.Info,
                            title = "关于 ClawAgent",
                            subtitle = "版本 ${BuildConfig.VERSION_NAME}",
                            onClick = { aboutOpen = true },
                        )
                    }
                }

                item {
                    Text(
                        text = "ClawAgent · Kotlin + Jetpack Compose\nOpenRouter / Google ADK / 本地 OpenAI 兼容端点",
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                        color = Muted.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        lineHeight = 17.sp,
                    )
                }
            }
        }

        if (embedded) {
            ClawBottomBar(
                selected = 4,
                onSelect = { index ->
                    when (index) {
                        0 -> vm.goHome()
                        1 -> vm.switchTab("日程")
                        2 -> onOpenExtensions(1)
                        3 -> vm.switchTab("云盘")
                        4 -> Unit
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    if (modelSettingsOpen) {
        OpenRouterSettingsDialog(
            initial = vm.openRouterConfig(),
            onSave = vm::saveOpenRouterConfig,
            onDismiss = { modelSettingsOpen = false },
        )
    }

    if (helpOpen) {
        AlertDialog(
            onDismissRequest = { helpOpen = false },
            shape = RoundedCornerShape(22.dp),
            containerColor = White,
            title = { Text("帮助与反馈", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    text = listOf(
                        "附件：对话输入框点击「图片」或「文件」。图片会压缩后直接随消息发送；文本文件会读入前 16KB 作为上下文。",
                        "语音：点击输入框右侧波形按钮开始语音输入；「旁听」会持续收音，结束后把转写发送给 Agent。",
                        "本地 Agent：Base URL 需指向 OpenAI 兼容的 /v1 端点，可先用「测试连接」探测模型列表。",
                        "三方 Agent：当前版本保存云电脑预设与命令；真实 CLI 需要连接云电脑 worker。",
                    ).joinToString("\n\n"),
                    color = Muted,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { helpOpen = false }) {
                    Text("知道了", color = Ink, fontWeight = FontWeight.Medium)
                }
            },
        )
    }

    if (aboutOpen) {
        AlertDialog(
            onDismissRequest = { aboutOpen = false },
            shape = RoundedCornerShape(22.dp),
            containerColor = White,
            title = { Text("关于 ClawAgent", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    text = "版本 ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n\n" +
                        "一个原生 Android 的智能体对话工作台：Jetpack Compose 界面、ChatConversation 会话状态机，" +
                        "通过 OpenRouter、Google ADK for Kotlin 与本地 OpenAI 兼容端点完成真实推理。\n\n" +
                        "API Key 仅保存在本机 SharedPreferences 或内存中，不会上传到本项目服务器。",
                    color = Muted,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { aboutOpen = false }) {
                    Text("关闭", color = Ink, fontWeight = FontWeight.Medium)
                }
            },
        )
    }
}

private fun engineTitle(vm: AppViewModel): String =
    if (vm.openRouterConfig().engine == com.qoder.clawagent.chat.ReplyEngine.ADK) "ADK Agent" else "OpenRouter 直连"

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(3.dp))
        Text(label, color = Muted, fontSize = 11.5.sp)
    }
}

@Composable
private fun ProfileSectionLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
        color = Muted,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(21.dp))
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = Muted, fontSize = 11.5.sp, maxLines = 1)
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = Muted,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun ProfileDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 50.dp, end = 16.dp)
            .height(1.dp)
            .background(Line.copy(alpha = 0.55f)),
    )
}
