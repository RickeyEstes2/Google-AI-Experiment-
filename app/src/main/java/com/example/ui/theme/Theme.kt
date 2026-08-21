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

val DeepDarkBackground = Color(0xFF0F172A)
val CardSurfaceDark = Color(0xFF162032)
val CardElevatedDark = Color(0xFF1E293B)
val BorderDark = Color(0xFF2E3D52)
val TextMuted = Color(0xFF94A3B8)
val TextLight = Color(0xFFF1F5F9)

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Slate900,
    primaryContainer = CardElevatedDark,
    onPrimaryContainer = Color.White,
    secondary = SkyBlue500,
    onSecondary = Slate900,
    secondaryContainer = CardElevatedDark,
    onSecondaryContainer = SkyBlue100,
    tertiary = Emerald600,
    background = DeepDarkBackground,
    onBackground = TextLight,
    surface = CardSurfaceDark,
    onSurface = TextLight,
    surfaceVariant = CardElevatedDark,
    onSurfaceVariant = TextMuted,
    outline = BorderDark,
    outlineVariant = BorderDark
)

private val LightColorScheme = DarkColorScheme // Default to sleek Mastermind Dark style

@Composable
fun DatabaseMastermindTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DeepDarkBackground.toArgb()
                window.navigationBarColor = DeepDarkBackground.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
