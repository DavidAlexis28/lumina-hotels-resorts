package com.lumina.luminahotels.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = NavyDeep,
    primaryContainer = NavyLight,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF94A3B8),
    background = Color(0xFF0A1422),
    surface = Color(0xFF122033),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9)
)

private val LightColorScheme = lightColorScheme(
    primary = NavyDeep,
    onPrimary = Color.White,
    primaryContainer = GoldSoft,
    onPrimaryContainer = NavyDeep,
    secondary = GoldAccent,
    onSecondary = Color.White,
    background = IvoryBackground,
    surface = CardWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFEBE7DE),
    onSurfaceVariant = TextSecondary
)

@Composable
fun LuminahotelsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
