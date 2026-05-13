package com.ldreams.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = DeepNavy,
    primaryContainer = ElectricViolet,
    secondary = NeonCyan,
    onSecondary = DeepNavy,
    secondaryContainer = SoftCyan,
    tertiary = DreamBlue,
    background = DeepNavy,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    outlineVariant = CardBorder.copy(alpha = 0.3f),
    error = NightmareRed,
    onError = DeepNavy
)

@Composable
fun LDreamsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
