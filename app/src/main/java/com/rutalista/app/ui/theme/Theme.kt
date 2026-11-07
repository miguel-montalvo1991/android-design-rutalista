package com.rutalista.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LightBlue,
    onPrimary = PrimaryNavy,
    primaryContainer = PrimaryBlue,
    onPrimaryContainer = Color.White,
    secondary = AccentYellow,
    onSecondary = PrimaryNavy,
    tertiary = SoftYellow,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryNavy,
    onPrimary = Color.White,
    primaryContainer = SoftBlueBg,
    onPrimaryContainer = PrimaryNavy,
    secondary = AccentBlue,
    onSecondary = Color.White,
    tertiary = AccentYellow,
    onTertiary = PrimaryNavy,
    background = SoftBlueBg,
    surface = SurfaceLight,
    onBackground = TextDark,
    onSurface = TextDark,
    surfaceVariant = SurfaceVariant
)

@Composable
fun RutaListaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
