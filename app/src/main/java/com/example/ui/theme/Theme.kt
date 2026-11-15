package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val StudioDarkColorScheme = darkColorScheme(
    primary = NeonEmerald,
    onPrimary = StudioObsidian,
    primaryContainer = NeonEmeraldSoft,
    onPrimaryContainer = NeonEmerald,
    secondary = ElectricCyan,
    onSecondary = StudioObsidian,
    secondaryContainer = StudioElevated,
    onSecondaryContainer = TextCrisp,
    tertiary = AmberDrift,
    onTertiary = StudioObsidian,
    tertiaryContainer = AmberDriftSoft,
    onTertiaryContainer = AmberDrift,
    error = CrimsonSharp,
    onError = StudioObsidian,
    errorContainer = CrimsonSharpSoft,
    onErrorContainer = CrimsonSharp,
    background = StudioObsidian,
    onBackground = TextCrisp,
    surface = StudioConsole,
    onSurface = TextCrisp,
    surfaceVariant = StudioElevated,
    onSurfaceVariant = TextMuted,
    outline = StudioBorder,
    outlineVariant = StudioBorder.copy(alpha = 0.6f)
)

private val StudioShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AfinadorStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudioDarkColorScheme,
        typography = StudioTypography,
        shapes = StudioShapes,
        content = content
    )
}
