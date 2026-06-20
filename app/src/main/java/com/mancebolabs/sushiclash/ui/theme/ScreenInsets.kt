package com.mancebolabs.sushiclash.ui.theme

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

val LocalFloatingNavBarHeight = staticCompositionLocalOf { ItamaeSpacing.floatingNavBarDefaultHeight }

/**
 * Calculates bottom padding so content clears the floating navigation bar and system navigation area.
 */
@Composable
fun rememberItamaeBottomContentPadding(
    scrollable: Boolean = false,
): Dp {
    val navBarHeight = LocalFloatingNavBarHeight.current
    val systemNavigationPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val contentSpacing = if (scrollable) ItamaeSpacing.md else ItamaeSpacing.lg

    return systemNavigationPadding +
        ItamaeSpacing.navBottomMargin +
        navBarHeight +
        contentSpacing
}

/**
 * Applies system bar and cutout insets plus the design-system top content spacing.
 */
fun Modifier.itamaeScreenTopInsets(): Modifier = this
    .statusBarsPadding()
    .displayCutoutPadding()
    .padding(top = ItamaeSpacing.lg)

/**
 * Applies bottom padding for the floating navigation bar and system navigation insets.
 */
@Composable
fun Modifier.itamaeScreenBottomInsets(
    scrollable: Boolean = false,
): Modifier = padding(bottom = rememberItamaeBottomContentPadding(scrollable = scrollable))

/**
 * Applies top and bottom screen insets. For scrollable screens, prefer applying bottom padding
 * inside the scroll container via [rememberItamaeBottomContentPadding] instead.
 */
@Composable
fun Modifier.itamaeScreenInsets(
    scrollable: Boolean = false,
): Modifier = itamaeScreenTopInsets()
    .itamaeScreenBottomInsets(scrollable = scrollable)
