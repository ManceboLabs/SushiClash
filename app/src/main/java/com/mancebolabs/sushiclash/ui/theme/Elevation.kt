package com.mancebolabs.sushiclash.ui.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun rememberItamaeIsDarkTheme(): Boolean {
    return MaterialTheme.colorScheme.background.luminance() < 0.5f
}

@Composable
fun itamaeWasabiAccent(alpha: Float = 0.85f): Color {
    val colorScheme = MaterialTheme.colorScheme
    val wasabi = if (colorScheme.background.luminance() > 0.5f) {
        colorScheme.secondaryContainer
    } else {
        colorScheme.secondary
    }
    return wasabi.copy(alpha = alpha)
}

@Composable
fun Modifier.itamaeCardShadow(
    elevation: Dp = 1.dp,
    shape: Shape = ItamaeShapes.large,
): Modifier = shadow(
    elevation = elevation,
    shape = shape,
    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
)

@Composable
fun Modifier.itamaeInteractionShadow(
    elevation: Dp = 3.dp,
    shape: Shape = ItamaePillShape,
): Modifier = shadow(
    elevation = elevation,
    shape = shape,
    spotColor = ItamaePrimaryContainer.copy(alpha = 0.08f),
    ambientColor = ItamaePrimaryContainer.copy(alpha = 0.04f),
)

@Composable
fun Modifier.itamaePressedShadow(
    elevation: Dp = 1.dp,
    shape: Shape = ItamaeShapes.large,
): Modifier {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return shadow(
        elevation = elevation,
        shape = shape,
        spotColor = if (isDark) {
            itamaeWasabiAccent(alpha = 0.18f)
        } else {
            Color.Black.copy(alpha = 0.06f)
        },
        ambientColor = if (isDark) {
            ItamaePrimaryContainer.copy(alpha = 0.12f)
        } else {
            Color.Black.copy(alpha = 0.03f)
        },
    )
}

@Composable
fun sushiButtonContainerColor(isPressed: Boolean): Color {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background.luminance() < 0.5f
    return when {
        !isDark && isPressed -> colorScheme.surfaceContainerHigh
        !isDark -> colorScheme.surfaceContainerLowest
        isPressed -> ItamaeSushiButtonDarkContainerPressed
        else -> ItamaeSushiButtonDarkContainer
    }
}

@Composable
fun Modifier.sushiButtonBorder(isPressed: Boolean = false): Modifier {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    if (!isDark) return this

    val borderColor = if (isPressed) {
        itamaeWasabiAccent(alpha = 0.55f)
    } else {
        itamaeWasabiAccent(alpha = 0.8f)
    }
    return border(
        width = 2.dp,
        color = borderColor,
        shape = ItamaeShapes.large,
    )
}

@Composable
fun Modifier.sushiButtonShadow(elevation: Dp): Modifier = shadow(
    elevation = elevation,
    shape = ItamaeShapes.large,
    spotColor = itamaeWasabiAccent(alpha = 0.38f),
    ambientColor = ItamaePrimaryContainer.copy(alpha = 0.28f),
)

@Composable
fun sushiButtonImageBackdropColor(): Color {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDark) {
        ItamaeSushiButtonDarkImageBackdrop
    } else {
        Color.Transparent
    }
}

@Composable
fun sushiButtonImageOutlineColor(): Color {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDark) {
        ItamaeSushiButtonDarkImageOutline
    } else {
        Color.Transparent
    }
}
