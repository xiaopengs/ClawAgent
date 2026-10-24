package com.qoder.clawagent.ui.screens

import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.qoder.clawagent.model.Agent
import com.qoder.clawagent.model.AgentKind
import com.qoder.clawagent.model.AttachmentKind
import com.qoder.clawagent.model.ChatMessage
import com.qoder.clawagent.ui.theme.Bg
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.White
import android.content.Intent
import android.os.Bundle

enum class VoiceMode {
    INPUT,
    LISTEN,
}

@Composable
fun WorkspaceDialog(
    agent: Agent,
    modelLabel: String,
    engineLabel: String,
    onDismiss: () -> Unit,
    onOpenSettings: (() -> Unit)? = null,
) {
    val typeLabel = when (agent.kind) {
        AgentKind.COZE -> "扣子 Agent · 云端工作区"
        AgentKind.THIRD_PARTY -> "云电脑 Agent 预设"
        AgentKind.LOCAL -> "本地 Agent · 局域网端点"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = White,
        icon = {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEAEFF7)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Cloud,
                    contentDescription = null,
                    tint = Ink,
                    modifier = Modifier.size(23.dp),
                )
            }
        },
        title = {
            Text(
                text = if (agent.kind == AgentKind.LOCAL) "本地运行环境" else "云端工作区",
                color = Ink,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF35C28F)),
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = if (agent.kind == AgentKind.LOCAL) "端点已配置" else "运行中",
                        color = Color(0xFF257A58),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Spacer(Modifier.height(13.dp))
                WorkspaceRow("类型", typeLabel)
                WorkspaceRow("运行时", engineLabel)
                WorkspaceRow("模型", modelLabel)
                WorkspaceRow("工作目录", agent.workdir.ifBlank { "未设置" })
                WorkspaceRow(
                    "端点",
                    when {
                        agent.kind == AgentKind.LOCAL -> agent.localEndpoint.orEmpty().ifBlank { "未配置" }
                        agent.kind == AgentKind.THIRD_PARTY -> "云电脑 worker · ${agent.command.orEmpty()}"
                        else -> "OpenRouter / ADK 云端"
                    },
                )
                if (agent.extensions.isNotEmpty()) {
                    WorkspaceRow("扩展", agent.extensions.joinToString("、"))
                }
                if (agent.kind == AgentKind.THIRD_PARTY) {
                    Spacer(Modifier.height(11.dp))
                    Text(
                        text = "当前版本只保存云电脑预设与命令，不会启动真实 CLI 进程。",
                        color = Muted,
                        fontSize = 11.5.sp,
                        lineHeight = 17.sp,
                    )
                }
            }
        },
        confirmButton = {
            if (onOpenSettings != null) {
                TextButton(onClick = onOpenSettings) {
                    Text("模型设置", color = Ink, fontWeight = FontWeight.Medium)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭", color = Muted)
            }
        },
    )
}

@Composable
private fun WorkspaceRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(label, color = Muted, fontSize = 12.5.sp, modifier = Modifier.width(64.dp))
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            color = Ink,
            fontSize = 12.5.sp,
            lineHeight = 18.sp,
        )
    }
}

@Composable
fun ProcessedDialog(
    message: ChatMessage,
    modelLabel: String,
    engineLabel: String,
    onDismiss: () -> Unit,
) {
    val images = message.attachments.count { it.kind == AttachmentKind.IMAGE }
    val files = message.attachments.size - images
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = White,
        title = { Text("任务处理详情", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                WorkspaceRow("耗时", message.processed ?: "已处理 1秒")
                WorkspaceRow("时间", message.time.ifBlank { "刚刚" })
                WorkspaceRow("运行时", engineLabel)
                WorkspaceRow("模型", modelLabel)
                WorkspaceRow("输出", "${message.text.length} 字")
                if (message.attachments.isNotEmpty()) {
                    WorkspaceRow("附件", "图片 $images · 文件 $files")
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "耗时由本地会话状态机计时，用于展示本轮请求的处理节奏。",
                    color = Muted,
                    fontSize = 11.5.sp,
                    lineHeight = 17.sp,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("知道了", color = Ink, fontWeight = FontWeight.Medium)
            }
        },
    )
}

