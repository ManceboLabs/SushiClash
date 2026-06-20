package com.mancebolabs.sushicounter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import com.mancebolabs.sushicounter.ui.theme.ItamaePillShape
import com.mancebolabs.sushicounter.ui.theme.ItamaeShapes
import com.mancebolabs.sushicounter.ui.theme.ItamaeSpacing
import com.mancebolabs.sushicounter.ui.theme.itamaeInteractionShadow

data class ItamaeNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun ItamaeFloatingNavBar(
    items: List<ItamaeNavItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
  Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ItamaeSpacing.navSidePadding)
            .itamaeInteractionShadow(shape = ItamaeShapes.extraLarge)
            .clip(ItamaeShapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.95f))
            .padding(horizontal = ItamaeSpacing.sm, vertical = ItamaeSpacing.sm),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            val selected = item.route == selectedRoute
            Row(
                modifier = Modifier
                    .clip(ItamaePillShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onItemSelected(item.route) },
                    )
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0f)
                        },
                    )
                    .padding(horizontal = ItamaeSpacing.md, vertical = ItamaeSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(ItamaeSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}
