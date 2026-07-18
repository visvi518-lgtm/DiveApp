package com.diveapp.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = DivePrimary,
    secondary = DiveSecondary,
    background = DiveBackgroundLight,
    surface = DiveSurfaceLight,
    error = DiveError,
)

private val DarkColors = darkColorScheme(
    primary = DivePrimaryDark,
    secondary = DiveSecondary,
    background = DiveBackgroundDark,
    surface = DiveSurfaceDark,
    error = DiveError,
)

@Composable
fun DiveAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = DiveAppTypography,
        content = content,
    )
}