@Composable
fun VoiceCaptureDialog(
    agentName: String,
    mode: VoiceMode,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val recognizer = remember {
        runCatching { SpeechRecognizer.createSpeechRecognizer(context) }.getOrNull()
    }
    var transcript by remember { mutableStateOf("") }
    var partial by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(recognizer != null) }
    var restarts by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    val activeState by rememberUpdatedState(active)
    val transcriptState by rememberUpdatedState(transcript)
    val modeState by rememberUpdatedState(mode)
    val restartState by rememberUpdatedState { restarts += 1 }

    val intent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
    }

    val listener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                error = null
            }

            override fun onBeginningOfSpeech() {
                error = null
            }

            override fun onRmsChanged(rmsdB: Float) = Unit

            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() {
                partial = ""
            }

            override fun onError(errorCode: Int) {
                if (!activeState) return
                val friendly = when (errorCode) {
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "麦克风权限不足"
                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "语音服务网络不可用"
                    SpeechRecognizer.ERROR_AUDIO -> "录音出错，请检查麦克风"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别器正忙，请稍后重试"
                    else -> "没有听清，请再说一次"
                }
                if (modeState == VoiceMode.LISTEN &&
                    errorCode in setOf(
                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
                        SpeechRecognizer.ERROR_CLIENT,
                    )
                ) {
                    restartState()
                } else {
                    error = friendly
                    active = false
                }
            }

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                    .trim()
                partial = ""
                if (text.isBlank()) {
                    if (modeState == VoiceMode.LISTEN && activeState) {
                        restartState()
                    } else {
                        error = "没有听清，点击「重新收音」再试一次"
                        active = false
                    }
                    return
                }
                transcript = listOf(transcriptState, text)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                if (modeState == VoiceMode.LISTEN) {
                    restartState()
                } else {
                    active = false
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        }
    }

    DisposableEffect(recognizer) {
        recognizer?.setRecognitionListener(listener)
        onDispose {
            runCatching {
                recognizer?.cancel()
                recognizer?.destroy()
            }
        }
    }

    LaunchedEffect(restarts, active) {
        if (recognizer == null) return@LaunchedEffect
        if (active) {
            runCatching { recognizer.startListening(intent) }
                .onFailure {
                    error = "无法启动语音识别：${it.message.orEmpty().take(60)}"
                    active = false
                }
        } else {
            runCatching { recognizer.stopListening() }
        }
    }

    val pulse by rememberInfiniteTransition(label = "voice-pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(850), RepeatMode.Reverse),
        label = "voice-scale",
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = White,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (mode == VoiceMode.LISTEN) "旁听模式" else "语音输入",
                    color = Ink,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = agentName,
                    color = Muted,
                    fontSize = 12.5.sp,
                )
                Spacer(Modifier.height(22.dp))
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .scale(if (active) pulse else 1f)
                        .clip(CircleShape)
                        .background(if (active) Ink else Color(0xFFEAE8E4))
                        .then(
                            if (active) {
                                Modifier.border(6.dp, Color(0xFFDCD8D1), CircleShape)
                            } else {
                                Modifier
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = when {
                            !active -> Icons.Outlined.Stop
                            mode == VoiceMode.LISTEN -> Icons.Outlined.GraphicEq
                            else -> Icons.Outlined.Mic
                        },
                        contentDescription = null,
                        tint = if (active) White else Ink,
                        modifier = Modifier.size(34.dp),
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = when {
                        recognizer == null -> "当前设备不支持语音识别"
                        active && mode == VoiceMode.LISTEN -> "正在旁听，转写会持续累积"
                        active -> "正在听，请说话…"
                        transcript.isNotBlank() -> "识别完成"
                        else -> "已停止收音"
                    },
                    color = if (recognizer == null) Color(0xFFB34A30) else Muted,
                    fontSize = 12.5.sp,
                )
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(15.dp))
                        .background(Bg)
                        .padding(14.dp),
                ) {
                    Text(
                        text = when {
                            transcript.isNotBlank() -> transcript
                            partial.isNotBlank() -> partial
                            else -> "识别结果会显示在这里。"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (transcript.isNotBlank()) Ink else Muted,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                    )
                }
                if (partial.isNotBlank() && transcript.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = partial,
                        color = Muted.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                    )
                }
                val errorText = error
                if (errorText != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = errorText,
                        color = Color(0xFFB34A30),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = Muted, fontSize = 14.sp)
                    }
                    if (!active) {
                        TextButton(
                            onClick = {
                                error = null
                                active = true
                                restarts += 1
                            },
                            enabled = recognizer != null,
                        ) {
                            Text("重新收音", color = Ink, fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (transcript.isNotBlank()) Ink else Color(0xFFB7B3AD))
                            .clickable(enabled = transcript.isNotBlank()) {
                                if (mode == VoiceMode.LISTEN && active) active = false
                                onConfirm(transcript.trim())
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (mode == VoiceMode.LISTEN) "结束并发送" else "使用这段文字",
                            color = White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}
