package com.qoder.clawagent.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qoder.clawagent.AppViewModel
import com.qoder.clawagent.data.SampleData
import com.qoder.clawagent.model.AgentKind
import com.qoder.clawagent.network.LocalProbeResult
import com.qoder.clawagent.ui.ScreenHeader
import com.qoder.clawagent.ui.TagPill
import com.qoder.clawagent.ui.theme.Bg
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Line
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.White
import kotlinx.coroutines.launch

@Composable
fun AgentSettingsScreen(vm: AppViewModel, agentId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val agent = vm.agentById(agentId)
    if (agent == null) {
        Box(Modifier.fillMaxSize().background(Bg))
        return
    }

    var name by rememberSaveable(agent.id) { mutableStateOf(agent.name) }
    var workdir by rememberSaveable(agent.id) { mutableStateOf(agent.workdir) }
    var description by rememberSaveable(agent.id) { mutableStateOf(agent.description) }
    var extensions by remember(agent.id) { mutableStateOf(agent.extensions.toSet()) }
    var endpoint by rememberSaveable(agent.id) { mutableStateOf(agent.localEndpoint.orEmpty()) }
    var model by rememberSaveable(agent.id) { mutableStateOf(agent.localModel.orEmpty()) }
    var apiKey by rememberSaveable(agent.id) { mutableStateOf(agent.localApiKey.orEmpty()) }
    var probing by remember { mutableStateOf(false) }
    var probe by remember { mutableStateOf<LocalProbeResult?>(null) }
    var deleteOpen by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
            .imePadding(),
    ) {
        ScreenHeader(
            title = "Agent 设置",
            onBack = { vm.goBack() },
            actions = {
                if (vm.canDeleteAgent(agent) && agent.visibleInList) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .clickable { deleteOpen = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                    ) {
                        Text("删除", color = Color(0xFFB34A30), fontSize = 13.5.sp)
                    }
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 4.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(White)
                    .padding(15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Color(0xFFF3F1EE)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = Ink,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = when (agent.kind) {
                            AgentKind.COZE -> "扣子 Agent"
                            AgentKind.THIRD_PARTY -> "云电脑 Agent"
                            AgentKind.LOCAL -> "本地 Agent"
                        },
                        color = Ink,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = when (agent.kind) {
                            AgentKind.COZE -> "共享 OpenRouter / ADK 推理与扩展能力"
                            AgentKind.THIRD_PARTY -> "预设命令 ${agent.command.orEmpty()}，真实 CLI 需云电脑 worker"
                            AgentKind.LOCAL -> "推理直接发送到本地 OpenAI 兼容端点"
                        },
                        color = Muted,
                        fontSize = 11.5.sp,
                        lineHeight = 17.sp,
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            SettingsLabel("Agent 名称")
            Spacer(Modifier.height(7.dp))
            SettingsField(value = name, onValueChange = { name = it.take(24) }, placeholder = "Agent 名称")

            Spacer(Modifier.height(14.dp))
            SettingsLabel("工作目录")
            Spacer(Modifier.height(7.dp))
            SettingsField(
                value = workdir,
                onValueChange = { workdir = it.take(40) },
                placeholder = "例如：灵感池",
                leadingIcon = Icons.Outlined.FolderOpen,
            )

            Spacer(Modifier.height(14.dp))
            SettingsLabel("描述")
            Spacer(Modifier.height(7.dp))
            SettingsField(
                value = description,
                onValueChange = { description = it.take(160) },
                placeholder = "这个 Agent 擅长什么？",
            )

            if (agent.kind == AgentKind.COZE) {
                Spacer(Modifier.height(18.dp))
                SettingsLabel("扩展能力")
                Spacer(Modifier.height(7.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(White)
                        .padding(vertical = 4.dp),
                ) {
                    SampleData.extensionOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    extensions = if (option.name in extensions) {
                                        extensions - option.name
                                    } else {
                                        extensions + option.name
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Extension,
                                contentDescription = null,
                                tint = Muted,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(9.dp))
                            Text(
                                text = option.name,
                                modifier = Modifier.weight(1f),
                                color = Ink,
                                fontSize = 14.sp,
                            )
                            TagPill(text = option.group)
                            Spacer(Modifier.width(4.dp))
                            Checkbox(
                                checked = option.name in extensions,
                                onCheckedChange = {
                                    extensions = if (option.name in extensions) {
                                        extensions - option.name
                                    } else {
                                        extensions + option.name
                                    }
                                },
                                modifier = Modifier.size(22.dp),
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Ink,
                                    uncheckedColor = Line,
                                    checkmarkColor = White,
                                ),
                            )
                        }
                    }
                }
            }

            if (agent.kind == AgentKind.LOCAL) {
                Spacer(Modifier.height(18.dp))
                SettingsLabel("本地端点")
                Spacer(Modifier.height(7.dp))
                SettingsField(
                    value = endpoint,
                    onValueChange = { endpoint = it.take(180) },
                    placeholder = "http://192.168.1.8:11434/v1",
                )
                Spacer(Modifier.height(10.dp))
                SettingsField(
                    value = model,
                    onValueChange = { model = it.take(100) },
                    placeholder = "模型 ID，例如 llama3.2",
                )
                Spacer(Modifier.height(10.dp))
                SettingsField(
                    value = apiKey,
                    onValueChange = { apiKey = it.take(200) },
                    placeholder = "Local API Key（可选）",
                    password = true,
                )

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEDEBE7))
                        .clickable(enabled = !probing && endpoint.isNotBlank()) {
                            probing = true
                            probe = null
                            scope.launch {
                                val result = vm.probeLocalEndpoint(endpoint, apiKey)
                                probe = result
                                if (result.models.isNotEmpty() && model.isBlank()) {
                                    model = result.models.first()
                                }
                                probing = false
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (probing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            strokeWidth = 1.6.dp,
                            color = Ink,
                        )
                        Spacer(Modifier.width(7.dp))
                        Text("正在探测 /models…", color = Ink, fontSize = 13.sp)
                    } else {
                        Text("测试连接", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }

                val state = probe
                if (state != null) {
                    Spacer(Modifier.height(9.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(13.dp))
                            .background(if (state.error == null) Color(0xFFEAF6F0) else Color(0xFFFCEEE9))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (state.error == null) Icons.Outlined.CheckCircle else Icons.Outlined.CloudOff,
                            contentDescription = null,
                            tint = if (state.error == null) Color(0xFF2E946C) else Color(0xFFB34A30),
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = state.error ?: "已连通，发现 ${state.models.size} 个模型",
                            modifier = Modifier.weight(1f),
                            color = if (state.error == null) Color(0xFF257A58) else Color(0xFF8C3522),
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                        )
                    }
                    if (state.models.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            state.models.take(12).forEach { candidate ->
                                val selected = candidate == model
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(if (selected) Ink else White)
                                        .clickable { model = candidate }
                                        .padding(horizontal = 9.dp, vertical = 5.dp),
                                ) {
                                    Text(
                                        text = candidate,
                                        color = if (selected) White else Ink,
                                        fontSize = 11.5.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF7F5F2))
                    .padding(13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "保存后会同步到当前会话，下一轮消息立即生效。",
                    color = Muted,
                    fontSize = 11.5.sp,
                    lineHeight = 17.sp,
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            val enabled = name.isNotBlank() && (agent.kind != AgentKind.LOCAL || (endpoint.startsWith("http") && model.isNotBlank()))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(if (enabled) Ink else Color(0xFF96938F))
                    .clickable(enabled = enabled) {
                        vm.saveAgent(
                            agent.copy(
                                name = name,
                                workdir = workdir,
                                description = description,
                                extensions = extensions.toList(),
                                localEndpoint = if (agent.kind == AgentKind.LOCAL) endpoint else agent.localEndpoint,
                                localModel = if (agent.kind == AgentKind.LOCAL) model else agent.localModel,
                                localApiKey = if (agent.kind == AgentKind.LOCAL) apiKey else agent.localApiKey,
                            ),
                        )
                        Toast.makeText(context, "Agent 设置已保存", Toast.LENGTH_SHORT).show()
                        vm.goBack()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text("保存设置", color = White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    if (deleteOpen) {
        AlertDialog(
            onDismissRequest = { deleteOpen = false },
            shape = RoundedCornerShape(22.dp),
            containerColor = White,
            title = { Text("删除 Agent？", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    "「${agent.name}」及其对话会从本机移除，此操作不可撤销。",
                    color = Muted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteOpen = false
                        vm.deleteAgent(agent.id)
                        vm.goHome()
                    },
                ) {
                    Text("删除", color = Color(0xFFB34A30), fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteOpen = false }) { Text("取消", color = Muted) }
            },
        )
    }
}

@Composable
private fun SettingsLabel(text: String) {
    Text(text, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun SettingsField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    password: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(White)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = Muted,
                modifier = Modifier.size(19.dp),
            )
            Spacer(Modifier.width(8.dp))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = TextStyle(fontSize = 15.sp, color = Ink),
            cursorBrush = SolidColor(Ink),
            visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(placeholder, color = Muted, fontSize = 14.sp)
                    }
                    inner()
                }
            },
        )
    }
}
