package com.qoder.clawagent.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qoder.clawagent.model.AvatarStyle
import com.qoder.clawagent.model.McpIconKind
import com.qoder.clawagent.ui.theme.Ink
import com.qoder.clawagent.ui.theme.Line
import com.qoder.clawagent.ui.theme.White

@Composable
fun AgentAvatar(
    style: AvatarStyle,
    size: Dp,
    colorOverride: Long? = null,
) {
    val baseColor = colorOverride?.let { Color(it) } ?: defaultAvatarColor(style)
    val brush: Brush = if (style == AvatarStyle.ROBOT && colorOverride == null) {
        Brush.verticalGradient(listOf(Color(0xFFF5A0B5), Color(0xFFEE728C)))
    } else {
        SolidColor(baseColor)
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(brush),
        contentAlignment = Alignment.Center,
    ) {
        when (style) {
            AvatarStyle.CAT -> CatAvatar()
            AvatarStyle.TERMINAL -> Text(
                text = ">_",
                color = Ink,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.26f).sp,
            )

            AvatarStyle.SUNGLASSES -> SunglassesFace()
            AvatarStyle.ROBOT -> RobotFace()
            AvatarStyle.FACE -> PlainFace()
            AvatarStyle.SPARK -> SparkFace()
            AvatarStyle.GLASSES -> GlassesFace()
        }
    }
}

private fun defaultAvatarColor(style: AvatarStyle): Color = when (style) {
    AvatarStyle.CAT -> Color(0xFF1E1C1B)
    AvatarStyle.TERMINAL -> Color(0xFFCBDCC5)
    AvatarStyle.SUNGLASSES -> Color(0xFFC3B0F2)
    AvatarStyle.ROBOT -> Color(0xFFF08AA4)
    AvatarStyle.FACE -> Color(0xFFD8D8E2)
    AvatarStyle.SPARK -> Color(0xFFE3DCCB)
    AvatarStyle.GLASSES -> Color(0xFFD8DDEC)
}

@Composable
private fun CatAvatar() {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // lilac hoodie
        val hoodie = Path().apply {
            moveTo(w * 0.14f, h * 0.74f)
            quadraticBezierTo(w * 0.50f, h * 0.60f, w * 0.86f, h * 0.74f)
            lineTo(w * 0.92f, h * 1.02f)
            lineTo(w * 0.08f, h * 1.02f)
            close()
        }
        drawPath(hoodie, Color(0xFF8E7BDD))

        // head + ears
        val head = Path().apply {
            moveTo(w * 0.33f, h * 0.57f)
            cubicTo(w * 0.27f, h * 0.48f, w * 0.28f, h * 0.35f, w * 0.35f, h * 0.30f)
            lineTo(w * 0.37f, h * 0.19f)
            lineTo(w * 0.46f, h * 0.27f)
            cubicTo(w * 0.49f, h * 0.26f, w * 0.51f, h * 0.26f, w * 0.54f, h * 0.27f)
            lineTo(w * 0.63f, h * 0.19f)
            lineTo(w * 0.65f, h * 0.30f)
            cubicTo(w * 0.72f, h * 0.35f, w * 0.73f, h * 0.48f, w * 0.67f, h * 0.57f)
            cubicTo(w * 0.62f, h * 0.65f, w * 0.38f, h * 0.65f, w * 0.33f, h * 0.57f)
            close()
        }
        drawPath(head, Color(0xFFFFF8EE))

        // inner ears
        drawPath(
            Path().apply {
                moveTo(w * 0.39f, h * 0.27f)
                lineTo(w * 0.38f, h * 0.22f)
                lineTo(w * 0.43f, h * 0.27f)
                close()
            },
            Color(0xFFE7B7C1),
        )
        drawPath(
            Path().apply {
                moveTo(w * 0.61f, h * 0.27f)
                lineTo(w * 0.62f, h * 0.22f)
                lineTo(w * 0.57f, h * 0.27f)
                close()
            },
            Color(0xFFE7B7C1),
        )

        // closed happy eyes
        drawArc(
            color = Ink,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(w * 0.38f, h * 0.42f),
            size = Size(w * 0.09f, h * 0.06f),
            style = Stroke(width = w * 0.018f, cap = StrokeCap.Round),
        )
        drawArc(
            color = Ink,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(w * 0.53f, h * 0.42f),
            size = Size(w * 0.09f, h * 0.06f),
            style = Stroke(width = w * 0.018f, cap = StrokeCap.Round),
        )

        // cheeks + nose + mouth
        drawCircle(Color(0xFFF0A7B7), w * 0.032f, Offset(w * 0.33f, h * 0.52f))
        drawCircle(Color(0xFFF0A7B7), w * 0.032f, Offset(w * 0.67f, h * 0.52f))
        drawPath(
            Path().apply {
                moveTo(w * 0.49f, h * 0.51f)
                lineTo(w * 0.51f, h * 0.51f)
                lineTo(w * 0.50f, h * 0.545f)
                close()
            },
            Color(0xFFD97E92),
        )
        drawPath(
            Path().apply {
                moveTo(w * 0.47f, h * 0.57f)
                quadraticBezierTo(w * 0.50f, h * 0.60f, w * 0.53f, h * 0.57f)
            },
            Ink,
            style = Stroke(width = w * 0.013f, cap = StrokeCap.Round),
        )

        // paw
        drawRoundRect(
            color = Color(0xFFFFF8EE),
            topLeft = Offset(w * 0.43f, h * 0.86f),
            size = Size(w * 0.14f, h * 0.10f),
            cornerRadius = CornerRadius(w * 0.05f, h * 0.05f),
        )
    }
}

