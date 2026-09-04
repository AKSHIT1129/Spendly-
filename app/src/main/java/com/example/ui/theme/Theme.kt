package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FintechAccent,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1F222C),
    onPrimaryContainer = FintechAccent,
    secondary = AmbientIndigoGlow,
    onSecondary = Color.White,
    background = RawSlateBg,
    onBackground = RawLightText,
    surface = RawSlateSurface,
    onSurface = RawLightText,
    surfaceVariant = RawSlateCard,
    onSurfaceVariant = RawGrayText,
    error = CoralRed,
    onError = Color.White,
    outline = RawSlateLine
)

private val LightColorScheme = lightColorScheme(
    primary = FintechAccent,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFFE2E8F0),
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = AmbientIndigoGlow,
    onSecondary = Color.White,
    background = RawSlateBg,
    onBackground = RawLightText,
    surface = RawSlateSurface,
    onSurface = RawLightText,
    surfaceVariant = RawSlateCard,
    onSurfaceVariant = RawGrayText,
    error = CoralRed,
    onError = Color.White,
    outline = RawSlateLine
)

@Composable
fun SpendlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Backward compatibility alias for existing usages and tests.
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) = SpendlyTheme(darkTheme = darkTheme, content = content)
