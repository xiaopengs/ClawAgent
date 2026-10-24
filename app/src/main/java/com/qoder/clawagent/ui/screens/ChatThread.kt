package com.qoder.clawagent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qoder.clawagent.model.Agent
import com.qoder.clawagent.model.ChatMessage
import com.qoder.clawagent.ui.AgentAvatar
import com.qoder.clawagent.ui.theme.Accent
import com.qoder.clawagent.ui.theme.CardGray
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Line
import com.qoder.clawagent.ui.theme.Muted
import com.qoder.clawagent.ui.theme.White

@Composable
internal fun ChatThread(
    agent: Agent,
    messages: List<ChatMessage>,
    isTyping: Boolean,
    onProcessedClick: (ChatMessage) -> Unit,
    onLinkClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val lastTextLength = messages.lastOrNull()?.text?.length ?: 0
    LaunchedEffect(messages.size, isTyping, lastTextLength) {
        val target = messages.size + if (isTyping) 1 else 0
        if (target > 0) {
            if (isTyping || lastTextLength == 0) {
                listState.animateScrollToItem(target - 1)
            } else {
                listState.scrollToItem(target - 1)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 12.dp),
    ) {
        items(messages, key = { it.id }) { message ->
            if (message.fromUser) {
                UserMessage(message)
            } else {
                AgentMessage(
                    agent = agent,
                    message = message,
                    onProcessedClick = { onProcessedClick(message) },
                    onLinkClick = onLinkClick,
                    onRetry = onRetry,
                )
            }
        }
        if (isTyping) {
            item(key = "typing") {
                TypingIndicator(agent)
            }
        }
    }
}

@Composable
private fun AgentMessage(
    agent: Agent,
    message: ChatMessage,
    onProcessedClick: () -> Unit,
    onLinkClick: () -> Unit,
    onRetry: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AgentAvatar(
            style = agent.avatar,
            size = 43.dp,
            colorOverride = agent.avatarColor,
        )
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = agent.name,
                    color = Ink,
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(8.dp))
                Text(message.time, color = Muted, fontSize = 12.sp)
            }
            if (message.processed != null) {
                Spacer(Modifier.height(7.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onProcessedClick)
                        .padding(vertical = 1.dp, horizontal = 1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = message.processed,
                        color = Muted,
                        fontSize = 14.sp,
                    )
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = Muted,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
            Spacer(Modifier.height(11.dp))
            when {
                message.isError -> ErrorMessageCard(message.text, onRetry)
                message.title != null -> RichMessageCard(message)
                else -> PlainMessageCard(message.text)
            }
            if (message.linkTitle != null) {
                Spacer(Modifier.height(11.dp))
                TopicLinkCard(
                    title = message.linkTitle,
                    subtitle = message.linkSubtitle.orEmpty(),
                    onClick = onLinkClick,
                )
            }
        }
    }
}

@Composable
private fun RichMessageCard(message: ChatMessage) {
    var expanded by rememberSaveable(message.id) { mutableStateOf(false) }
    var canExpand by rememberSaveable(message.id) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardGray)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        if (message.title != null) {
            Text(
                text = message.title,
                color = Ink,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 24.sp,
            )
            Spacer(Modifier.height(11.dp))
        }
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = "·",
                color = Ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 1.dp, end = 7.dp),
            )
            Text(
                text = message.text,
                color = Ink.copy(alpha = 0.94f),
                fontSize = 16.5.sp,
                lineHeight = 26.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 8,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { result ->
                    if (result.hasVisualOverflow) canExpand = true
                },
            )
        }
        if (canExpand) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expanded = !expanded }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (expanded) "收起" else "显示更多",
                    color = Accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun PlainMessageCard(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardGray)
            .padding(horizontal = 17.dp, vertical = 14.dp),
    ) {
        Text(
            text = text,
            color = Ink.copy(alpha = 0.94f),
            fontSize = 16.sp,
            lineHeight = 25.sp,
        )
    }
}

@Composable
private fun ErrorMessageCard(text: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFCEEEA))
            .border(1.dp, Color(0xFFF0D3CB), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFB34A30),
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "请求未完成",
                color = Color(0xFF8C3522),
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = text,
            color = Ink.copy(alpha = 0.9f),
            fontSize = 14.5.sp,
            lineHeight = 22.sp,
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(White)
                .clickable(onClick = onRetry)
                .padding(horizontal = 14.dp, vertical = 7.dp),
        ) {
            Text("重试", color = Ink, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun UserMessage(message: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 6.dp))
                .background(Ink)
                .padding(horizontal = 15.dp, vertical = 11.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = message.text,
                color = White,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            )
        }
    }
}

@Composable
private fun TopicLinkCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .border(1.dp, Line.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEDF1FD)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.TrackChanges,
                contentDescription = null,
                tint = Color(0xFF5067E8),
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = Ink,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = subtitle,
                color = Muted,
                fontSize = 12.5.sp,
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = Muted,
            modifier = Modifier.size(19.dp),
        )
    }
}

@Composable
private fun TypingIndicator(agent: Agent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AgentAvatar(
            style = agent.avatar,
            size = 36.dp,
            colorOverride = agent.avatarColor,
        )
        Spacer(Modifier.width(11.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(CardGray)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(13.dp),
                strokeWidth = 1.5.dp,
                color = Muted,
            )
            Spacer(Modifier.width(8.dp))
            Text("正在处理…", color = Muted, fontSize = 13.5.sp)
        }
    }
}
