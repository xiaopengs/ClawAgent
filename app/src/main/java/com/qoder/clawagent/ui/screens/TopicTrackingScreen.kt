package com.qoder.clawagent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qoder.clawagent.AppViewModel
import com.qoder.clawagent.ui.ScreenHeader
import com.qoder.clawagent.ui.TagPill
import com.qoder.clawagent.ui.theme.Bg
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Line
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.White

@Composable
fun TopicTrackingScreen(vm: AppViewModel, focusTopicId: String? = null) {
    var addOpen by rememberSaveable { mutableStateOf(false) }
    var draftTitle by rememberSaveable { mutableStateOf("") }
    var draftSummary by rememberSaveable { mutableStateOf("") }
    var deleteId by rememberSaveable { mutableStateOf<String?>(null) }
    val topics = vm.topics.toList()
    val deleting = deleteId?.let { id -> topics.firstOrNull { it.id == id } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding(),
    ) {
        ScreenHeader(
            title = "话题追踪",
            onBack = { vm.goBack() },
            actions = {
                IconButton(onClick = { addOpen = true }) {
                    Icon(Icons.Outlined.Add, contentDescription = "添加话题", tint = Ink)
                }
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFEDF1FD))
                        .padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.TrackChanges,
                        contentDescription = null,
                        tint = Color(0xFF5067E8),
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(11.dp))
                    Text(
                        text = "持续跟踪对话里的话题，新的进展会汇总到这里。",
                        color = Ink.copy(alpha = 0.76f),
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                    )
                }
            }

            if (topics.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("还没有追踪的话题", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(6.dp))
                        Text("从对话中的话题卡或右上角添加", color = Muted, fontSize = 13.sp)
                    }
                }
            }

            items(topics, key = { it.id }) { topic ->
                val focused = focusTopicId == topic.id
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(White)
                        .border(
                            width = if (focused) 1.5.dp else 1.dp,
                            color = if (focused) Color(0xFFB8C4FF) else Color.Transparent,
                            shape = RoundedCornerShape(20.dp),
                        )
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = topic.title,
                                    modifier = Modifier.weight(1f, fill = false),
                                    color = Ink,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 22.sp,
                                )
                                if (focused) {
                                    Spacer(Modifier.width(7.dp))
                                    TagPill(text = "来自对话")
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = topic.summary,
                                color = Muted,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(topic.source, color = Muted, fontSize = 11.5.sp)
                                Text(" · ", color = Muted, fontSize = 11.5.sp)
                                Text(topic.updatedLabel, color = Muted, fontSize = 11.5.sp)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Switch(
                                checked = topic.followed,
                                onCheckedChange = { vm.toggleTopicFollow(topic.id) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = White,
                                    checkedTrackColor = Ink,
                                    uncheckedThumbColor = White,
                                    uncheckedTrackColor = Line,
                                ),
                            )
                            Spacer(Modifier.height(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "删除话题",
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { deleteId = topic.id },
                                tint = Muted.copy(alpha = 0.75f),
                            )
                        }
                    }
                }
            }
        }
    }

    if (addOpen) {
        AlertDialog(
            onDismissRequest = { addOpen = false },
            shape = RoundedCornerShape(22.dp),
            containerColor = White,
            title = { Text("追踪新话题", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = draftTitle,
                        onValueChange = { if (it.length <= 60) draftTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("话题标题", color = Muted, fontSize = 14.sp) },
                        textStyle = TextStyle(fontSize = 15.sp, color = Ink),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Ink,
                            unfocusedBorderColor = Line,
                            cursorColor = Ink,
                        ),
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = draftSummary,
                        onValueChange = { if (it.length <= 160) draftSummary = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        placeholder = { Text("关注点或补充说明（可选）", color = Muted, fontSize = 14.sp) },
                        textStyle = TextStyle(fontSize = 15.sp, color = Ink),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Ink,
                            unfocusedBorderColor = Line,
                            cursorColor = Ink,
                        ),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = draftTitle.isNotBlank(),
                    onClick = {
                        vm.addTopic(draftTitle, draftSummary)
                        draftTitle = ""
                        draftSummary = ""
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

    if (deleting != null) {
        AlertDialog(
            onDismissRequest = { deleteId = null },
            shape = RoundedCornerShape(22.dp),
            containerColor = White,
            title = { Text("删除追踪？", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    "「${deleting.title}」会从话题列表中移除。",
                    color = Muted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteTopic(deleting.id)
                        deleteId = null
                    },
                ) {
                    Text("删除", color = Color(0xFFB34A30), fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteId = null }) { Text("取消", color = Muted) }
            },
        )
    }
}
