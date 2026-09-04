package com.example.solveflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Emerald400,
    onPrimary = Color(0xFF022C22),
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = Amber500,
    onSecondary = Color(0xFF451A03),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = Color(0xFFFDE68A),
    tertiary = Purple500,
    onTertiary = Color(0xFF3B0764),
    background = Zinc950,
    onBackground = Zinc50,
    surface = Zinc900,
    onSurface = Zinc50,
    surfaceVariant = Zinc800,
    onSurfaceVariant = Zinc400,
    outline = Zinc700,
    outlineVariant = Zinc800
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald600,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46),
    secondary = Amber600,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF92400E),
    tertiary = Purple600,
    onTertiary = Color.White,
    background = Color(0xFFF9FAFB),
    onBackground = Zinc950,
    surface = Color.White,
    onSurface = Zinc950,
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Zinc600,
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB)
)

@Composable
fun SolveFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
