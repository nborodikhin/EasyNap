package me.easynap.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val AppColorScheme = darkColorScheme(
    primary = CalmTealPrimary,
    onPrimary = CalmTealOnPrimary,
    primaryContainer = CalmTealPrimaryContainer,
    onPrimaryContainer = CalmTealOnPrimaryContainer,
    secondaryContainer = CalmTealSecondaryContainer,
    onSecondaryContainer = CalmTealOnSecondaryContainer,
    background = CalmTealBackground,
    surface = CalmTealSurface,
    surfaceContainer = CalmTealSurfaceContainer,
    surfaceContainerHigh = CalmTealSurfaceContainerHigh,
    onSurface = CalmTealOnSurface,
    onSurfaceVariant = CalmTealOnSurfaceVariant,
    outline = CalmTealOutline,
    outlineVariant = CalmTealOutlineVariant,
    error = CalmTealError,
    onError = CalmTealOnError,
    errorContainer = CalmTealErrorContainer,
    onErrorContainer = CalmTealOnErrorContainer,
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(22.dp),
)

@Composable
fun EasyNapTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
