package com.mancebolabs.sushicounter.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
