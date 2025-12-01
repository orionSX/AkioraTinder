package com.example.akioratinder.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color(0xFFFFFFFF),
    secondary = PrimaryDark,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = PrimaryDark,
    background = BackgroundLight,
    surface = BackgroundLight,
    onSurface = Color(0xFF000000),
    onBackground = Color(0xFF000000)
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color(0xFFFFFFFF),
    secondary = Primary,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = Color(0xFFE8E0FF),
    background = BackgroundDark,
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFFFFFFF),
    onBackground = Color(0xFFFFFFFF)
)

@Composable
fun AkioraTinderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}
