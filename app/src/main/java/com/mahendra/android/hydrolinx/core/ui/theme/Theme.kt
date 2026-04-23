package com.mahendra.android.hydrolinx.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = HydrolinxCyan,
    onPrimary = Color.White,
    secondary = HydrolinxGreen,
    onSecondary = Color.White,
    tertiary = HydrolinxPink,
    onTertiary = Color.White,
    background = HydrolinxBackground,
    onBackground = HydrolinxOnSurface,
    surface = HydrolinxSurface,
    onSurface = HydrolinxOnSurface,
    surfaceVariant = HydrolinxBackground,
    onSurfaceVariant = HydrolinxOnSurfaceMuted,
)

@Composable
fun HydrolinxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = HydrolinxTypography,
        content = content,
    )
}
