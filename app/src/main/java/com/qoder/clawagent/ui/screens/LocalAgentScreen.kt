package com.qoder.clawagent.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qoder.clawagent.AppViewModel
import com.qoder.clawagent.model.AppScreen
import com.qoder.clawagent.network.LocalProbeResult
import com.qoder.clawagent.ui.ScreenHeader
import com.qoder.clawagent.ui.theme.Bg
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Line
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.White
import kotlinx.coroutines.launch

@Composable
fun LocalAgentScreen(vm: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf("本地模型") }
    var endpoint by rememberSaveable { mutableStateOf("http://10.0.2.2:11434/v1") }
    var model by rememberSaveable { mutableStateOf("") }
    var apiKey by rememberSaveable { mutableStateOf("") }
    var probing by rememberSaveable { mutableStateOf(false) }
    var result by remember { mutableStateOf<LocalProbeResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
            .imePadding(),
    ) {
        ScreenHeader(title = "接入本地 Agent", onBack = { vm.goBack() })

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 4.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFEAF2E3))
                    .padding(15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(White.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Computer,
                        contentDescription = null,
                        tint = Ink,
                        modifier = Modifier.size(23.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "连接电脑上的模型服务",
                        color = Ink,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "支持 Ollama、LM Studio、vLLM 等 OpenAI 兼容 /v1 端点。",
                        color = Muted,
                        fontSize = 11.5.sp,
                        lineHeight = 17.sp,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            LocalLabel("Agent 名称")
            Spacer(Modifier.height(7.dp))
            LocalField(value = name, onValueChange = { name = it.take(24) }, placeholder = "例如：本地 Llama")

            Spacer(Modifier.height(15.dp))
            LocalLabel("Base URL")
            Spacer(Modifier.height(7.dp))
            LocalField(
                value = endpoint,
                onValueChange = { endpoint = it.take(180) },
                placeholder = "http://192.168.1.8:11434/v1",
                keyboardType = KeyboardType.Uri,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "模拟器访问宿主机可先用 10.0.2.2；真机请填写同一局域网内的电脑 IP。",
                color = Muted,
                fontSize = 11.sp,
                lineHeight = 16.sp,
            )

            Spacer(Modifier.height(15.dp))
            LocalLabel("模型 ID")
            Spacer(Modifier.height(7.dp))
            LocalField(
                value = model,
                onValueChange = { model = it.take(100) },
                placeholder = "llama3.2 / qwen2.5 / ...",
            )

            Spacer(Modifier.height(15.dp))
            LocalLabel("Local API Key（可选）")
            Spacer(Modifier.height(7.dp))
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it.take(200) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                textStyle = TextStyle(fontSize = 15.sp, color = Ink),
                placeholder = { Text("大多数本地服务可留空", color = Muted, fontSize = 14.sp) },
                shape = RoundedCornerShape(13.dp),
                colors = localFieldColors(),
            )

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEDEBE7))
                    .clickable(enabled = !probing && endpoint.isNotBlank()) {
                        probing = true
                        result = null
                        scope.launch {
                            val probe = vm.probeLocalEndpoint(endpoint, apiKey)
                            result = probe
                            if (probe.models.isNotEmpty() && model.isBlank()) {
                                model = probe.models.first()
                            }
                            probing = false
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                if (probing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 1.6.dp,
                        color = Ink,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("正在探测 /models…", color = Ink, fontSize = 13.5.sp)
                } else {
                    Text("测试连接", color = Ink, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                }
            }

            val probe = result
            if (probe != null) {
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (probe.error == null) Color(0xFFEAF6F0) else Color(0xFFFCEEE9))
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (probe.error == null) Icons.Outlined.CheckCircle else Icons.Outlined.CloudOff,
                            contentDescription = null,
                            tint = if (probe.error == null) Color(0xFF2E946C) else Color(0xFFB34A30),
                            modifier = Modifier.size(19.dp),
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = if (probe.error == null) {
                                "已连通，发现 ${probe.models.size} 个模型"
                            } else {
                                "连接未通过"
                            },
                            color = if (probe.error == null) Color(0xFF257A58) else Color(0xFF8C3522),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    if (probe.error != null) {
                        Spacer(Modifier.height(5.dp))
                        Text(probe.error, color = Muted, fontSize = 12.5.sp, lineHeight = 18.sp)
                    }
                    if (probe.models.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            probe.models.take(12).forEach { candidate ->
                                val selected = candidate == model
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(if (selected) Ink else White)
                                        .clickable { model = candidate }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text = candidate,
                                        color = if (selected) White else Ink,
                                        fontSize = 12.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "本地端点使用 HTTP 明文时，需要允许 App 的 cleartext 流量。API Key 仅保存在本机内存；正式使用建议为端点启用 HTTPS 与鉴权。",
                color = Muted,
                fontSize = 11.sp,
                lineHeight = 16.sp,
            )
            Spacer(Modifier.height(24.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            val enabled = name.isNotBlank() && model.isNotBlank() && endpoint.startsWith("http") && !probing
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(if (enabled) Ink else Color(0xFF96938F))
                    .clickable(enabled = enabled) {
                        val agent = vm.createLocalAgent(
                            name = name,
                            endpoint = endpoint,
                            model = model,
                            apiKey = apiKey,
                            workdir = name,
                        )
                        Toast.makeText(context, "已接入本地 Agent「${agent.name}」", Toast.LENGTH_SHORT).show()
                        vm.navigate(AppScreen.Chat(agent.id))
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text("创建本地 Agent", color = White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun LocalLabel(text: String) {
    Text(text = text, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun LocalField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = TextStyle(fontSize = 15.sp, color = Ink),
        placeholder = { Text(placeholder, color = Muted, fontSize = 14.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(13.dp),
        colors = localFieldColors(),
    )
}

@Composable
private fun localFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Ink,
    unfocusedBorderColor = Line,
    focusedContainerColor = White,
    unfocusedContainerColor = White,
    cursorColor = Ink,
)
