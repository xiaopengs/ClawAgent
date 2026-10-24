package com.qoder.clawagent.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qoder.clawagent.AppViewModel
import com.qoder.clawagent.model.AppScreen
import com.qoder.clawagent.ui.ProjectIcon
import com.qoder.clawagent.ui.ScreenHeader
import com.qoder.clawagent.ui.theme.Bg
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Line
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.White

@Composable
fun ProjectDetailScreen(vm: AppViewModel, projectId: String) {
    val project = vm.projectById(projectId)
    if (project == null) {
        LaunchedEffect(Unit) { vm.goBack() }
        Box(Modifier.fillMaxSize().background(Bg))
        return
    }
    var menuOpen by remember { mutableStateOf(false) }
    var renameOpen by rememberSaveable { mutableStateOf(false) }
    var deleteOpen by rememberSaveable { mutableStateOf(false) }
    var draftName by rememberSaveable { mutableStateOf(project.name) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding(),
    ) {
        ScreenHeader(
            title = "项目",
            onBack = { vm.goBack() },
            actions = {
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "更多", tint = Ink)
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = White,
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Edit, contentDescription = null, tint = Ink, modifier = Modifier.size(19.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text("重命名项目", color = Ink, fontSize = 15.sp)
                                }
                            },
                            onClick = {
                                menuOpen = false
                                draftName = project.name
                                renameOpen = true
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Outlined.DeleteOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFB34A30),
                                        modifier = Modifier.size(19.dp),
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text("删除项目", color = Color(0xFFB34A30), fontSize = 15.sp)
                                }
                            },
                            onClick = {
                                menuOpen = false
                                deleteOpen = true
                            },
                        )
                    }
                }
            },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProjectIcon(project.isWebsite, size = 62.dp)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        color = Ink,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = if (project.isWebsite) "网站项目 · 扣子编程" else "协作项目 · 通用工作区",
                        color = Muted,
                        fontSize = 13.sp,
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = project.subtitle,
                color = Ink.copy(alpha = 0.76f),
                fontSize = 14.5.sp,
                lineHeight = 22.sp,
            )
            Spacer(Modifier.height(22.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(White)
                    .padding(18.dp),
            ) {
                Text("项目工作区", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "项目对话复用同一个 ChatConversation，支持 OpenRouter、ADK 与本地模型。",
                    color = Muted,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                )
                Spacer(Modifier.height(16.dp))
                InfoRow(icon = Icons.Outlined.FolderOpen, label = "工作目录", value = project.name)
                Spacer(Modifier.height(10.dp))
                InfoRow(
                    icon = Icons.Outlined.ArrowForward,
                    label = "最近活跃",
                    value = project.timeLabel,
                )
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Ink)
                        .clickable {
                            val agent = vm.ensureProjectAgent(project)
                            vm.navigate(AppScreen.Chat(agent.id))
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text("打开项目对话", color = White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = White, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(White)
                    .border(1.dp, Line.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                    .padding(16.dp),
            ) {
                Text("项目资料", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (project.isWebsite) {
                        "网站预览、需求与代码修改会沉淀在项目对话中。你可以继续让 Agent 调整页面结构或补齐功能。"
                    } else {
                        "把需求、文件和讨论放进项目工作区，Agent 会在同一上下文中持续推进。"
                    },
                    color = Muted,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                )
            }
            Spacer(Modifier.height(30.dp))
        }
    }

    if (renameOpen) {
        AlertDialog(
            onDismissRequest = { renameOpen = false },
            shape = RoundedCornerShape(22.dp),
            containerColor = White,
            title = { Text("重命名项目", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { if (it.length <= 30) draftName = it },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 15.sp, color = Ink),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Ink,
                        unfocusedBorderColor = Line,
                        cursorColor = Ink,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    enabled = draftName.isNotBlank(),
                    onClick = {
                        vm.renameProject(project.id, draftName)
                        renameOpen = false
                    },
                ) {
                    Text("保存", color = Ink, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { renameOpen = false }) { Text("取消", color = Muted) }
            },
        )
    }

    if (deleteOpen) {
        AlertDialog(
            onDismissRequest = { deleteOpen = false },
            shape = RoundedCornerShape(22.dp),
            containerColor = White,
            title = { Text("删除项目？", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    "项目「${project.name}」及其专属对话会从本机移除，此操作不可撤销。",
                    color = Muted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteOpen = false
                        vm.deleteProject(project.id)
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
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(9.dp))
        Text(label, color = Muted, fontSize = 13.sp)
        Spacer(Modifier.weight(1f))
        Text(value, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