@Composable
private fun SunglassesFace() {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // bucket hat
        drawArc(
            color = Color(0xFFDCE7F6),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.31f, h * 0.09f),
            size = Size(w * 0.38f, h * 0.30f),
            style = Stroke(width = w * 0.09f, cap = StrokeCap.Round),
        )
        drawRoundRect(
            color = Color(0xFFDCE7F6),
            topLeft = Offset(w * 0.25f, h * 0.26f),
            size = Size(w * 0.50f, h * 0.045f),
            cornerRadius = CornerRadius(w * 0.03f, h * 0.03f),
        )

        // sunglasses
        drawRoundRect(
            color = Color(0xFF171615),
            topLeft = Offset(w * 0.26f, h * 0.40f),
            size = Size(w * 0.21f, h * 0.105f),
            cornerRadius = CornerRadius(w * 0.035f, h * 0.035f),
        )
        drawRoundRect(
            color = Color(0xFF171615),
            topLeft = Offset(w * 0.53f, h * 0.40f),
            size = Size(w * 0.21f, h * 0.105f),
            cornerRadius = CornerRadius(w * 0.035f, h * 0.035f),
        )
        drawLine(
            color = Color(0xFF171615),
            start = Offset(w * 0.47f, h * 0.435f),
            end = Offset(w * 0.53f, h * 0.435f),
            strokeWidth = w * 0.018f,
        )
        drawCircle(Color.White.copy(alpha = 0.55f), w * 0.015f, Offset(w * 0.30f, h * 0.425f))
        drawCircle(Color.White.copy(alpha = 0.55f), w * 0.015f, Offset(w * 0.57f, h * 0.425f))

        // smile
        drawArc(
            color = Color(0xFFFFF7EC),
            startAngle = 10f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(w * 0.43f, h * 0.60f),
            size = Size(w * 0.14f, h * 0.075f),
            style = Stroke(width = w * 0.02f, cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun RobotFace() {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            color = Color(0xFF1A1918),
            topLeft = Offset(w * 0.32f, h * 0.36f),
            size = Size(w * 0.14f, h * 0.25f),
            cornerRadius = CornerRadius(w * 0.07f, h * 0.07f),
        )
        drawRoundRect(
            color = Color(0xFF1A1918),
            topLeft = Offset(w * 0.54f, h * 0.36f),
            size = Size(w * 0.14f, h * 0.25f),
            cornerRadius = CornerRadius(w * 0.07f, h * 0.07f),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.65f),
            topLeft = Offset(w * 0.355f, h * 0.40f),
            size = Size(w * 0.035f, h * 0.075f),
            cornerRadius = CornerRadius(w * 0.02f, h * 0.02f),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.65f),
            topLeft = Offset(w * 0.575f, h * 0.40f),
            size = Size(w * 0.035f, h * 0.075f),
            cornerRadius = CornerRadius(w * 0.02f, h * 0.02f),
        )
    }
}

