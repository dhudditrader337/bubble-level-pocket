package com.bubblelevel.pocket.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ToolColors = lightColorScheme(
    primary = ActionBlue, onPrimary = White, primaryContainer = VialGreen, onPrimaryContainer = Ink,
    secondary = Brass, onSecondary = Ink, secondaryContainer = VialGreen, onSecondaryContainer = Ink,
    tertiary = Brass, onTertiary = Ink, tertiaryContainer = VialGreen, onTertiaryContainer = Ink,
    background = Paper, onBackground = Ink, surface = White, onSurface = Ink,
    surfaceVariant = Paper, onSurfaceVariant = Ink.copy(alpha = 0.8f),
    outline = Brass.copy(alpha = 0.5f), error = ActionBlue, onError = White,
    errorContainer = Paper, onErrorContainer = Ink, surfaceTint = Brass,
    outlineVariant = Brass.copy(alpha = 0.25f), scrim = Ink,
    inverseSurface = Ink, inverseOnSurface = White, inversePrimary = VialGreen,
    surfaceBright = White, surfaceDim = Paper,
    surfaceContainerLowest = White, surfaceContainerLow = Paper, surfaceContainer = Paper,
    surfaceContainerHigh = White, surfaceContainerHighest = VialGreen
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ToolColors, typography = ToolTypography, content = content)
}
