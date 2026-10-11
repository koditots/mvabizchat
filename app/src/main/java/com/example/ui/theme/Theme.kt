package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkMVAColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = PurplePrimaryDark,
    onPrimaryContainer = PurpleNeonGlow,
    secondary = PurpleAccent,
    onSecondary = Color.Black,
    secondaryContainer = PurpleSurfaceVariant,
    onSecondaryContainer = PurpleNeonGlow,
    tertiary = PurpleE2EEGreen,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = PurpleSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = PurpleBorder,
    outlineVariant = Color(0xFF281347)
)

val LightMVAColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9D5FF),
    onPrimaryContainer = Color(0xFF581C87),
    secondary = PurplePrimaryDark,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = Color(0xFF3B0764),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = Color(0xFFDDD6FE),
    outlineVariant = Color(0xFFC4B5FD)
)

@Composable
fun MVABusinessChatTheme(
    darkTheme: Boolean = true, // Default to sleek purple & black dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkMVAColorScheme else LightMVAColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
