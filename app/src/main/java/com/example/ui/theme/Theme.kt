package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VoxDarkColorScheme = darkColorScheme(
    primary = VoxBlue,
    onPrimary = Color.White,
    primaryContainer = VoxBlueDark,
    onPrimaryContainer = Color.White,
    secondary = VoxCyan,
    onSecondary = Color.Black,
    secondaryContainer = VoxSurfaceVariant,
    onSecondaryContainer = VoxTextPrimary,
    tertiary = VoxOrangeAccent,
    onTertiary = Color.Black,
    background = VoxBackground,
    onBackground = VoxTextPrimary,
    surface = VoxSurface,
    onSurface = VoxTextPrimary,
    surfaceVariant = VoxSurfaceVariant,
    onSurfaceVariant = VoxTextSecondary,
    error = VoxRedMic,
    onError = Color.White,
    outline = VoxBorder,
    outlineVariant = VoxSurfaceHover
)

@Composable
fun VoxFitTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VoxDarkColorScheme,
        typography = Typography,
        content = content
    )
}
