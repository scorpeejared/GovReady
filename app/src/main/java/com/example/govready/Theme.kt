package com.example.govready

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Palette from the approved Week 4 prototype. */
object GovColors {
    val Navy = Color(0xFF123A8B)
    val Background = Color(0xFFF5F7FB)
    val IconTint = Color(0xFFE1E8F7)
    val TextDark = Color(0xFF1A2233)
    val TextMuted = Color(0xFF5A6577)
    val Border = Color(0xFFE4E8F0)
    val White = Color.White
    val Green = Color(0xFF15803D)
    val GreenBg = Color(0xFFE4F5EA)
    val Red = Color(0xFFB3261E)
    val RedBg = Color(0xFFFDECEA)
    val Amber = Color(0xFF9A6B00)
    val AmberBg = Color(0xFFFBF0D8)
}

@Composable
fun GovReadyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = GovColors.Navy,
            background = GovColors.Background,
            surface = GovColors.White,
            onSurface = GovColors.TextDark
        ),
        content = content
    )
}
