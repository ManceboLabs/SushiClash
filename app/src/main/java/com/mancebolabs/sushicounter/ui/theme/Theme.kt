package com.mancebolabs.sushicounter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ItamaePrimary,
    onPrimary = ItamaeOnPrimary,
    primaryContainer = ItamaePrimaryContainer,
    onPrimaryContainer = ItamaeOnPrimaryContainer,
    inversePrimary = ItamaeInversePrimary,
    secondary = ItamaeSecondary,
    onSecondary = ItamaeOnSecondary,
    secondaryContainer = ItamaeSecondaryContainer,
    onSecondaryContainer = ItamaeOnSecondaryContainer,
    tertiary = ItamaeTertiary,
    onTertiary = ItamaeOnTertiary,
    tertiaryContainer = ItamaeTertiaryContainer,
    onTertiaryContainer = ItamaeOnTertiaryContainer,
    background = ItamaeBackground,
    onBackground = ItamaeOnBackground,
    surface = ItamaeSurface,
    onSurface = ItamaeOnSurface,
    surfaceVariant = ItamaeSurfaceVariant,
    onSurfaceVariant = ItamaeOnSurfaceVariant,
    surfaceContainerLowest = ItamaeSurfaceContainerLowest,
    surfaceContainerLow = ItamaeSurfaceContainerLow,
    surfaceContainer = ItamaeSurfaceContainer,
    surfaceContainerHigh = ItamaeSurfaceContainerHigh,
    surfaceContainerHighest = ItamaeSurfaceContainerHighest,
    outline = ItamaeOutline,
    outlineVariant = ItamaeOutlineVariant,
    error = ItamaeError,
    onError = ItamaeOnError,
    errorContainer = ItamaeErrorContainer,
    onErrorContainer = ItamaeOnErrorContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary = ItamaeInversePrimary,
    onPrimary = ItamaeOnPrimaryContainer,
    primaryContainer = ItamaePrimary,
    onPrimaryContainer = ItamaePrimaryFixed,
    secondary = ItamaeSecondaryContainer,
    onSecondary = ItamaeOnSecondaryContainer,
    secondaryContainer = ItamaeSecondary,
    onSecondaryContainer = ItamaeSecondaryFixed,
    tertiary = ItamaeTertiaryContainer,
    onTertiary = ItamaeOnTertiaryContainer,
    background = ItamaeDarkBackground,
    onBackground = ItamaeDarkOnBackground,
    surface = ItamaeDarkSurface,
    onSurface = ItamaeDarkOnSurface,
    surfaceVariant = ItamaeDarkSurfaceContainerHigh,
    onSurfaceVariant = ItamaeDarkOnSurfaceVariant,
    surfaceContainerLowest = ItamaeDarkBackground,
    surfaceContainerLow = ItamaeDarkSurface,
    surfaceContainer = ItamaeDarkSurfaceContainer,
    surfaceContainerHigh = ItamaeDarkSurfaceContainerHigh,
    surfaceContainerHighest = ItamaeDarkSurfaceContainerHigh,
    outline = ItamaeOutline,
    outlineVariant = ItamaeOutlineVariant,
    error = ItamaeError,
    onError = ItamaeOnError,
    errorContainer = ItamaeErrorContainer,
    onErrorContainer = ItamaeOnErrorContainer,
)

@Composable
fun SushiCounterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ItamaeShapes,
        content = content,
    )
}
