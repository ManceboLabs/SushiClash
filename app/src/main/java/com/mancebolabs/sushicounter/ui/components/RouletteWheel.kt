package com.mancebolabs.sushicounter.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mancebolabs.sushicounter.feature.wheel.WheelViewModel
import com.mancebolabs.sushicounter.ui.theme.ItamaeWheelSegmentColors
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun RouletteWheel(
    participants: List<String>,
    targetRotation: Float,
    isSpinning: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val animatedRotation = remember { Animatable(targetRotation) }
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(targetRotation, isSpinning) {
        if (isSpinning) {
            animatedRotation.animateTo(
                targetValue = targetRotation,
                animationSpec = tween(
                    durationMillis = WheelViewModel.SPIN_DURATION_MS.toInt(),
                    easing = FastOutSlowInEasing,
                ),
            )
        } else {
            animatedRotation.snapTo(targetRotation)
        }
    }

    val pointerColor = if (isSystemInDarkTheme()) {
        Color.White
    } else {
        Color.Black
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
                .clickable { onClick() },
        ) {
            val diameter = min(size.width, size.height)
            val radius = diameter / 2f
            val topLeft = Offset(
                x = (size.width - diameter) / 2f,
                y = (size.height - diameter) / 2f,
            )

            rotate(degrees = animatedRotation.value, pivot = Offset(size.width / 2f, topLeft.y + radius)) {
                drawWheelSegments(
                    participants = participants,
                    topLeft = topLeft,
                    diameter = diameter,
                    textMeasurer = textMeasurer,
                )
            }

            drawPointer(
                centerX = size.width / 2f,
                wheelTopY = topLeft.y,
                color = pointerColor,
            )
        }
    }
}

private fun DrawScope.drawWheelSegments(
    participants: List<String>,
    topLeft: Offset,
    diameter: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
) {
    if (participants.isEmpty()) return

    val segmentAngle = 360f / participants.size
    val radius = diameter / 2f
    val center = Offset(topLeft.x + radius, topLeft.y + radius)

    participants.forEachIndexed { index, name ->
        val startAngle = index * segmentAngle - 90f
        val segmentColor = ItamaeWheelSegmentColors[index % ItamaeWheelSegmentColors.size]

        drawArc(
            color = segmentColor,
            startAngle = startAngle,
            sweepAngle = segmentAngle,
            useCenter = true,
            topLeft = topLeft,
            size = Size(diameter, diameter),
        )

        drawArc(
            color = Color.White.copy(alpha = 0.25f),
            startAngle = startAngle,
            sweepAngle = segmentAngle,
            useCenter = true,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f),
        )

        val labelAngleRadians = Math.toRadians((startAngle + segmentAngle / 2f).toDouble())
        val labelRadius = radius * 0.62f
        val labelCenter = Offset(
            x = center.x + (cos(labelAngleRadians) * labelRadius).toFloat(),
            y = center.y + (sin(labelAngleRadians) * labelRadius).toFloat(),
        )

        val textLayoutResult = textMeasurer.measure(
            text = name,
            style = TextStyle(
                color = Color.White,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
        )

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                x = labelCenter.x - textLayoutResult.size.width / 2f,
                y = labelCenter.y - textLayoutResult.size.height / 2f,
            ),
        )
    }

    drawCircle(
        color = Color.White,
        radius = radius * 0.12f,
        center = center,
    )
    drawCircle(
        color = Color.Black.copy(alpha = 0.15f),
        radius = radius * 0.12f,
        center = center,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f),
    )
}

private fun DrawScope.drawPointer(
    centerX: Float,
    wheelTopY: Float,
    color: Color,
) {
    val pointerHeight = 28f
    val pointerHalfWidth = 16f
    val tipY = wheelTopY + 6f
    val baseY = tipY - pointerHeight

    val pointerPath = Path().apply {
        moveTo(centerX, tipY)
        lineTo(centerX - pointerHalfWidth, baseY)
        lineTo(centerX + pointerHalfWidth, baseY)
        close()
    }
    drawPath(
        path = pointerPath,
        color = color,
    )
}
