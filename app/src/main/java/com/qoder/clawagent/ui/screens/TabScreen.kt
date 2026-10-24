package com.qoder.clawagent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qoder.clawagent.AppViewModel
import com.qoder.clawagent.ui.ClawBottomBar
import com.qoder.clawagent.ui.theme.Bg
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.White

@Composable
fun TabScreen(
    vm: AppViewModel,
    title: String,
    onOpenExtensions: (Int) -> Unit,
) {
    val selectedIndex = when (title) {
        "日程" -> 1
        "云盘" -> 3
        else -> 4
    }
    val icon: ImageVector = when (title) {
        "日程" -> Icons.Outlined.CalendarToday
        "云盘" -> Icons.Outlined.Cloud
        else -> Icons.Outlined.PersonOutline
    }
    val subtitle = when (title) {
        "日程" -> "把 Agent 的定时任务和待办都放在这里。"
        "云盘" -> "查看 Agent 生成的文件与云端工作区产物。"
        else -> "账号设置、帮助与反馈等能力将陆续开放。"
    }

    Box(Modifier.fillMaxSize().background(Bg)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp),
                color = Ink,
                fontSize = 29.sp,
                fontWeight = FontWeight.Bold,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 86.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(White),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Ink,
                        modifier = Modifier.size(34.dp),
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "$title 功能正在准备中",
                    color = Ink,
                    fontSize = 17.5.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    text = subtitle,
                    modifier = Modifier.padding(horizontal = 46.dp),
                    color = Muted,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Ink)
                        .clickable { vm.goHome() }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("回到对话", color = White, fontSize = 13.5.sp)
                }
            }
        }
        ClawBottomBar(
            selected = selectedIndex,
            onSelect = { index ->
                when (index) {
                    0 -> vm.goHome()
                    1 -> vm.switchTab("日程")
                    2 -> onOpenExtensions(1)
                    3 -> vm.switchTab("云盘")
                    4 -> vm.switchTab("更多")
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
