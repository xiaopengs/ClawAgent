package com.qoder.clawagent.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qoder.clawagent.AppViewModel
import com.qoder.clawagent.data.SampleData
import com.qoder.clawagent.model.McpService
import com.qoder.clawagent.model.PluginItem
import com.qoder.clawagent.model.Skill
import com.qoder.clawagent.ui.AddChip
import com.qoder.clawagent.ui.CircleIconButton
import com.qoder.clawagent.ui.EmojiTile
import com.qoder.clawagent.ui.McpIcon
import com.qoder.clawagent.ui.TagPill
import com.qoder.clawagent.ui.theme.Bg
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.White

@Composable
fun ExtensionsOverlay(
    visible: Boolean,
    initialTab: Int,
    vm: AppViewModel,
    onDismiss: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(initialTab.coerceIn(0, 2)) }

    LaunchedEffect(visible, initialTab) {
        if (visible) {
            selectedTab = initialTab.coerceIn(0, 2)
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + slideInVertically(tween(260), initialOffsetY = { it / 16 }),
        exit = fadeOut(tween(160)) + slideOutVertically(tween(200), targetOffsetY = { it / 22 }),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.34f))
                .pointerInput(Unit) {
                    detectTapGestures { onDismiss() }
                },
        ) {
            val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.93f)
                    .shadow(
                        elevation = 24.dp,
                        shape = sheetShape,
                        clip = false,
                        ambientColor = Color(0x33000000),
                        spotColor = Color(0x33000000),
                    )
                    .clip(sheetShape)
                    .background(Bg)
                    .pointerInput(Unit) { detectTapGestures { } },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 18.dp, top = 20.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "扩展",
                        color = Ink,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.weight(1f))
                    CircleIconButton(
                        icon = Icons.Outlined.Close,
                        contentDescription = "关闭",
                        onClick = onDismiss,
                        size = 36.dp,
                        background = Color(0xFFEAE8E4),
                        bordered = false,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .height(47.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFE9E7E3))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    listOf("插件", "技能", "MCP").forEachIndexed { index, label ->
                        val selected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selected) White else Color.Transparent)
                                .clickable { selectedTab = index },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                color = if (selected) Ink else Ink.copy(alpha = 0.62f),
                                fontSize = 15.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }

                Crossfade(
                    targetState = selectedTab,
                    animationSpec = tween(170),
                    label = "extension-tab",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) { tab ->
                    when (tab) {
                        0 -> PluginsTab(vm)
                        1 -> SkillsTab(vm)
                        else -> McpTab(vm)
                    }
                }
            }
        }
    }
}

@Composable
private fun PluginsTab(vm: AppViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 26.dp),
    ) {
        item {
            Text(
                text = "为 Agent 添加可调用的工具能力",
                color = Muted,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
        items(SampleData.plugins, key = { it.id }) { plugin ->
            PluginCard(
                plugin = plugin,
                added = vm.addedExtensions[plugin.id] ?: false,
                onToggle = { vm.toggleExtension(plugin.id) },
            )
            Spacer(Modifier.height(9.dp))
        }
    }
}

@Composable
private fun PluginCard(
    plugin: PluginItem,
    added: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(White)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmojiTile(icon = plugin.icon, bgColor = plugin.iconBg, size = 46.dp)
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = plugin.name,
                color = Ink,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = plugin.description,
                color = Muted,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(10.dp))
        AddChip(added = added, onClick = onToggle)
    }
}

@Composable
private fun SkillsTab(vm: AppViewModel) {
    var category by rememberSaveable { mutableStateOf("全部") }
    val categories = listOf("全部") + SampleData.skillSections.map { it.title }
    val sections = if (category == "全部") {
        SampleData.skillSections
    } else {
        SampleData.skillSections.filter { it.title == category }
    }
    val featured = listOfNotNull(
        SampleData.skillSections.firstOrNull { it.title == "金融" }?.skills?.firstOrNull(),
        SampleData.skillSections.firstOrNull { it.title == "自媒体" }?.skills?.firstOrNull(),
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(top = 10.dp, bottom = 30.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                categories.forEach { label ->
                    val selected = category == label
                    Box(
                        modifier = Modifier
                            .height(37.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .background(if (selected) Ink else White)
                            .clickable { category = label }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            color = if (selected) White else Ink,
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
            }
        }

        if (featured.isNotEmpty()) {
            item {
                Spacer(Modifier.height(16.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(featured, key = { it.id }) { skill ->
                        FeaturedSkillCard(
                            skill = skill,
                            added = vm.addedExtensions[skill.id] ?: false,
                            onToggle = { vm.toggleExtension(skill.id) },
                        )
                    }
                }
            }
        }

        sections.forEach { section ->
            item(key = "header-${section.title}") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 21.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = section.title,
                        color = Ink,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = Muted,
                        modifier = Modifier
                            .padding(start = 1.dp)
                            .size(19.dp),
                    )
                }
            }
            item(key = "card-${section.title}") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(White),
                ) {
                    Column {
                        section.skills.forEachIndexed { index, skill ->
                            if (index > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 76.dp, end = 16.dp)
                                        .height(1.dp)
                                        .background(Color(0xFFF3F1ED)),
                                )
                            }
                            SkillRow(
                                skill = skill,
                                added = vm.addedExtensions[skill.id] ?: false,
                                onToggle = { vm.toggleExtension(skill.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedSkillCard(
    skill: Skill,
    added: Boolean,
    onToggle: () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .width(310.dp)
            .clip(shape)
            .background(White)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            EmojiTile(icon = skill.icon, bgColor = skill.iconBg, size = 54.dp, radius = 15.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = skill.name,
                    color = Ink,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    lineHeight = 22.sp,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = skill.description,
                    color = Muted,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                TagPill(text = skill.tag)
            }
            Spacer(Modifier.width(8.dp))
            AddChip(added = added, onClick = onToggle, modifier = Modifier.padding(top = 2.dp))
        }
        if (skill.footer != null) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFF1EFEB)),
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(42.dp).height(20.dp)) {
                    listOf("📈", "📊", "📰").forEachIndexed { index, icon ->
                        Box(
                            modifier = Modifier
                                .offset(x = (index * 13).dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF6F2EA))
                                .border(1.dp, White, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = icon, fontSize = 9.sp)
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = skill.footer,
                    color = Muted,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun SkillRow(
    skill: Skill,
    added: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmojiTile(icon = skill.icon, bgColor = skill.iconBg, size = 48.dp, radius = 14.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = skill.name,
                color = Ink,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = skill.description,
                color = Muted,
                fontSize = 12.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(5.dp))
            TagPill(text = skill.tag, positive = skill.tag.contains("免费"))
        }
        Spacer(Modifier.width(8.dp))
        AddChip(added = added, onClick = onToggle)
    }
}

@Composable
private fun McpTab(vm: AppViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 26.dp),
    ) {
        item {
            Text(
                text = "通过 MCP 连接你常用的服务与数据",
                color = Muted,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
        items(SampleData.mcpServices, key = { it.id }) { service ->
            McpCard(
                service = service,
                added = vm.addedExtensions[service.id] ?: false,
                onToggle = { vm.toggleExtension(service.id) },
            )
            Spacer(Modifier.height(9.dp))
        }
    }
}

@Composable
private fun McpCard(
    service: McpService,
    added: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(White)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        McpIcon(kind = service.icon, size = 46.dp)
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = service.name,
                color = Ink,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = service.description,
                color = Muted,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(10.dp))
        AddChip(added = added, onClick = onToggle)
    }
}
