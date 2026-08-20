package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Grey Background & Black Text Color Scheme
private val LightGreyColorScheme = lightColorScheme(
    primary = AppPrimary,
    onPrimary = Color.White,
    primaryContainer = AppSurfaceVariant,
    onPrimaryContainer = AppTextPrimary,
    secondary = AppSecondary,
    onSecondary = Color.White,
    secondaryContainer = AppSurfaceVariant,
    onSecondaryContainer = AppTextPrimary,
    tertiary = AppTertiary,
    onTertiary = Color.White,
    background = AppBackground,
    onBackground = AppTextPrimary,
    surface = AppSurface,
    onSurface = AppTextPrimary,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = AppTextSecondary,
    outline = AppBorder,
    outlineVariant = AppBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightGreyColorScheme,
        typography = Typography,
        content = content
    )
}

