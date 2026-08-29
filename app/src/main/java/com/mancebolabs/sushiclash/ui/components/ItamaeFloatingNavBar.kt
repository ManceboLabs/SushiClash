package com.mancebolabs.sushiclash.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.ui.theme.ItamaePreviewTheme
import com.mancebolabs.sushiclash.ui.theme.ItamaeShapes
import com.mancebolabs.sushiclash.ui.theme.itamaeFloatingNavBarShadow
import com.mancebolabs.sushiclash.ui.theme.itamaeWasabiAccent

private val NavBarBorderWidth = 1.5.dp
private val NavItemSize = 64.dp
private val NavIconSize = 32.dp
private val NavIndicatorSize = 56.dp
private val NavItemSpacing = 12.dp
private val NavContainerHorizontalPadding = 12.dp
private val NavContainerVerticalPadding = 8.dp

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
    Row(
        modifier = modifier
            .itamaeFloatingNavBarShadow(shape = ItamaeShapes.extraLarge)
            .clip(ItamaeShapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.95f))
            .border(
                width = NavBarBorderWidth,
                color = itamaeWasabiAccent(alpha = 0.85f),
                shape = ItamaeShapes.extraLarge,
            )
            .padding(
                horizontal = NavContainerHorizontalPadding,
                vertical = NavContainerVerticalPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(NavItemSpacing),
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

@Composable
private fun ItamaeFloatingNavBarItem(
    item: ItamaeNavItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.25f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "navIconScale",
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "navIconTint",
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.72f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "navIconAlpha",
    )
    val indicatorScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "navIndicatorScale",
    )
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "navIndicatorAlpha",
    )

    Box(
        modifier = Modifier
            .size(NavItemSize)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(NavIndicatorSize)
                .scale(indicatorScale)
                .alpha(indicatorAlpha)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
        )

        when {
            item.icon != null -> {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.contentDescription,
                    modifier = Modifier
                        .size(NavIconSize)
                        .scale(iconScale)
                        .alpha(iconAlpha),
                    tint = iconTint,
                )
            }
            item.iconRes != null -> {
                SushiIcon(
                    contentDescription = item.contentDescription,
                    modifier = Modifier
                        .size(NavIconSize)
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

@Preview(name = "Floating nav bar – Counter selected", showBackground = true)
@Composable
private fun FloatingNavBarCounterSelectedPreview() {
    ItamaePreviewTheme {
        ItamaeFloatingNavBar(
            items = previewNavItems,
            selectedRoute = "counter",
            onItemSelected = {},
        )
    }
}

@Preview(name = "Floating nav bar – Wheel selected", showBackground = true)
@Composable
private fun FloatingNavBarWheelSelectedPreview() {
    ItamaePreviewTheme(darkTheme = true) {
        ItamaeFloatingNavBar(
            items = previewNavItems,
            selectedRoute = "wheel",
            onItemSelected = {},
        )
    }
}
