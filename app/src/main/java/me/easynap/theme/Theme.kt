package me.easynap.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = darkColorScheme(
    primary = TealPrimary,
    onPrimary = TealOnPrimary,
    background = DarkBackground,
    onBackground = LightOnBackground,
    surface = DarkSurface,
    onSurface = LightOnBackground,
    secondary = TealPrimary,
    onSecondary = TealOnPrimary,
)

@Composable
fun EasyNapTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColorScheme, typography = Typography, content = content)
}
