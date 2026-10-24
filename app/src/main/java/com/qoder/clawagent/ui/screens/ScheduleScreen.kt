package com.qoder.clawagent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qoder.clawagent.AppViewModel
import com.qoder.clawagent.ui.ClawBottomBar
import com.qoder.clawagent.ui.TagPill
import com.qoder.clawagent.ui.theme.Bg
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Line
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.White

@Composable
fun ScheduleScreen(vm: AppViewModel, onOpenExtensions: (Int) -> Unit) {
    var addOpen by rememberSaveable { mutableStateOf(false) }
    var draftTitle by rememberSaveable { mutableStateOf("") }
    var draftTime by rememberSaveable { mutableStateOf("每天 09:00") }
    var draftRepeat by rememberSaveable { mutableStateOf("每天") }
    var agentName by rememberSaveable { mutableStateOf(vm.agents.firstOrNull { it.visibleInList }?.name.orEmpty()) }
    val tasks = vm.scheduleTasks.toList()
    val pending = tasks.count { !it.done }

    Box(Modifier.fillMaxSize().background(Bg)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, top = 10.dp, bottom = 4.dp)
                    .height(50.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("日程", color = Ink, fontSize = 29.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { addOpen = true }, modifier = Modifier.size(42.dp)) {
                    Icon(Icons.Outlined.Add, contentDescription = "添加日程", tint = Ink, modifier = Modifier.size(25.dp))
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFEAF2E3))
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(White.copy(alpha = 0.75f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = Ink, modifier = Modifier.size(19.dp))
                        }
                        Spacer(Modifier.width(11.dp))
                        Column {
                            Text(
                                text = "$pending 个待执行 · ${tasks.size} 个定时任务",
                                color = Ink,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text("任务到点后由对应 Agent 执行，结果会回到会话。", color = Muted, fontSize = 11.5.sp)
                        }
                    }
                }

                if (tasks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("还没有定时任务", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(6.dp))
                            Text("点击右上角 + 添加", color = Muted, fontSize = 13.sp)
                        }
                    }
                }

                items(tasks, key = { it.id }) { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(White)
                            .clickable { vm.toggleScheduleTask(task.id) }
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (task.done) Ink else Color(0xFFF0EEE9)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (task.done) {
                                Icon(Icons.Outlined.Check, contentDescription = null, tint = White, modifier = Modifier.size(17.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                color = if (task.done) Muted else Ink,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(5.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(task.timeLabel, color = Muted, fontSize = 12.sp)
                                Spacer(Modifier.width(7.dp))
                                TagPill(text = task.repeatLabel)
                                Spacer(Modifier.width(7.dp))
                                Text(task.agentName, color = Muted, fontSize = 11.5.sp, maxLines = 1)
                            }
                        }
                        IconButton(
                            onClick = { vm.deleteScheduleTask(task.id) },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                Icons.Outlined.DeleteOutline,
                                contentDescription = "删除日程",
                                tint = Muted.copy(alpha = 0.7f),
                                modifier = Modifier.size(19.dp),
                            )
                        }
                    }
                }
            }
        }

        ClawBottomBar(
            selected = 1,
            onSelect = { index ->
                when (index) {
                    0 -> vm.goHome()
                    1 -> Unit
                    2 -> onOpenExtensions(1)
                    3 -> vm.switchTab("云盘")
                    4 -> vm.switchTab("更多")
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (addOpen) {
        AlertDialog(
            onDismissRequest = { addOpen = false },
            shape = RoundedCornerShape(22.dp),
            containerColor = White,
            title = { Text("新建定时任务", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = draftTitle,
                        onValueChange = { if (it.length <= 60) draftTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("任务名称", color = Muted, fontSize = 14.sp) },
                        textStyle = TextStyle(fontSize = 15.sp, color = Ink),
                        shape = RoundedCornerShape(12.dp),
                        colors = scheduleFieldColors(),
                    )
                    Spacer(Modifier.height(9.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = draftTime,
                            onValueChange = { if (it.length <= 30) draftTime = it },
                            modifier = Modifier.weight(1.3f),
                            singleLine = true,
                            label = { Text("时间", color = Muted, fontSize = 11.sp) },
                            textStyle = TextStyle(fontSize = 14.sp, color = Ink),
                            shape = RoundedCornerShape(12.dp),
                            colors = scheduleFieldColors(),
                        )
                        OutlinedTextField(
                            value = draftRepeat,
                            onValueChange = { if (it.length <= 12) draftRepeat = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("重复", color = Muted, fontSize = 11.sp) },
                            textStyle = TextStyle(fontSize = 14.sp, color = Ink),
                            shape = RoundedCornerShape(12.dp),
                            colors = scheduleFieldColors(),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("执行 Agent", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(7.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        vm.agents.filter { it.visibleInList }.forEach { agent ->
                            val selected = agent.name == agentName
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(if (selected) Ink else Color(0xFFF0EEE9))
                                    .clickable { agentName = agent.name }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = agent.name,
                                    color = if (selected) White else Ink,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = draftTitle.isNotBlank(),
                    onClick = {
                        vm.addScheduleTask(draftTitle, draftTime, draftRepeat, agentName)
                        draftTitle = ""
                        addOpen = false
                    },
                ) {
                    Text("添加", color = Ink, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { addOpen = false }) { Text("取消", color = Muted) }
            },
        )
    }
}

@Composable
private fun scheduleFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Ink,
    unfocusedBorderColor = Line,
    cursorColor = Ink,
)