@Composable
private fun PlainFace() {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawCircle(Color(0xFF2B2A2E), w * 0.025f, Offset(w * 0.41f, h * 0.45f))
        drawCircle(Color(0xFF2B2A2E), w * 0.025f, Offset(w * 0.59f, h * 0.45f))
        drawRoundRect(
            color = Color(0xFF52515A),
            topLeft = Offset(w * 0.42f, h * 0.58f),
            size = Size(w * 0.16f, h * 0.022f),
            cornerRadius = CornerRadius(w * 0.02f, h * 0.02f),
        )
    }
}

@Composable
private fun SparkFace() {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val outer = w * 0.27f
        val inner = outer * 0.34f
        val star = Path().apply {
            moveTo(cx, cy - outer)
            lineTo(cx + inner, cy - inner)
            lineTo(cx + outer, cy)
            lineTo(cx + inner, cy + inner)
            lineTo(cx, cy + outer)
            lineTo(cx - inner, cy + inner)
            lineTo(cx - outer, cy)
            lineTo(cx - inner, cy - inner)
            close()
        }
        drawPath(star, Color(0xFF4E4A3F).copy(alpha = 0.78f))
        drawCircle(Color(0xFF4E4A3F).copy(alpha = 0.45f), w * 0.025f, Offset(w * 0.72f, h * 0.27f))
        drawCircle(Color(0xFF4E4A3F).copy(alpha = 0.35f), w * 0.018f, Offset(w * 0.25f, h * 0.70f))
    }
}

@Composable
private fun GlassesFace() {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawCircle(
            color = Ink.copy(alpha = 0.82f),
            radius = w * 0.10f,
            center = Offset(w * 0.40f, h * 0.48f),
            style = Stroke(width = w * 0.02f),
        )
        drawCircle(
            color = Ink.copy(alpha = 0.82f),
            radius = w * 0.10f,
            center = Offset(w * 0.60f, h * 0.48f),
            style = Stroke(width = w * 0.02f),
        )
        drawLine(
            color = Ink.copy(alpha = 0.82f),
            start = Offset(w * 0.49f, h * 0.47f),
            end = Offset(w * 0.51f, h * 0.47f),
            strokeWidth = w * 0.018f,
        )
        drawCircle(Ink, w * 0.015f, Offset(w * 0.40f, h * 0.48f))
        drawCircle(Ink, w * 0.015f, Offset(w * 0.60f, h * 0.48f))
        drawArc(
            color = Ink.copy(alpha = 0.7f),
            startAngle = 15f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = Offset(w * 0.44f, h * 0.61f),
            size = Size(w * 0.12f, h * 0.06f),
            style = Stroke(width = w * 0.016f, cap = StrokeCap.Round),
        )
    }
}

@Composable
fun ProfileMiniAvatar(size: Dp = 26.dp) {
    Canvas(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFDCE8F6)),
    ) {
        val w = this.size.width
        val h = this.size.height
        drawCircle(Color.White.copy(alpha = 0.8f), w * 0.30f, Offset(w * 0.38f, h * 0.55f))
        drawCircle(Color(0xFFA8C6EC), w * 0.22f, Offset(w * 0.62f, h * 0.40f))
        drawCircle(Color(0xFF7FA7DC), w * 0.07f, Offset(w * 0.66f, h * 0.36f))
    }
}

