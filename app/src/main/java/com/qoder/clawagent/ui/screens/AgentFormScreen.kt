package com.qoder.clawagent.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.qoder.clawagent.AppViewModel
import com.qoder.clawagent.data.SampleData
import com.qoder.clawagent.model.AppScreen
import com.qoder.clawagent.ui.ScreenHeader
import com.qoder.clawagent.ui.TagPill
import com.qoder.clawagent.ui.dashedBorder
import com.qoder.clawagent.ui.theme.Bg
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Line
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.StarRed
import com.qoder.clawagent.ui.theme.White

@Composable
fun AgentFormScreen(
    vm: AppViewModel,
    templateId: String?,
) {
    val context = LocalContext.current
    val template = SampleData.templates.firstOrNull { it.id == templateId }
    val initialName = template?.name ?: "星野"

    var name by rememberSaveable(templateId) { mutableStateOf(initialName) }
    var workdir by rememberSaveable(templateId) { mutableStateOf(initialName) }
    var workdirEdited by rememberSaveable(templateId) { mutableStateOf(false) }
    var description by rememberSaveable(templateId) { mutableStateOf("") }
    var selectedExtensions by remember { mutableStateOf(listOf<String>()) }
    var pickerOpen by remember { mutableStateOf(false) }
    var draftExtensions by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
            .imePadding(),
    ) {
        ScreenHeader(title = "新建扣子 Agent", onBack = { vm.goBack() })

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 4.dp),
        ) {
            UpgradeBanner {
                vm.navigate(AppScreen.Upgrade)
            }
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(White)
                    .padding(16.dp),
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
                Spacer(Modifier.width(15.dp))
                Column {
                    Text(
                        text = "直接创建",
                        color = Ink,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "从空白开始，自由定义 Agent 的扩展能力。",
                        color = Muted,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            FieldLabel(label = "Agent 名称", required = true) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val pool = SampleData.agentNameIdeas
                            val index = pool.indexOf(name)
                            val next = pool[(index + 1).mod(pool.size)]
                            name = next
                            if (!workdirEdited) workdir = next
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        tint = Muted,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("换一个", color = Muted, fontSize = 13.5.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            WhiteField(
                value = name,
                onValueChange = { newValue ->
                    name = newValue.take(24)
                    if (!workdirEdited) workdir = name
                },
                placeholder = "给 Agent 起个名字",
            )

            Spacer(Modifier.height(16.dp))
            FieldLabel(
                label = "工作目录",
                hint = "系统将自动创建，名称可修改",
            )
            Spacer(Modifier.height(8.dp))
            WhiteField(
                value = workdir,
                onValueChange = {
                    workdirEdited = true
                    workdir = it.take(24)
                },
                placeholder = "工作目录",
                leadingIcon = Icons.Outlined.FolderOpen,
            )

            Spacer(Modifier.height(16.dp))
            FieldLabel(label = "描述（可选）")
            Spacer(Modifier.height(8.dp))
            WhiteField(
                value = description,
                onValueChange = { description = it.take(120) },
                placeholder = "请输入描述",
            )

            Spacer(Modifier.height(16.dp))
            FieldLabel(label = "扩展能力")
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .dashedBorder(color = Color(0xFFD9D6D0), radius = 14.dp)
                    .clickable {
                        draftExtensions = selectedExtensions.toSet()
                        pickerOpen = true
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selectedExtensions.isEmpty()) {
                    Text(
                        text = "添加扩展能力（插件、技能和 MCP）",
                        modifier = Modifier.weight(1f),
                        color = Muted,
                        fontSize = 14.sp,
                        maxLines = 1,
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        selectedExtensions.forEach { extension ->
                            Box(
                                modifier = Modifier
                                    .height(27.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEDEBE7))
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = extension,
                                    color = Ink.copy(alpha = 0.8f),
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "添加扩展能力",
                    tint = Muted,
                    modifier = Modifier.size(21.dp),
                )
            }

            Spacer(Modifier.height(30.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            val enabled = name.isNotBlank()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(if (enabled) Ink else Color(0xFF96938F))
                    .clickable(enabled = enabled) {
                        vm.createAgent(
                            name = name,
                            workdir = workdir,
                            description = description,
                            extensions = selectedExtensions,
                        )
                        Toast.makeText(context, "已创建 Agent「${name.trim()}」", Toast.LENGTH_SHORT).show()
                        vm.goHome()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "创建 Agent",
                    color = White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }

    if (pickerOpen) {
        ExtensionPickerDialog(
            selected = draftExtensions,
            onToggle = { optionName ->
                draftExtensions = if (optionName in draftExtensions) {
                    draftExtensions - optionName
                } else {
                    draftExtensions + optionName
                }
            },
            onDismiss = { pickerOpen = false },
            onConfirm = {
                selectedExtensions = draftExtensions.toList()
                pickerOpen = false
            },
        )
    }
}

@Composable
private fun FieldLabel(
    label: String,
    required: Boolean = false,
    hint: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Ink,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        if (required) {
            Text(
                text = " *",
                color = StarRed,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (hint != null) {
            Spacer(Modifier.width(7.dp))
            Text(
                text = hint,
                color = Muted,
                fontSize = 12.sp,
                maxLines = 1,
            )
        }
        Spacer(Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable
private fun WhiteField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(White)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = Muted,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = TextStyle(fontSize = 16.sp, color = Ink),
            cursorBrush = SolidColor(Ink),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Muted,
                            fontSize = 15.sp,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun ExtensionPickerDialog(
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = White,
            shadowElevation = 10.dp,
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = "添加扩展能力",
                    color = Ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "选择插件、技能和 MCP 服务",
                    color = Muted,
                    fontSize = 12.5.sp,
                )
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .heightIn(max = 350.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    SampleData.extensionOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onToggle(option.name) }
                                .padding(horizontal = 4.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = option.name,
                                modifier = Modifier.weight(1f),
                                color = Ink,
                                fontSize = 14.5.sp,
                            )
                            TagPill(text = option.group)
                            Spacer(Modifier.width(4.dp))
                            Checkbox(
                                checked = option.name in selected,
                                onCheckedChange = { onToggle(option.name) },
                                modifier = Modifier.size(22.dp),
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Ink,
                                    uncheckedColor = Line,
                                    checkmarkColor = White,
                                ),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = Muted, fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Ink)
                            .clickable(onClick = onConfirm)
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("确定", color = White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
