package com.mancebolabs.sushiclash.ui.theme

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
fun Modifier.itamaeFloatingNavBarShadow(
    elevation: Dp = 4.dp,
    shape: Shape = ItamaeShapes.extraLarge,
): Modifier = shadow(
    elevation = elevation,
    shape = shape,
    spotColor = itamaeWasabiAccent(alpha = 0.22f),
    ambientColor = itamaeWasabiAccent(alpha = 0.12f),
)

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
): Modifier = shadow(
    elevation = elevation,
    shape = shape,
    spotColor = Color.Black.copy(alpha = 0.06f),
    ambientColor = Color.Black.copy(alpha = 0.03f),
)
