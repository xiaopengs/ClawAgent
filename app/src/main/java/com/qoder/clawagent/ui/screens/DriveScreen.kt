package com.qoder.clawagent.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.TextSnippet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qoder.clawagent.AppViewModel
import com.qoder.clawagent.model.DriveFile
import com.qoder.clawagent.ui.ClawBottomBar
import com.qoder.clawagent.ui.TagPill
import com.qoder.clawagent.ui.theme.Bg
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.White
import com.qoder.clawagent.util.inspectFile
import kotlinx.coroutines.launch

@Composable
fun DriveScreen(vm: AppViewModel, onOpenExtensions: (Int) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var previewFile by rememberSaveable { mutableStateOf<DriveFile?>(null) }
    var deleteFile by rememberSaveable { mutableStateOf<DriveFile?>(null) }
    var importing by rememberSaveable { mutableStateOf(false) }
    val files = vm.driveFiles.toList()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        importing = true
        scope.launch {
            runCatching { inspectFile(context, uri) }
                .onSuccess { info ->
                    val file = vm.newDriveFile(
                        name = info.name,
                        mimeType = info.mimeType,
                        sizeLabel = info.sizeLabel,
                        uri = uri.toString(),
                        previewText = info.previewText,
                    )
                    vm.addDriveFile(file)
                    Toast.makeText(context, "已添加到云盘", Toast.LENGTH_SHORT).show()
                }
                .onFailure { error ->
                    Toast.makeText(context, "添加失败：${error.message.orEmpty().take(80)}", Toast.LENGTH_SHORT).show()
                }
            importing = false
        }
    }

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
                Text("云盘", color = Ink, fontSize = 29.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = { if (!importing) picker.launch(arrayOf("*/*")) },
                    modifier = Modifier.size(42.dp),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "添加文件", tint = Ink, modifier = Modifier.size(25.dp))
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(White)
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "工作区文件",
                                color = Ink,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = if (importing) "正在读取文件…" else "${files.size} 个文件 · 本地演示工作区，可从对话或本页添加",
                                color = Muted,
                                fontSize = 11.5.sp,
                            )
                        }
                        TextButton(onClick = { if (!importing) picker.launch(arrayOf("*/*")) }) {
                            Text("添加", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                if (files.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("云盘还是空的", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(6.dp))
                            Text("点击右上角 + 从设备添加文件", color = Muted, fontSize = 13.sp)
                        }
                    }
                }

                items(files, key = { it.id }) { file ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(17.dp))
                            .background(White)
                            .clickable {
                                if (file.previewText != null || file.uri != null) {
                                    previewFile = file
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(fileTint(file)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = fileIcon(file),
                                contentDescription = null,
                                tint = Ink,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = file.name,
                                color = Ink,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(5.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(file.sizeLabel, color = Muted, fontSize = 11.5.sp)
                                Spacer(Modifier.width(7.dp))
                                TagPill(text = fileKind(file))
                                Spacer(Modifier.width(7.dp))
                                Text(file.addedLabel, color = Muted, fontSize = 11.5.sp)
                            }
                        }
                        IconButton(
                            onClick = { deleteFile = file },
                            modifier = Modifier.size(34.dp),
                        ) {
                            Icon(
                                Icons.Outlined.DeleteOutline,
                                contentDescription = "删除文件",
                                tint = Muted.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }

        ClawBottomBar(
            selected = 3,
            onSelect = { index ->
                when (index) {
                    0 -> vm.goHome()
                    1 -> vm.switchTab("日程")
                    2 -> onOpenExtensions(1)
                    3 -> Unit
                    4 -> vm.switchTab("更多")
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (previewFile != null) {
        val file = previewFile ?: return
        AlertDialog(
            onDismissRequest = { previewFile = null },
            shape = RoundedCornerShape(22.dp),
            containerColor = White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(fileIcon(file), contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(file.name, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "${file.sizeLabel} · ${file.addedLabel}",
                        color = Muted,
                        fontSize = 12.sp,
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Bg)
                            .padding(13.dp),
                    ) {
                        Text(
                            text = file.previewText ?: "该文件类型不支持文本预览，点击下方按钮用系统应用打开。",
                            modifier = Modifier
                                .height(190.dp)
                                .verticalScroll(rememberScrollState()),
                            color = Ink.copy(alpha = 0.82f),
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                        )
                    }
                }
            },
            confirmButton = {
                if (file.uri != null) {
                    TextButton(
                        onClick = {
                            openDriveFile(context, file)
                        },
                    ) {
                        Icon(Icons.Outlined.OpenInNew, contentDescription = null, tint = Ink, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("用其他应用打开", color = Ink, fontWeight = FontWeight.Medium)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { previewFile = null }) {
                    Text("关闭", color = Muted)
                }
            },
        )
    }

    if (deleteFile != null) {
        val file = deleteFile ?: return
        AlertDialog(
            onDismissRequest = { deleteFile = null },
            shape = RoundedCornerShape(22.dp),
            containerColor = White,
            title = { Text("删除文件？", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    "「${file.name}」会从云盘列表移除，设备上的原文件不受影响。",
                    color = Muted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteDriveFile(file.id)
                        deleteFile = null
                    },
                ) {
                    Text("删除", color = Color(0xFFB34A30), fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteFile = null }) { Text("取消", color = Muted) }
            },
        )
    }
}

private fun fileIcon(file: DriveFile): ImageVector = when {
    file.mimeType.contains("pdf") -> Icons.Outlined.PictureAsPdf
    file.mimeType.startsWith("text") || file.name.endsWith(".md") -> Icons.Outlined.TextSnippet
    else -> Icons.Outlined.Description
}

private fun fileTint(file: DriveFile): Color = when {
    file.mimeType.contains("pdf") -> Color(0xFFFBF0ED)
    file.mimeType.startsWith("text") || file.name.endsWith(".md") -> Color(0xFFEAF2E3)
    else -> Color(0xFFEDEBE7)
}

private fun fileKind(file: DriveFile): String {
    val extension = file.name.substringAfterLast('.', "").uppercase()
    return extension.ifBlank { "FILE" }
}

private fun openDriveFile(context: android.content.Context, file: DriveFile) {
    val uri = file.uri?.let(Uri::parse) ?: return
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, file.mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "没有可以打开此文件的应用", Toast.LENGTH_SHORT).show()
    }
}
