package com.qoder.clawagent.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.qoder.clawagent.chat.ReplyEngine
import com.qoder.clawagent.network.OpenRouterConfig
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Line
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.White

@Composable
internal fun OpenRouterSettingsDialog(
    initial: OpenRouterConfig,
    onSave: (apiKey: String, model: String, engine: ReplyEngine) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var model by remember { mutableStateOf(initial.model) }
    var apiKey by remember { mutableStateOf("") }
    var engine by remember { mutableStateOf(initial.engine) }
    val keyConfigured = initial.apiKey.isNotBlank()
    val canSave = model.isNotBlank() && (apiKey.isNotBlank() || keyConfigured)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(White)
                .padding(20.dp),
        ) {
            Text(
                text = "OpenRouter 设置",
                color = Ink,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "通过 OpenAI 兼容协议连接",
                color = Muted,
                fontSize = 12.5.sp,
            )
            Spacer(Modifier.height(14.dp))
            Text("对话引擎", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(7.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                EngineChoice(
                    label = "直连 OpenRouter",
                    selected = engine == ReplyEngine.OPENROUTER,
                    modifier = Modifier.weight(1f),
                ) { engine = ReplyEngine.OPENROUTER }
                Spacer(Modifier.width(8.dp))
                EngineChoice(
                    label = "ADK Agent",
                    selected = engine == ReplyEngine.ADK,
                    modifier = Modifier.weight(1f),
                ) { engine = ReplyEngine.ADK }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (engine == ReplyEngine.ADK) {
                    "Google ADK 运行时：会话、指令与工具编排；模型仍走 OpenRouter。"
                } else {
                    "轻量直连，流式输出，启动最快。"
                },
                color = Muted,
                fontSize = 11.5.sp,
                lineHeight = 16.sp,
            )
            Spacer(Modifier.height(16.dp))
            Text("模型 ID", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = model,
                onValueChange = { if (it.length <= 120) model = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.5.sp, color = Ink),
                placeholder = { Text("openrouter/free", color = Muted, fontSize = 14.sp) },
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors(),
            )
            Spacer(Modifier.height(13.dp))
            Text("API Key", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = apiKey,
                onValueChange = { if (it.length <= 220) apiKey = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                textStyle = TextStyle(fontSize = 14.5.sp, color = Ink),
                placeholder = {
                    Text(
                        text = if (keyConfigured) "已配置，留空保持当前 Key" else "sk-or-v1-...",
                        color = Muted,
                        fontSize = 14.sp,
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (keyConfigured) {
                    "当前 Key 已配置（仅保存在本机）"
                } else {
                    "尚未配置 Key"
                },
                color = Muted,
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Key 会写入本机 SharedPreferences。正式产品请改用后端代理，避免密钥进入客户端。",
                color = Muted,
                fontSize = 11.5.sp,
                lineHeight = 16.sp,
            )
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "取消",
                    color = Muted,
                    fontSize = 14.5.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                )
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (canSave) Ink else Color(0xFFB9B6B1))
                        .clickable(enabled = canSave) {
                            onSave(apiKey.ifBlank { initial.apiKey }, model, engine)
                            Toast.makeText(context, "OpenRouter 设置已保存", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                        .padding(horizontal = 22.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "保存",
                        color = White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun EngineChoice(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Ink else Color(0xFFF1EFEA))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) White else Ink,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Ink,
    unfocusedBorderColor = Line,
    focusedLabelColor = Ink,
    unfocusedLabelColor = Muted,
    cursorColor = Ink,
    focusedContainerColor = White,
    unfocusedContainerColor = White,
)
