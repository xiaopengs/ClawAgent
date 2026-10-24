package com.qoder.clawagent.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.qoder.clawagent.AppViewModel
import com.qoder.clawagent.chat.ReplyPhase
import com.qoder.clawagent.chat.SendResult
import com.qoder.clawagent.model.AgentKind
import com.qoder.clawagent.model.AppScreen
import com.qoder.clawagent.model.AttachmentKind
import com.qoder.clawagent.model.ChatAttachment
import com.qoder.clawagent.model.ChatMessage
import com.qoder.clawagent.ui.theme.Bg
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Line
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.White
import com.qoder.clawagent.util.decodeAttachmentBitmap
import com.qoder.clawagent.util.formatSize
import com.qoder.clawagent.util.loadFileAttachment
import com.qoder.clawagent.util.loadImageAttachment
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    vm: AppViewModel,
    agentId: String,
    onOpenExtensions: (Int) -> Unit,
) {
    val agent = vm.agentById(agentId)
    if (agent == null) {
        LaunchedEffect(Unit) { vm.goBack() }
        Box(Modifier.fillMaxSize().background(Bg))
        return
    }

    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val chatState by vm.chatState(agentId).collectAsState()
    val messages = chatState.messages
    val isTyping = chatState.phase == ReplyPhase.WAITING
    var input by rememberSaveable { mutableStateOf("") }
    var panelExpanded by rememberSaveable { mutableStateOf(true) }
    var moreMenuOpen by rememberSaveable { mutableStateOf(false) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var workspaceOpen by rememberSaveable { mutableStateOf(false) }
    var processedMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var voiceMode by remember { mutableStateOf<VoiceMode?>(null) }
    var pendingVoice by remember { mutableStateOf<VoiceMode?>(null) }
    var loadingAttachments by remember { mutableStateOf(false) }
    val attachments = remember { mutableStateListOf<ChatAttachment>() }

    val voicePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val requested = pendingVoice
        pendingVoice = null
        if (granted && requested != null) {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                voiceMode = requested
            } else {
                Toast.makeText(context, "当前设备没有可用的语音识别服务", Toast.LENGTH_SHORT).show()
            }
        } else if (!granted) {
            Toast.makeText(context, "需要麦克风权限才能使用语音", Toast.LENGTH_SHORT).show()
        }
    }

    val startVoice: (VoiceMode) -> Unit = { mode ->
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                voiceMode = mode
            } else {
                Toast.makeText(context, "当前设备没有可用的语音识别服务", Toast.LENGTH_SHORT).show()
            }
        } else {
            pendingVoice = mode
            voicePermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(4),
    ) { uris ->
        if (uris.isNotEmpty()) {
            val room = MAX_ATTACHMENTS - attachments.size
            if (room <= 0) {
                Toast.makeText(context, "最多附带 $MAX_ATTACHMENTS 个附件", Toast.LENGTH_SHORT).show()
            } else {
                loadingAttachments = true
                scope.launch {
                    var failed = 0
                    uris.take(room).forEach { uri ->
                        runCatching { loadImageAttachment(context, uri) }
                            .onSuccess { attachments.add(it) }
                            .onFailure { failed += 1 }
                    }
                    loadingAttachments = false
                    if (failed > 0) {
                        Toast.makeText(context, "有 $failed 张图片读取失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            if (attachments.size >= MAX_ATTACHMENTS) {
                Toast.makeText(context, "最多附带 $MAX_ATTACHMENTS 个附件", Toast.LENGTH_SHORT).show()
            } else {
                loadingAttachments = true
                scope.launch {
                    runCatching { loadFileAttachment(context, uri) }
                        .onSuccess { attachments.add(it) }
                        .onFailure {
                            Toast.makeText(
                                context,
                                "文件读取失败：${it.message.orEmpty().take(60)}",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    loadingAttachments = false
                }
            }
        }
    }

    val send: () -> Unit = {
        val text = input.trim()
        if (text.isNotEmpty() || attachments.isNotEmpty()) {
            when (vm.sendMessage(agentId, text, attachments.toList())) {
                SendResult.SENT -> {
                    input = ""
                    attachments.clear()
                }
                SendResult.BUSY -> Toast.makeText(context, "正在等待上一条回复", Toast.LENGTH_SHORT).show()
                SendResult.EMPTY -> Unit
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .imePadding(),
    ) {
        ChatHeader(
            agent = agent,
            moreMenuOpen = moreMenuOpen,
            onMoreMenuChange = { moreMenuOpen = it },
            onBack = { vm.goBack() },
            onClear = {
                vm.clearChat(agentId)
                attachments.clear()
            },
            onOpenSettings = { settingsOpen = true },
            onOpenAgentSettings = { vm.navigate(AppScreen.AgentSettings(agentId)) },
            onCloudClick = { workspaceOpen = true },
        )

        ChatThread(
            agent = agent,
            messages = messages,
            isTyping = isTyping,
            onProcessedClick = { message -> processedMessage = message },
            onLinkClick = {
                vm.navigate(AppScreen.TopicTracking(vm.topics.firstOrNull()?.id))
            },
            onRetry = { vm.retryLast(agentId) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 4.dp),
        ) {
            if (loadingAttachments) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(13.dp),
                        strokeWidth = 1.5.dp,
                        color = Muted,
                    )
                    Spacer(Modifier.width(7.dp))
                    Text("正在读取附件…", color = Muted, fontSize = 12.sp)
                }
            }
            if (attachments.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    attachments.forEach { attachment ->
                        PendingAttachmentChip(
                            attachment = attachment,
                            onRemove = { attachments.remove(attachment) },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        clip = false,
                        ambientColor = Color(0x14000000),
                        spotColor = Color(0x0D000000),
                    )
                    .background(White, RoundedCornerShape(28.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, Line, CircleShape)
                        .clickable {
                            panelExpanded = !panelExpanded
                            if (panelExpanded) keyboard?.hide()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (panelExpanded) Icons.Outlined.Close else Icons.Outlined.Add,
                        contentDescription = if (panelExpanded) "收起功能面板" else "展开功能面板",
                        tint = Ink,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = input,
                    onValueChange = { if (it.length <= 2000) input = it },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) panelExpanded = false
                        },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 15.5.sp, color = Ink),
                    cursorBrush = SolidColor(Ink),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { send() }),
                    decorationBox = { innerTextField ->
                        Box {
                            if (input.isEmpty()) {
                                Text(
                                    text = "发送消息或长按说话开始任务",
                                    color = Muted,
                                    fontSize = 14.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                if (input.isNotBlank() || attachments.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Ink)
                            .clickable { send() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowUpward,
                            contentDescription = "发送",
                            tint = White,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                } else {
                    IconButton(
                        onClick = { startVoice(VoiceMode.INPUT) },
                        modifier = Modifier.size(38.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.GraphicEq,
                            contentDescription = "语音输入",
                            tint = Ink,
                            modifier = Modifier.size(23.dp),
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = panelExpanded,
                enter = expandVertically(tween(200)) + fadeIn(tween(160)),
                exit = shrinkVertically(tween(180)) + fadeOut(tween(100)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ActionTile(
                            icon = Icons.Outlined.Image,
                            label = "图片",
                            modifier = Modifier.weight(1f),
                        ) {
                            if (loadingAttachments) {
                                Toast.makeText(context, "正在读取上一个附件", Toast.LENGTH_SHORT).show()
                            } else {
                                imagePicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            }
                        }
                        ActionTile(
                            icon = Icons.AutoMirrored.Outlined.InsertDriveFile,
                            label = "文件",
                            modifier = Modifier.weight(1f),
                        ) {
                            if (loadingAttachments) {
                                Toast.makeText(context, "正在读取上一个附件", Toast.LENGTH_SHORT).show()
                            } else {
                                filePicker.launch(arrayOf("*/*"))
                            }
                        }
                        ActionTile(
                            icon = Icons.Outlined.HeadsetMic,
                            label = "旁听",
                            modifier = Modifier.weight(1f),
                        ) {
                            startVoice(VoiceMode.LISTEN)
                        }
                        ActionTile(
                            icon = Icons.Outlined.Extension,
                            label = "插件",
                            modifier = Modifier.weight(1f),
                        ) {
                            keyboard?.hide()
                            onOpenExtensions(0)
                        }
                        ActionTile(
                            icon = Icons.Outlined.BarChart,
                            label = "技能",
                            modifier = Modifier.weight(1f),
                        ) {
                            keyboard?.hide()
                            onOpenExtensions(1)
                        }
                    }
                }
            }
        }
    }

    if (settingsOpen) {
        OpenRouterSettingsDialog(
            initial = vm.openRouterConfig(),
            onSave = vm::saveOpenRouterConfig,
            onDismiss = { settingsOpen = false },
        )
    }

    if (workspaceOpen) {
        WorkspaceDialog(
            agent = agent,
            modelLabel = vm.modelLabel(agent),
            engineLabel = vm.engineLabel(agent),
            onDismiss = { workspaceOpen = false },
            onOpenSettings = {
                workspaceOpen = false
                if (agent.kind == AgentKind.LOCAL) {
                    vm.navigate(AppScreen.AgentSettings(agent.id))
                } else {
                    settingsOpen = true
                }
            },
        )
    }

    processedMessage?.let { message ->
        ProcessedDialog(
            message = message,
            modelLabel = vm.modelLabel(agent),
            engineLabel = vm.engineLabel(agent),
            onDismiss = { processedMessage = null },
        )
    }

    val activeVoice = voiceMode
    if (activeVoice != null) {
        VoiceCaptureDialog(
            agentName = agent.name,
            mode = activeVoice,
            onConfirm = { text ->
                voiceMode = null
                if (activeVoice == VoiceMode.LISTEN) {
                    when (vm.sendMessage(agentId, text)) {
                        SendResult.SENT -> Unit
                        SendResult.BUSY -> Toast.makeText(context, "正在等待上一条回复", Toast.LENGTH_SHORT).show()
                        SendResult.EMPTY -> Unit
                    }
                } else {
                    input = listOf(input.trim(), text)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
                }
            },
            onDismiss = { voiceMode = null },
        )
    }
}

@Composable
private fun PendingAttachmentChip(
    attachment: ChatAttachment,
    onRemove: () -> Unit,
) {
    if (attachment.kind == AttachmentKind.IMAGE) {
        Box(Modifier.size(62.dp)) {
            val bitmap = remember(attachment.dataUrl) { decodeAttachmentBitmap(attachment.dataUrl) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color(0xFFEAE8E4)),
                contentAlignment = Alignment.Center,
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = attachment.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = Muted,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(19.dp)
                    .clip(CircleShape)
                    .background(Color(0xCC11110F))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "移除图片",
                    tint = White,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    } else {
        Row(
            modifier = Modifier
                .height(46.dp)
                .widthIn(max = 200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(White)
                .border(1.dp, Line, RoundedCornerShape(12.dp))
                .padding(start = 10.dp, end = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                tint = Ink,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Column(Modifier.weight(1f, fill = false)) {
                Text(
                    text = attachment.name,
                    color = Ink,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatSize(attachment.sizeBytes),
                    color = Muted,
                    fontSize = 10.sp,
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "移除文件",
                    tint = Muted,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun ActionTile(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xFFEAE9E6))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Ink,
            modifier = Modifier.size(23.dp),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            color = Ink,
            fontSize = 12.5.sp,
        )
    }
}

private const val MAX_ATTACHMENTS = 6