@Composable
fun EmojiTile(
    icon: String,
    bgColor: Long,
    size: Dp = 46.dp,
    radius: Dp = 14.dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(radius))
            .background(Color(bgColor)),
        contentAlignment = Alignment.Center,
    ) {
        TileContent(icon, size)
    }
}

@Composable
fun RoundEmojiAvatar(
    icon: String,
    bgColor: Long,
    size: Dp = 48.dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(bgColor)),
        contentAlignment = Alignment.Center,
    ) {
        TileContent(icon, size)
    }
}

@Composable
private fun TileContent(icon: String, size: Dp) {
    val isText = icon == "10w+" || icon == ">_" || icon == "</>"
    if (isText) {
        Text(
            text = icon,
            color = Ink,
            fontFamily = if (icon == "10w+") FontFamily.Default else FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = if (icon == "10w+") (size.value * 0.27f).sp else (size.value * 0.28f).sp,
        )
    } else {
        Text(
            text = icon,
            fontSize = (size.value * 0.47f).sp,
        )
    }
}

@Composable
fun ProjectIcon(isWebsite: Boolean, size: Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFEFE6D5)),
        contentAlignment = Alignment.Center,
    ) {
        if (isWebsite) {
            Text(
                text = "</>",
                color = Ink,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.28f).sp,
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                tint = Ink,
                modifier = Modifier.size(size * 0.46f),
            )
        }
    }
}

@Composable
fun McpIcon(kind: McpIconKind, size: Dp) {
    val shape = RoundedCornerShape(size * 0.24f)
    val background = when (kind) {
        McpIconKind.QCC -> Color(0xFF3478F6)
        McpIconKind.TYC -> Color(0xFF2F7BF6)
        McpIconKind.AGENTKEY -> White
        McpIconKind.NOTION -> White
        McpIconKind.GITHUB -> White
        McpIconKind.IFIND -> White
        McpIconKind.OCTOPUS -> Color(0xFF1E6BFA)
    }
    val needsLine = kind == McpIconKind.AGENTKEY ||
        kind == McpIconKind.NOTION ||
        kind == McpIconKind.GITHUB ||
        kind == McpIconKind.IFIND

    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(background)
            .then(if (needsLine) Modifier.border(1.dp, Line, shape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        when (kind) {
            McpIconKind.QCC -> Canvas(Modifier.fillMaxSize()) { drawQcc() }
            McpIconKind.TYC -> Canvas(Modifier.fillMaxSize()) { drawTyc() }
            McpIconKind.AGENTKEY -> Canvas(Modifier.fillMaxSize()) { drawAgentKey() }
            McpIconKind.GITHUB -> Canvas(Modifier.fillMaxSize()) { drawGitHub() }
            McpIconKind.IFIND -> Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFFE23B3B))) { append("i") }
                    withStyle(SpanStyle(color = Color(0xFF2468D8))) { append("Fin") }
                    withStyle(SpanStyle(color = Color(0xFF222222))) { append("D") }
                    withStyle(SpanStyle(color = Color(0xFFF0A31A), fontSize = (size.value * 0.14f).sp)) {
                        append("●")
                    }
                },
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.23f).sp,
                maxLines = 1,
            )

            McpIconKind.NOTION -> Box(
                modifier = Modifier
                    .size(size * 0.62f)
                    .border(1.5.dp, Ink, RoundedCornerShape(size * 0.06f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "N",
                    color = Ink,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Black,
                    fontSize = (size.value * 0.42f).sp,
                )
            }

            McpIconKind.OCTOPUS -> Canvas(Modifier.fillMaxSize()) { drawOctopus() }
        }
    }
}

