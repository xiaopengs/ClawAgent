package com.qoder.clawagent.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qoder.clawagent.AppViewModel
import com.qoder.clawagent.model.Plan
import com.qoder.clawagent.ui.ScreenHeader
import com.qoder.clawagent.ui.theme.Bg
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.Purple
import com.qoder.clawagent.ui.theme.White

@Composable
fun UpgradeScreen(vm: AppViewModel) {
    val context = LocalContext.current
    val isPro = vm.plan == Plan.PRO

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding(),
    ) {
        ScreenHeader(title = "个人进阶", onBack = { vm.goBack() })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF26222F), Color(0xFF11100F)),
                            ),
                        )
                        .padding(22.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(Color(0xFF342E45)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Diamond,
                                contentDescription = null,
                                tint = Color(0xFFC8B7FF),
                                modifier = Modifier.size(23.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isPro) "Pro 会员 · 已解锁" else "爪爪 Pro",
                                color = White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = if (isPro) "当前为本地演示 Pro 状态" else "把 Agent 放到更大的工作区里",
                                color = White.copy(alpha = 0.58f),
                                fontSize = 12.5.sp,
                            )
                        }
                    }
                    Spacer(Modifier.height(22.dp))
                    listOf(
                        "创建不限数量的 Agent",
                        "云电脑长任务与定时运行",
                        "更高并发与更长上下文",
                        "专属模型路由与用量面板",
                    ).forEach { feature ->
                        Row(
                            modifier = Modifier.padding(bottom = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color(0xFF9BE2C5),
                                modifier = Modifier.size(17.dp),
                            )
                            Spacer(Modifier.width(9.dp))
                            Text(feature, color = White.copy(alpha = 0.9f), fontSize = 14.sp)
                        }
                    }
                }
            }

            item {
                PlanCard(
                    title = "Free",
                    price = "¥0",
                    caption = "当前可用",
                    selected = !isPro,
                    badge = "基础",
                    features = listOf("3 个自定义 Agent", "OpenRouter / ADK 双引擎", "本地 Agent 接入"),
                    onClick = {},
                )
            }

            item {
                PlanCard(
                    title = "Pro",
                    price = "¥39 / 月",
                    caption = if (isPro) "已启用" else "演示升级，不产生真实扣费",
                    selected = isPro,
                    badge = "推荐",
                    features = listOf("无限 Agent 与项目", "云电脑 Agent 工作区", "定时任务与文件沉淀"),
                    onClick = {
                        if (!isPro) {
                            vm.upgradeToPro()
                            Toast.makeText(context, "已切换到 Pro 演示套餐", Toast.LENGTH_SHORT).show()
                        }
                    },
                )
            }

            item {
                Text(
                    text = "这是本地套餐状态演示，不包含支付与订阅能力。接入真实支付前请勿用于线上收费。",
                    color = Muted,
                    fontSize = 11.5.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    caption: String,
    selected: Boolean,
    badge: String,
    features: List<String>,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .border(
                width = if (selected) 1.6.dp else 1.dp,
                color = if (selected) Purple else Color(0xFFE7E3DC),
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onClick)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Text(
                text = badge,
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (selected) Color(0xFFF0EBFF) else Color(0xFFF2F0EC))
                    .padding(horizontal = 7.dp, vertical = 3.dp),
                color = if (selected) Purple else Muted,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.weight(1f))
            Text(price, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(6.dp))
        Text(caption, color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(14.dp))
        features.forEach { feature ->
            Row(
                modifier = Modifier.padding(bottom = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) Purple else Muted.copy(alpha = 0.55f)),
                )
                Spacer(Modifier.width(9.dp))
                Text(feature, color = Ink.copy(alpha = 0.82f), fontSize = 13.sp)
            }
        }
    }
}
