package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = IntelCyan,
    onPrimary = Color(0xFF02162E),
    primaryContainer = Color(0xFF0C365C),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = IntelGold,
    onSecondary = Color(0xFF331E00),
    secondaryContainer = Color(0xFF5E3900),
    onSecondaryContainer = Color(0xFFFDE68A),
    tertiary = IntelEmerald,
    onTertiary = Color(0xFF003822),
    background = DeskDarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DeskDarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = DeskDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = DeskDarkBorder,
    error = IntelCrimson,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = IntelBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = IntelAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = IntelEmerald,
    onTertiary = Color.White,
    background = DeskLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = DeskLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = DeskLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = DeskLightBorder,
    error = IntelCrimson,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to executive dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