private fun DrawScope.drawQcc() {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = w * 0.06f, cap = StrokeCap.Round)
    drawCircle(White, w * 0.20f, Offset(w * 0.46f, h * 0.44f), style = stroke)
    drawCircle(White, w * 0.055f, Offset(w * 0.46f, h * 0.44f))
    drawLine(White, Offset(w * 0.60f, h * 0.58f), Offset(w * 0.74f, h * 0.72f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
}

private fun DrawScope.drawTyc() {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = w * 0.07f, cap = StrokeCap.Round)
    drawArc(
        color = White,
        startAngle = 195f,
        sweepAngle = 230f,
        useCenter = false,
        topLeft = Offset(w * 0.24f, w * 0.24f),
        size = Size(w * 0.52f, w * 0.52f),
        style = stroke,
    )
    drawArc(
        color = White,
        startAngle = 100f,
        sweepAngle = 95f,
        useCenter = false,
        topLeft = Offset(w * 0.38f, w * 0.38f),
        size = Size(w * 0.24f, w * 0.24f),
        style = stroke,
    )
    drawCircle(White, w * 0.045f, Offset(w * 0.38f, h * 0.62f))
}

private fun DrawScope.drawAgentKey() {
    val w = size.width
    val cx = w * 0.5f
    val cy = size.height * 0.5f
    val len = w * 0.25f
    val angles = listOf(0.0, 60.0, 120.0)
    angles.forEach { angle ->
        val rad = Math.toRadians(angle)
        val dx = (Math.cos(rad) * len).toFloat()
        val dy = (Math.sin(rad) * len).toFloat()
        drawLine(
            color = Ink,
            start = Offset(cx - dx, cy - dy),
            end = Offset(cx + dx, cy + dy),
            strokeWidth = w * 0.065f,
            cap = StrokeCap.Square,
        )
    }
    drawCircle(Ink, w * 0.055f, Offset(cx, cy))
}

private fun DrawScope.drawGitHub() {
    val w = size.width
    val h = size.height
    val head = Path().apply {
        moveTo(w * 0.32f, h * 0.45f)
        cubicTo(w * 0.28f, h * 0.28f, w * 0.40f, h * 0.25f, w * 0.43f, h * 0.28f)
        lineTo(w * 0.36f, h * 0.16f)
        lineTo(w * 0.49f, h * 0.25f)
        cubicTo(w * 0.50f, h * 0.25f, w * 0.50f, h * 0.25f, w * 0.51f, h * 0.25f)
        lineTo(w * 0.64f, h * 0.16f)
        lineTo(w * 0.57f, h * 0.28f)
        cubicTo(w * 0.60f, h * 0.25f, w * 0.72f, h * 0.28f, w * 0.68f, h * 0.45f)
        cubicTo(w * 0.68f, h * 0.55f, w * 0.62f, h * 0.58f, w * 0.54f, h * 0.59f)
        lineTo(w * 0.55f, h * 0.70f)
        lineTo(w * 0.45f, h * 0.70f)
        lineTo(w * 0.46f, h * 0.59f)
        cubicTo(w * 0.38f, h * 0.58f, w * 0.32f, h * 0.55f, w * 0.32f, h * 0.45f)
        close()
    }
    drawPath(head, Ink)
    drawCircle(White, w * 0.025f, Offset(w * 0.42f, h * 0.42f))
    drawCircle(White, w * 0.025f, Offset(w * 0.58f, h * 0.42f))
}

private fun DrawScope.drawOctopus() {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = w * 0.055f, cap = StrokeCap.Round)
    drawCircle(White, w * 0.16f, Offset(w * 0.5f, h * 0.40f), style = stroke)
    drawCircle(White, w * 0.025f, Offset(w * 0.45f, h * 0.38f))
    drawCircle(White, w * 0.025f, Offset(w * 0.55f, h * 0.38f))
    for (i in 0..3) {
        val x = w * (0.38f + i * 0.08f)
        val path = Path().apply {
            moveTo(x, h * 0.54f)
            cubicTo(x - w * 0.04f, h * 0.63f, x + w * 0.05f, h * 0.69f, x - w * 0.01f, h * 0.79f)
        }
        drawPath(path, White, style = stroke)
    }
}
