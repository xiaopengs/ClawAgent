package com.qoder.clawagent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Icon
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
import com.qoder.clawagent.AppViewModel
import com.qoder.clawagent.data.SampleData
import com.qoder.clawagent.model.AppScreen
import com.qoder.clawagent.model.Template
import com.qoder.clawagent.ui.RoundEmojiAvatar
import com.qoder.clawagent.ui.ScreenHeader
import com.qoder.clawagent.ui.theme.Bg
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.Purple
import com.qoder.clawagent.ui.theme.White

@Composable
fun CozeTemplatesScreen(vm: AppViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding(),
    ) {
        ScreenHeader(title = "新建扣子 Agent", onBack = { vm.goBack() })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 26.dp),
        ) {
            item {
                UpgradeBanner {
                    vm.navigate(AppScreen.Upgrade)
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "直接创建",
                    color = Ink,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                DirectCreateCard {
                    vm.navigate(AppScreen.AgentForm(null))
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    text = "选择职业模板",
                    color = Ink,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
            }

            items(SampleData.templates, key = { it.id }) { template ->
                TemplateRow(template) {
                    vm.navigate(AppScreen.AgentForm(template.id))
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun UpgradeBanner(onUpgrade: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFE9E6E2))
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Diamond,
            contentDescription = null,
            tint = Purple,
            modifier = Modifier.size(21.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "当前套餐最多可接入 3 个 Agent，\n升级后可创建更多 Agent",
            modifier = Modifier.weight(1f),
            color = Ink.copy(alpha = 0.86f),
            fontSize = 13.5.sp,
            lineHeight = 19.sp,
        )
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .height(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Ink)
                .clickable(onClick = onUpgrade)
                .padding(horizontal = 15.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "升级",
                color = White,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun DirectCreateCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F1EE)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.SmartToy,
                contentDescription = null,
                tint = Ink,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = "直接创建",
            modifier = Modifier.weight(1f),
            color = Ink,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = Muted,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun TemplateRow(template: Template, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(White)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundEmojiAvatar(
            icon = template.icon,
            bgColor = template.iconBg,
            size = 48.dp,
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = template.name,
            modifier = Modifier.weight(1f),
            color = Ink,
            fontSize = 17.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = Muted,
            modifier = Modifier.size(20.dp),
        )
    }
}
