package com.qoder.clawagent.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qoder.clawagent.AppViewModel
import com.qoder.clawagent.data.SampleData
import com.qoder.clawagent.model.AppScreen
import com.qoder.clawagent.model.ThirdPartyAgent
import com.qoder.clawagent.ui.AgentAvatar
import com.qoder.clawagent.ui.ScreenHeader
import com.qoder.clawagent.ui.TagPill
import com.qoder.clawagent.ui.theme.Bg
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Line
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.Purple
import com.qoder.clawagent.ui.theme.White

@Composable
fun ThirdPartyAgentsScreen(vm: AppViewModel) {
    val context = LocalContext.current
    var selected by rememberSaveable { mutableStateOf<ThirdPartyAgent?>(null) }
    var draftName by rememberSaveable { mutableStateOf("") }
    var draftWorkdir by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding(),
    ) {
        ScreenHeader(title = "三方精选 Agent", onBack = { vm.goBack() })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFF1ECFF))
                        .padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.CloudQueue,
                        contentDescription = null,
                        tint = Purple,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(11.dp))
                    Column {
                        Text(
                            text = "云电脑 Agent 工作区",
                            color = Ink,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "本版本提供云电脑配置与对话流程；真实 CLI 执行需要连接 worker。",
                            color = Muted,
                            fontSize = 11.5.sp,
                            lineHeight = 17.sp,
                        )
                    }
                }
            }

            items(SampleData.thirdPartyAgents, key = { it.id }) { preset ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(White)
                        .clickable {
                            selected = preset
                            draftName = preset.name
                            draftWorkdir = preset.name.lowercase().replace(" ", "-")
                        }
                        .padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AgentAvatar(
                        style = preset.avatar,
                        size = 50.dp,
                        colorOverride = preset.iconBg,
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = preset.name,
                                color = Ink,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.width(7.dp))
                            TagPill(text = preset.command)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = preset.description,
                            color = Muted,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            maxLines = 2,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = Muted,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }

    if (selected != null) {
        val preset = selected ?: return
        AlertDialog(
            onDismissRequest = { selected = null },
            shape = RoundedCornerShape(22.dp),
            containerColor = White,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Diamond,
                    contentDescription = null,
                    tint = Purple,
                    modifier = Modifier.size(24.dp),
                )
            },
            title = {
                Text("接入 ${preset.name}", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = { if (it.length <= 24) draftName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Agent 名称", color = Muted, fontSize = 13.sp) },
                        textStyle = TextStyle(fontSize = 15.sp, color = Ink),
                        shape = RoundedCornerShape(12.dp),
                        colors = dialogFieldColors(),
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = draftWorkdir,
                        onValueChange = { if (it.length <= 40) draftWorkdir = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("云电脑工作目录", color = Muted, fontSize = 13.sp) },
                        textStyle = TextStyle(fontSize = 15.sp, color = Ink),
                        shape = RoundedCornerShape(12.dp),
                        colors = dialogFieldColors(),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "命令 `${preset.command}` 会写入 Agent 配置。当前对话由 OpenRouter / ADK 提供推理，不会启动真实云进程。",
                        color = Muted,
                        fontSize = 11.5.sp,
                        lineHeight = 17.sp,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = draftName.isNotBlank(),
                    onClick = {
                        val agent = vm.createThirdPartyAgent(
                            preset = preset,
                            workdir = draftWorkdir,
                            name = draftName,
                        )
                        selected = null
                        Toast.makeText(context, "已接入 ${agent.name}", Toast.LENGTH_SHORT).show()
                        vm.navigate(AppScreen.Chat(agent.id))
                    },
                ) {
                    Text("创建并对话", color = Ink, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { selected = null }) {
                    Text("取消", color = Muted)
                }
            },
        )
    }
}

@Composable
private fun dialogFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Ink,
    unfocusedBorderColor = Line,
    focusedLabelColor = Ink,
    unfocusedLabelColor = Muted,
    cursorColor = Ink,
)
