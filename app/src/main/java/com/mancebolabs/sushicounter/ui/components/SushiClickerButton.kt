package com.mancebolabs.sushicounter.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mancebolabs.sushicounter.R
import com.mancebolabs.sushicounter.ui.theme.ItamaePrimaryContainer
import com.mancebolabs.sushicounter.ui.theme.ItamaeShapes
import com.mancebolabs.sushicounter.ui.theme.itamaeInteractionShadow
import com.mancebolabs.sushicounter.ui.theme.itamaePressedShadow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val PRESSED_SCALE = 0.92f
private const val PEAK_SCALE = 1.15f
private const val RESTING_SCALE = 1f
private const val PRESS_ANIMATION_MS = 100
private const val FLOATING_PLUS_ONE_MS = 600
private const val FLOATING_TRAVEL_DP = 56f

@Composable
fun SushiClickerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    playerName: String? = null,
    count: Int? = null,
    compact: Boolean = false,
    buttonSize: Dp = 220.dp,
    imageSize: Dp = 148.dp,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(RESTING_SCALE) }
    var isPressed by remember { mutableStateOf(false) }
    var plusOneEffects by remember { mutableStateOf(emptyList<Long>()) }
    var releaseAnimationJob by remember { mutableStateOf<Job?>(null) }

    val shadowElevation by animateDpAsState(
        targetValue = when {
            isPressed -> 1.dp
            isDarkTheme -> 6.dp
            else -> 3.dp
        },
        animationSpec = tween(durationMillis = PRESS_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "sushiButtonShadow",
    )

    val pressAnimationSpec = tween<Float>(
        durationMillis = PRESS_ANIMATION_MS,
        easing = FastOutSlowInEasing,
    )

    val containerColor = when {
        isPressed && isDarkTheme -> MaterialTheme.colorScheme.surfaceContainerHighest
        isPressed -> MaterialTheme.colorScheme.surfaceContainerHigh
        isDarkTheme -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surfaceContainerLowest
    }

    val shadowModifier = when {
        isPressed -> Modifier.itamaePressedShadow(
            elevation = shadowElevation,
            shape = ItamaeShapes.large,
        )
        isDarkTheme -> Modifier.shadow(
            elevation = shadowElevation,
            shape = ItamaeShapes.large,
            spotColor = ItamaePrimaryContainer.copy(alpha = 0.28f),
            ambientColor = ItamaePrimaryContainer.copy(alpha = 0.14f),
        )
        else -> Modifier.itamaeInteractionShadow(
            elevation = shadowElevation,
            shape = ItamaeShapes.large,
        )
    }

    val borderModifier = if (isDarkTheme) {
        Modifier.border(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
            shape = ItamaeShapes.large,
        )
    } else {
        Modifier
    }

    val nameAreaHeight = if (compact && !playerName.isNullOrBlank()) 28.dp else 0.dp
    val compactContentHeight = if (compact) {
        buttonSize + GroupCompactTextHeight + nameAreaHeight
    } else {
        buttonSize
    }
    val outerHeight = if (compact) compactContentHeight else 300.dp
    val outerWidth = if (compact) {
        maxOf(buttonSize + 20.dp, cellContentMinWidth(compact))
    } else {
        240.dp
    }

    Box(
        modifier = modifier.size(width = outerWidth, height = outerHeight),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (compact && !playerName.isNullOrBlank()) {
                Text(
                    text = playerName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(buttonSize)
                        .then(shadowModifier)
                        .then(borderModifier)
                        .clip(ItamaeShapes.large)
                        .background(containerColor)
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                        }
                        .pointerInput(onClick, onLongClick) {
                            if (onLongClick != null) {
                                var longPressTriggered = false
                                detectTapGestures(
                                    onPress = {
                                        longPressTriggered = false
                                        isPressed = true
                                        releaseAnimationJob?.cancel()
                                        scope.launch {
                                            scale.animateTo(PRESSED_SCALE, pressAnimationSpec)
                                        }

                                        val released = tryAwaitRelease()
                                        isPressed = false

                                        if (longPressTriggered || !released) {
                                            scope.launch {
                                                scale.animateTo(RESTING_SCALE, pressAnimationSpec)
                                            }
                                        }
                                    },
                                    onTap = {
                                        onClick()
                                        plusOneEffects = plusOneEffects + System.nanoTime()
                                        releaseAnimationJob = scope.launch {
                                            scale.snapTo(PRESSED_SCALE)
                                            scale.animateTo(
                                                targetValue = PEAK_SCALE,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessHigh,
                                                ),
                                            )
                                            scale.animateTo(
                                                targetValue = RESTING_SCALE,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMedium,
                                                ),
                                            )
                                        }
                                    },
                                    onLongPress = {
                                        longPressTriggered = true
                                        onLongClick.invoke()
                                    },
                                )
                            } else {
                                awaitEachGesture {
                                    awaitFirstDown().consume()
                                    isPressed = true
                                    releaseAnimationJob?.cancel()
                                    scope.launch {
                                        scale.animateTo(PRESSED_SCALE, pressAnimationSpec)
                                    }

                                    val up = waitForUpOrCancellation()
                                    isPressed = false

                                    if (up != null) {
                                        onClick()
                                        plusOneEffects = plusOneEffects + System.nanoTime()
                                        releaseAnimationJob = scope.launch {
                                            scale.snapTo(PRESSED_SCALE)
                                            scale.animateTo(
                                                targetValue = PEAK_SCALE,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessHigh,
                                                ),
                                            )
                                            scale.animateTo(
                                                targetValue = RESTING_SCALE,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMedium,
                                                ),
                                            )
                                        }
                                    } else {
                                        scope.launch {
                                            scale.animateTo(RESTING_SCALE, pressAnimationSpec)
                                        }
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_sushi),
                        contentDescription = stringResource(R.string.counter_sushi_content_description),
                        modifier = Modifier
                            .size(imageSize)
                            .clip(ItamaeShapes.large),
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-12).dp),
                    contentAlignment = Alignment.Center,
                ) {
                    plusOneEffects.forEach { effectId ->
                        key(effectId) {
                            FloatingPlusOne(
                                isDarkTheme = isDarkTheme,
                                onFinished = {
                                    plusOneEffects = plusOneEffects.filterNot { it == effectId }
                                },
                            )
                        }
                    }
                }
            }

            if (compact && count != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.counter_sushi_eaten),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

private val GroupCompactTextHeight = 88.dp

private fun cellContentMinWidth(compact: Boolean): Dp {
    return if (compact) 120.dp else 240.dp
}

@Composable
private fun FloatingPlusOne(
    isDarkTheme: Boolean,
    onFinished: () -> Unit,
) {
    val progress = remember { Animatable(0f) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = FLOATING_PLUS_ONE_MS,
                easing = FastOutSlowInEasing,
            ),
        )
        onFinished()
    }

    val alpha = when {
        progress.value < 0.2f -> progress.value / 0.2f
        progress.value > 0.65f -> 1f - ((progress.value - 0.65f) / 0.35f)
        else -> 1f
    }
    val verticalOffset = (-FLOATING_TRAVEL_DP * progress.value).dp

    Text(
        text = stringResource(R.string.counter_plus_one),
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = if (isDarkTheme) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.primary
        },
        modifier = Modifier
            .offset(y = verticalOffset)
            .graphicsLayer { this.alpha = alpha },
    )
}
