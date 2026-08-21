package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Slate900,
    onPrimary = Color.White,
    primaryContainer = Slate100,
    onPrimaryContainer = Slate900,
    secondary = SkyBlue600,
    onSecondary = Color.White,
    secondaryContainer = SkyBlue100,
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = Emerald600,
    background = Color(0xFFF3F4F6),
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = Slate600,
    outline = Slate400,
    outlineVariant = Slate200
)

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Slate900,
    primaryContainer = Slate800,
    onPrimaryContainer = Color.White,
    secondary = SkyBlue500,
    onSecondary = Slate900,
    secondaryContainer = Slate800,
    onSecondaryContainer = SkyBlue100,
    tertiary = Emerald600,
    background = Color(0xFF0B0F17),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF131B2A),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Slate600,
    outlineVariant = Slate700
)

@Composable
fun DatabaseMastermindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
