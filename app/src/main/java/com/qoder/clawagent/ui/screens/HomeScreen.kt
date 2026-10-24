package com.qoder.clawagent.ui.screens

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SmartToy
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qoder.clawagent.AppViewModel
import com.qoder.clawagent.model.Agent
import com.qoder.clawagent.model.AppScreen
import com.qoder.clawagent.model.Project
import com.qoder.clawagent.ui.AgentAvatar
import com.qoder.clawagent.ui.ClawBottomBar
import com.qoder.clawagent.ui.ProfileMiniAvatar
import com.qoder.clawagent.ui.ProjectIcon
import com.qoder.clawagent.ui.theme.Bg
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Line
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.White

@Composable
fun HomeScreen(
    vm: AppViewModel,
    onOpenExtensions: (Int) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var plusMenuOpen by remember { mutableStateOf(false) }
    var newProjectOpen by rememberSaveable { mutableStateOf(false) }
    var projectName by rememberSaveable { mutableStateOf("") }
    var projectIsWebsite by rememberSaveable { mutableStateOf(false) }
    val searchFocus = remember { FocusRequester() }

    LaunchedEffect(searchOpen) {
        if (searchOpen) {
            runCatching { searchFocus.requestFocus() }
        }
    }

    val agents = vm.agents.toList().filter { it.visibleInList }
    val projects = vm.projects.toList()
    val filteredAgents = if (query.isBlank()) {
        agents
    } else {
        agents.filter { it.name.contains(query, true) || it.subtitle.contains(query, true) }
    }
    val filteredProjects = if (query.isBlank()) {
        projects
    } else {
        projects.filter { it.name.contains(query, true) || it.subtitle.contains(query, true) }
    }
    val visibleAgents = if (expanded || query.isNotBlank()) filteredAgents else filteredAgents.take(5)

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
                Text(
                    text = "对话",
                    color = Ink,
                    fontSize = 29.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp,
                )
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(White)
                        .clickable {
                            vm.navigate(AppScreen.Profile)
                        }
                        .padding(start = 5.dp, end = 11.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfileMiniAvatar(27.dp)
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = "个人进阶",
                        color = Ink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        searchOpen = !searchOpen
                        if (!searchOpen) query = ""
                    },
                    modifier = Modifier.size(42.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "搜索",
                        tint = Ink,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Box {
                    IconButton(
                        onClick = { plusMenuOpen = true },
                        modifier = Modifier.size(42.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddCircleOutline,
                            contentDescription = "新建",
                            tint = Ink,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    DropdownMenu(
                        expanded = plusMenuOpen,
                        onDismissRequest = { plusMenuOpen = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = White,
                        shadowElevation = 12.dp,
                        modifier = Modifier.width(228.dp),
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.SmartToy,
                                        contentDescription = null,
                                        tint = Ink,
                                        modifier = Modifier.size(22.dp),
                                    )
                                    Spacer(Modifier.width(14.dp))
                                    Text("新建Agent", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                                }
                            },
                            onClick = {
                                plusMenuOpen = false
                                vm.navigate(AppScreen.NewAgent)
                            },
                            modifier = Modifier.height(54.dp),
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.AddComment,
                                        contentDescription = null,
                                        tint = Ink,
                                        modifier = Modifier.size(22.dp),
                                    )
                                    Spacer(Modifier.width(14.dp))
                                    Text("新建项目", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                                }
                            },
                            onClick = {
                                plusMenuOpen = false
                                projectName = ""
                                projectIsWebsite = false
                                newProjectOpen = true
                            },
                            modifier = Modifier.height(54.dp),
                        )
                    }
                }
            }

            if (searchOpen) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(White)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = Muted,
                        modifier = Modifier.size(19.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(searchFocus),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 15.sp, color = Ink),
                        cursorBrush = SolidColor(Ink),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        decorationBox = { inner ->
                            Box {
                                if (query.isEmpty()) {
                                    Text("搜索 Agent 或项目", color = Muted, fontSize = 15.sp)
                                }
                                inner()
                            }
                        },
                    )
                    if (query.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "清空",
                            tint = Muted,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { query = "" },
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 110.dp),
            ) {
                item {
                    Text(
                        text = "Agent",
                        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
                        color = Muted,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                items(visibleAgents, key = { it.id }) { agent ->
                    AgentRow(agent) {
                        vm.navigate(AppScreen.Chat(agent.id))
                    }
                }

                if (query.isBlank() && agents.size > 5) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                                .padding(vertical = 13.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (expanded) "收起" else "展开",
                                color = Muted,
                                fontSize = 14.5.sp,
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                contentDescription = null,
                                tint = Muted,
                                modifier = Modifier.size(19.dp),
                            )
                        }
                    }
                }

                if (filteredProjects.isNotEmpty()) {
                    item {
                        Text(
                            text = "项目",
                            modifier = Modifier.padding(top = 22.dp, bottom = 6.dp),
                            color = Muted.copy(alpha = 0.92f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    items(filteredProjects, key = { it.id }) { project ->
                        ProjectRow(project) {
                            vm.navigate(AppScreen.ProjectDetail(project.id))
                        }
                    }
                }

                if (filteredAgents.isEmpty() && filteredProjects.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 70.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("没有找到相关内容", color = Muted, fontSize = 15.sp)
                            Spacer(Modifier.height(6.dp))
                            Text("试试搜索其他关键词", color = Muted.copy(alpha = 0.7f), fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        ClawBottomBar(
            selected = 0,
            onSelect = { index ->
                when (index) {
                    1 -> vm.switchTab("日程")
                    2 -> onOpenExtensions(1)
                    3 -> vm.switchTab("云盘")
                    4 -> vm.switchTab("更多")
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (newProjectOpen) {
        AlertDialog(
            onDismissRequest = { newProjectOpen = false },
            shape = RoundedCornerShape(22.dp),
            containerColor = White,
            title = { Text("新建项目", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = { if (it.length <= 30) projectName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("项目名称", color = Muted, fontSize = 14.sp) },
                        textStyle = TextStyle(fontSize = 15.sp, color = Ink),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Ink,
                            unfocusedBorderColor = Line,
                            cursorColor = Ink,
                        ),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("项目类型", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(7.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProjectTypeChip(
                            label = "通用项目",
                            selected = !projectIsWebsite,
                            onClick = { projectIsWebsite = false },
                        )
                        ProjectTypeChip(
                            label = "网站项目",
                            selected = projectIsWebsite,
                            onClick = { projectIsWebsite = true },
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "项目会创建专属工作区与隐藏 Agent，对话仍使用同一套 ChatConversation。",
                        color = Muted,
                        fontSize = 11.5.sp,
                        lineHeight = 17.sp,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = projectName.isNotBlank(),
                    onClick = {
                        val project = vm.createProject(projectName, projectIsWebsite)
                        newProjectOpen = false
                        vm.navigate(AppScreen.ProjectDetail(project.id))
                    },
                ) {
                    Text("创建", color = Ink, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { newProjectOpen = false }) {
                    Text("取消", color = Muted)
                }
            },
        )
    }
}

@Composable
private fun ProjectTypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) Ink else Color(0xFFF0EEE9))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            color = if (selected) White else Ink,
            fontSize = 12.5.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
        )
    }
}

@Composable
private fun AgentRow(agent: Agent, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AgentAvatar(
            style = agent.avatar,
            size = 52.dp,
            colorOverride = agent.avatarColor,
        )
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = agent.name,
                    modifier = Modifier.weight(1f),
                    color = Ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = agent.timeLabel,
                    modifier = Modifier.padding(top = 3.dp),
                    color = Muted,
                    fontSize = 12.sp,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = agent.subtitle,
                color = Muted,
                fontSize = 13.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ProjectRow(project: Project, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.Top,
    ) {
        ProjectIcon(project.isWebsite)
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = project.name,
                    modifier = Modifier.weight(1f),
                    color = Ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = project.timeLabel,
                    modifier = Modifier.padding(top = 3.dp),
                    color = Muted,
                    fontSize = 12.sp,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = project.subtitle,
                color = Muted,
                fontSize = 13.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
