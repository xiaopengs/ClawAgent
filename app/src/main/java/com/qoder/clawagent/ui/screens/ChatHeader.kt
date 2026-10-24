package com.qoder.clawagent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qoder.clawagent.model.Agent
import com.qoder.clawagent.model.AgentKind
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.White

@Composable
internal fun ChatHeader(
    agent: Agent,
    moreMenuOpen: Boolean,
    onMoreMenuChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onClear: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAgentSettings: () -> Unit,
    onCloudClick: () -> Unit,
) {
    val runtimeLabel = when (agent.kind) {
        AgentKind.COZE -> "云端"
        AgentKind.THIRD_PARTY -> "云电脑"
        AgentKind.LOCAL -> "本地"
    }
    val runtimeIcon = when (agent.kind) {
        AgentKind.COZE -> Icons.Outlined.Cloud
        AgentKind.THIRD_PARTY -> Icons.Outlined.CloudQueue
        AgentKind.LOCAL -> Icons.Outlined.Computer
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 6.dp),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(44.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.ChevronLeft,
                contentDescription = "返回",
                tint = Ink,
                modifier = Modifier.size(29.dp),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 62.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = agent.name,
                color = Ink,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onCloudClick)
                    .padding(horizontal = 3.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    Icon(
                        imageVector = runtimeIcon,
                        contentDescription = null,
                        tint = Muted,
                        modifier = Modifier.size(16.dp),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-1).dp)
                            .size(6.dp)
                            .background(Color(0xFF35C28F), CircleShape),
                    )
                }
                Spacer(Modifier.width(3.dp))
                Text(runtimeLabel, color = Muted, fontSize = 12.sp)
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = Muted,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            IconButton(
                onClick = { onMoreMenuChange(true) },
                modifier = Modifier.size(44.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreHoriz,
                    contentDescription = "更多",
                    tint = Ink,
                    modifier = Modifier.size(24.dp),
                )
            }
            DropdownMenu(
                expanded = moreMenuOpen,
                onDismissRequest = { onMoreMenuChange(false) },
                shape = RoundedCornerShape(16.dp),
                containerColor = White,
                shadowElevation = 10.dp,
                modifier = Modifier.width(176.dp),
            ) {
                DropdownMenuItem(
                    text = { Text("清空对话", color = Ink, fontSize = 15.sp) },
                    onClick = {
                        onMoreMenuChange(false)
                        onClear()
                    },
                )
                DropdownMenuItem(
                    text = { Text("模型设置", color = Ink, fontSize = 15.sp) },
                    onClick = {
                        onMoreMenuChange(false)
                        onOpenSettings()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Agent 设置", color = Ink, fontSize = 15.sp) },
                    onClick = {
                        onMoreMenuChange(false)
                        onOpenAgentSettings()
                    },
                )
            }
        }
    }
}
