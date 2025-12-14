package com.example.mobile_final.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

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
fun Mobile_finalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),

    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}