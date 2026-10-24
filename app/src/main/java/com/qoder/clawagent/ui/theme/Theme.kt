package com.qoder.clawagent.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF141312)
val InkSoft = Color(0xFF2A2927)
val Muted = Color(0xFF9B9A96)
val MutedLight = Color(0xFFB8B6B1)
val Bg = Color(0xFFF4F3F1)
val BgDeep = Color(0xFFEBE9E6)
val CardGray = Color(0xFFEDECE9)
val White = Color(0xFFFFFFFF)
val Line = Color(0xFFE7E5E1)
val DividerSoft = Color(0xFFF0EEEA)
val Accent = Color(0xFF16B38A)
val AccentSoft = Color(0xFFE9F5F0)
val StarRed = Color(0xFFED5B50)
val Purple = Color(0xFF7D5CF0)
val BrandBlue = Color(0xFF4E63EA)
val WarmPink = Color(0xFFF3C3D2)
val WarmCream = Color(0xFFF0E7D7)

private val ClawColors = lightColorScheme(
    primary = Ink,
    onPrimary = White,
    secondary = Accent,
    onSecondary = White,
    background = Bg,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    surfaceVariant = BgDeep,
    onSurfaceVariant = Muted,
    outline = Line,
    outlineVariant = DividerSoft,
    error = StarRed,
)

@Composable
fun ClawTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ClawColors,
        typography = Typography(),
        content = content,
    )
}
