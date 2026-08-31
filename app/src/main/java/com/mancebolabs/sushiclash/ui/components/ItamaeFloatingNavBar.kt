package com.mancebolabs.sushiclash.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.ui.theme.FloatingNavBarMetrics
import com.mancebolabs.sushiclash.ui.theme.ItamaePreviewTheme
import com.mancebolabs.sushiclash.ui.theme.ItamaeShapes
import com.mancebolabs.sushiclash.ui.theme.itamaeFloatingNavBarShadow
import com.mancebolabs.sushiclash.ui.theme.itamaeFloatingNavBarSurfaceColor
import com.mancebolabs.sushiclash.ui.theme.itamaeNavInactiveIconColor
import com.mancebolabs.sushiclash.ui.theme.itamaeNavSelectedIndicatorColor
import com.mancebolabs.sushiclash.ui.theme.itamaeNavSelectedIndicatorShadow
import com.mancebolabs.sushiclash.ui.theme.rememberItamaeNigiriBorderBrush

private val navFloatAnimationSpec = tween<Float>(
    durationMillis = FloatingNavBarMetrics.indicatorAnimationMillis,
    easing = FastOutSlowInEasing,
)

private val navColorAnimationSpec: AnimationSpec<Color> = tween(
    durationMillis = FloatingNavBarMetrics.indicatorAnimationMillis,
    easing = FastOutSlowInEasing,
)

private val navDpAnimationSpec: AnimationSpec<Dp> = tween(
    durationMillis = FloatingNavBarMetrics.indicatorAnimationMillis,
    easing = FastOutSlowInEasing,
)

data class ItamaeNavItem(
    val route: String,
    val contentDescription: String,
    val icon: ImageVector? = null,
    @param:DrawableRes val iconRes: Int? = null,
) {
    init {
        require((icon != null) xor (iconRes != null)) {
            "Provide either icon or iconRes for $route"
        }
    }
}

@Composable
fun ItamaeFloatingNavBar(
    items: List<ItamaeNavItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = items.indexOfFirst { it.route == selectedRoute }.coerceAtLeast(0)
    val indicatorOffset by animateDpAsState(
        targetValue = FloatingNavBarMetrics.indicatorOffsetForIndex(selectedIndex),
        animationSpec = navDpAnimationSpec,
        label = "navIndicatorOffset",
    )
    val nigiriBorderBrush = rememberItamaeNigiriBorderBrush()

    Box(
        modifier = modifier
            .itamaeFloatingNavBarShadow(shape = ItamaeShapes.extraLarge)
            .clip(ItamaeShapes.extraLarge)
            .background(itamaeFloatingNavBarSurfaceColor())
            .border(
                width = FloatingNavBarMetrics.borderWidth,
                brush = nigiriBorderBrush,
                shape = ItamaeShapes.extraLarge,
            )
            .padding(
                horizontal = FloatingNavBarMetrics.containerHorizontalPadding,
                vertical = FloatingNavBarMetrics.containerVerticalPadding,
            ),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = indicatorOffset)
                .size(FloatingNavBarMetrics.indicatorSize)
                .itamaeNavSelectedIndicatorShadow()
                .clip(CircleShape)
                .background(itamaeNavSelectedIndicatorColor()),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(FloatingNavBarMetrics.itemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                ItamaeFloatingNavBarItem(
                    item = item,
                    selected = item.route == selectedRoute,
                    onClick = { onItemSelected(item.route) },
                )
            }
        }
    }
}

@Composable
private fun ItamaeFloatingNavBarItem(
    item: ItamaeNavItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.2f else 1f,
        animationSpec = navFloatAnimationSpec,
        label = "navIconScale",
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            itamaeNavInactiveIconColor()
        },
        animationSpec = navColorAnimationSpec,
        label = "navIconTint",
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.78f,
        animationSpec = navFloatAnimationSpec,
        label = "navIconAlpha",
    )

    Box(
        modifier = Modifier
            .size(FloatingNavBarMetrics.itemSize)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        when {
            item.icon != null -> {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.contentDescription,
                    modifier = Modifier
                        .size(FloatingNavBarMetrics.iconSize)
                        .scale(iconScale)
                        .alpha(iconAlpha),
                    tint = iconTint,
                )
            }
            item.iconRes != null -> {
                SushiIcon(
                    contentDescription = item.contentDescription,
                    modifier = Modifier
                        .size(FloatingNavBarMetrics.iconSize)
                        .scale(iconScale)
                        .alpha(iconAlpha),
                )
            }
        }
    }
}

private val previewNavItems = listOf(
    ItamaeNavItem(
        route = "counter",
        contentDescription = "Contador",
        iconRes = R.drawable.ic_sushi,
    ),
    ItamaeNavItem(
        route = "wheel",
        contentDescription = "Ruleta",
        icon = Icons.Default.Casino,
    ),
    ItamaeNavItem(
        route = "history",
        contentDescription = "Histórico",
        icon = Icons.Default.EmojiEvents,
    ),
    ItamaeNavItem(
        route = "settings",
        contentDescription = "Ajustes",
        icon = Icons.Default.Settings,
    ),
)

@Preview(name = "Floating nav bar – Counter selected (Light)", showBackground = true)
@Composable
private fun FloatingNavBarCounterSelectedLightPreview() {
    ItamaePreviewTheme {
        ItamaeFloatingNavBar(
            items = previewNavItems,
            selectedRoute = "counter",
            onItemSelected = {},
        )
    }
}

@Preview(name = "Floating nav bar – Wheel selected (Dark)", showBackground = true, backgroundColor = 0xFF1A1C1E)
@Composable
private fun FloatingNavBarWheelSelectedDarkPreview() {
    ItamaePreviewTheme(darkTheme = true) {
        ItamaeFloatingNavBar(
            items = previewNavItems,
            selectedRoute = "wheel",
            onItemSelected = {},
        )
    }
}

@Preview(name = "Floating nav bar – History selected (Light)", showBackground = true)
@Composable
private fun FloatingNavBarHistorySelectedLightPreview() {
    ItamaePreviewTheme {
        ItamaeFloatingNavBar(
            items = previewNavItems,
            selectedRoute = "history",
            onItemSelected = {},
        )
    }
}

@Preview(name = "Floating nav bar – Settings selected (Dark)", showBackground = true, backgroundColor = 0xFF1A1C1E)
@Composable
private fun FloatingNavBarSettingsSelectedDarkPreview() {
    ItamaePreviewTheme(darkTheme = true) {
        ItamaeFloatingNavBar(
            items = previewNavItems,
            selectedRoute = "settings",
            onItemSelected = {},
        )
    }
}
