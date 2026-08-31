package com.mancebolabs.sushiclash.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object FloatingNavBarMetrics {
    val borderWidth = 1.dp
    val itemSize = 56.dp
    val iconSize = 28.dp
    val indicatorSize = 48.dp
    val itemSpacing = 10.dp
    val containerHorizontalPadding = 12.dp
    val containerVerticalPadding = 6.dp
    const val indicatorAnimationMillis = 250

    fun indicatorOffsetForIndex(index: Int): Dp {
        return itemSize * index + itemSpacing * index + (itemSize - indicatorSize) / 2
    }
}

@Composable
fun rememberItamaeNigiriBorderBrush(): Brush {
    val isDark = rememberItamaeIsDarkTheme()
    return if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                ItamaePrimaryContainer.copy(alpha = 0.7f),
                ItamaeInversePrimary.copy(alpha = 0.5f),
                ItamaeOutlineVariant.copy(alpha = 0.4f),
            ),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                ItamaePrimaryContainer.copy(alpha = 0.88f),
                ItamaePrimaryFixed.copy(alpha = 0.82f),
                ItamaeSurfaceContainerLowest.copy(alpha = 0.92f),
            ),
        )
    }
}

@Composable
fun itamaeNavInactiveIconColor(): Color {
    return if (rememberItamaeIsDarkTheme()) {
        ItamaeDarkOnSurfaceVariant.copy(alpha = 0.72f)
    } else {
        ItamaeOutline.copy(alpha = 0.88f)
    }
}

@Composable
fun itamaeNavSelectedIndicatorColor(): Color {
    return if (rememberItamaeIsDarkTheme()) {
        ItamaePrimary.copy(alpha = 0.3f)
    } else {
        ItamaePrimaryContainer.copy(alpha = 0.34f)
    }
}

@Composable
fun Modifier.itamaeFloatingNavBarShadow(
    elevation: Dp = 4.dp,
    shape: androidx.compose.ui.graphics.Shape = ItamaeShapes.extraLarge,
): Modifier {
    val isDark = rememberItamaeIsDarkTheme()
    return shadow(
        elevation = if (isDark) 3.dp else elevation,
        shape = shape,
        spotColor = ItamaePrimary.copy(alpha = if (isDark) 0.16f else 0.1f),
        ambientColor = Color.Black.copy(alpha = if (isDark) 0.22f else 0.07f),
    )
}

@Composable
fun Modifier.itamaeNavSelectedIndicatorShadow(): Modifier {
    val isDark = rememberItamaeIsDarkTheme()
    return shadow(
        elevation = 2.dp,
        shape = CircleShape,
        spotColor = ItamaePrimary.copy(alpha = if (isDark) 0.2f else 0.14f),
        ambientColor = ItamaePrimaryContainer.copy(alpha = if (isDark) 0.12f else 0.08f),
    )
}

@Composable
fun itamaeFloatingNavBarSurfaceColor(): Color {
    val scheme = MaterialTheme.colorScheme
    return scheme.surfaceContainerLowest.copy(alpha = if (rememberItamaeIsDarkTheme()) 0.96f else 0.95f)
}
