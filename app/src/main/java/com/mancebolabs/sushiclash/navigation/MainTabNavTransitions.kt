package com.mancebolabs.sushiclash.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

internal val mainTabRoutes = setOf(
    SushiDestination.Counter.route,
    SushiDestination.Wheel.route,
    SushiDestination.History.route,
    SushiDestination.Settings.route,
)

internal const val MainTabEnterDurationMillis = 160
internal const val MainTabExitDurationMillis = 100
private const val MainTabEnterInitialScale = 0.985f

internal fun isMainTabSwitch(fromRoute: String?, toRoute: String?): Boolean {
    if (fromRoute == null || toRoute == null) return false
    return fromRoute in mainTabRoutes && toRoute in mainTabRoutes && fromRoute != toRoute
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isMainTabSwitch(): Boolean {
    return isMainTabSwitch(
        fromRoute = initialState.destination.route,
        toRoute = targetState.destination.route,
    )
}

internal fun AnimatedContentTransitionScope<NavBackStackEntry>.mainTabEnterTransition(): EnterTransition {
    if (!isMainTabSwitch()) return EnterTransition.None
    val animationSpec = tween<Float>(
        durationMillis = MainTabEnterDurationMillis,
        easing = FastOutSlowInEasing,
    )
    return fadeIn(animationSpec = animationSpec) + scaleIn(
        initialScale = MainTabEnterInitialScale,
        animationSpec = animationSpec,
    )
}

internal fun AnimatedContentTransitionScope<NavBackStackEntry>.mainTabExitTransition(): ExitTransition {
    if (!isMainTabSwitch()) return ExitTransition.None
    return fadeOut(
        animationSpec = tween(
            durationMillis = MainTabExitDurationMillis,
            easing = FastOutSlowInEasing,
        ),
    )
}

internal fun NavGraphBuilder.mainTabComposable(
    route: String,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        enterTransition = { mainTabEnterTransition() },
        exitTransition = { mainTabExitTransition() },
        popEnterTransition = { mainTabEnterTransition() },
        popExitTransition = { mainTabExitTransition() },
        content = content,
    )
}
