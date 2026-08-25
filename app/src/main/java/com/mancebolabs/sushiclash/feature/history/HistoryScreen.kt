package com.mancebolabs.sushiclash.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.domain.model.GroupPlayerRanking
import com.mancebolabs.sushiclash.domain.model.SoloGameHistoryEntry
import com.mancebolabs.sushiclash.ui.components.ItamaeCard
import com.mancebolabs.sushiclash.ui.components.ItamaeScreenTitle
import com.mancebolabs.sushiclash.ui.components.PersistenceErrorMessage
import com.mancebolabs.sushiclash.ui.theme.ItamaePreviewTheme
import com.mancebolabs.sushiclash.ui.theme.ItamaeShapes
import com.mancebolabs.sushiclash.ui.theme.ItamaeSpacing
import com.mancebolabs.sushiclash.ui.theme.itamaeScreenTopInsets
import com.mancebolabs.sushiclash.ui.theme.rememberItamaeBottomContentPadding

internal fun shouldShowHistoryEmptyCopy(
    persistenceError: Boolean,
    hasItems: Boolean,
): Boolean {
    return !persistenceError && !hasItems
}

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onSectionSelected: (HistorySection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val bottomContentPadding = rememberItamaeBottomContentPadding(scrollable = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .itamaeScreenTopInsets()
            .verticalScroll(scrollState)
            .padding(horizontal = ItamaeSpacing.marginMobile)
            .padding(bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.md),
    ) {
        ItamaeScreenTitle(title = stringResource(R.string.history_screen_title))

        if (uiState.persistenceError) {
            PersistenceErrorMessage(isRetrying = uiState.isPersistenceRetrying)
        }

        HistorySectionSelector(
            selectedSection = uiState.selectedSection,
            onSectionSelected = onSectionSelected,
        )

        when (uiState.selectedSection) {
            HistorySection.SOLO -> SoloHistoryContent(
                items = uiState.soloItems,
                persistenceError = uiState.persistenceError,
            )
            HistorySection.GROUP -> GroupHistoryContent(
                items = uiState.groupItems,
                persistenceError = uiState.persistenceError,
            )
        }
    }
}

@Composable
private fun HistorySectionSelector(
    selectedSection: HistorySection,
    onSectionSelected: (HistorySection) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ItamaeSpacing.sm),
    ) {
        HistorySectionChip(
            label = stringResource(R.string.history_section_solo),
            selected = selectedSection == HistorySection.SOLO,
            onClick = { onSectionSelected(HistorySection.SOLO) },
            modifier = Modifier.weight(1f),
        )
        HistorySectionChip(
            label = stringResource(R.string.history_section_group),
            selected = selectedSection == HistorySection.GROUP,
            onClick = { onSectionSelected(HistorySection.GROUP) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HistorySectionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Box(
        modifier = modifier
            .clip(ItamaeShapes.small)
            .border(width = 1.dp, color = borderColor, shape = ItamaeShapes.small)
            .background(containerColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = ItamaeSpacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun SoloHistoryContent(
    items: List<SoloHistoryItem>,
    persistenceError: Boolean,
) {
    if (shouldShowHistoryEmptyCopy(persistenceError = persistenceError, hasItems = items.isNotEmpty())) {
        HistoryEmptyState(message = stringResource(R.string.history_solo_empty))
        return
    }
    if (items.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.sm)) {
        items.forEach { item ->
            SoloHistoryCard(item = item)
        }
    }
}

@Composable
private fun SoloHistoryCard(
    item: SoloHistoryItem,
) {
    ItamaeCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ItamaeSpacing.md),
        ) {
            RankingBadge(position = item.position)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.xs),
            ) {
                Text(
                    text = stringResource(R.string.history_solo_sushi_count, item.entry.totalSushi),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = HistoryDateFormatter.format(item.entry.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (item.entry.randomRouletteEnabled) {
                    HistoryFeatureChip(label = stringResource(R.string.history_random_roulette_chip))
                }
            }
        }
    }
}

@Composable
private fun GroupHistoryContent(
    items: List<GroupHistoryItem>,
    persistenceError: Boolean,
) {
    if (shouldShowHistoryEmptyCopy(persistenceError = persistenceError, hasItems = items.isNotEmpty())) {
        HistoryEmptyState(message = stringResource(R.string.history_group_empty))
        return
    }
    if (items.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.sm)) {
        items.forEach { item ->
            GroupHistoryCard(item = item)
        }
    }
}

@Composable
private fun GroupHistoryCard(
    item: GroupHistoryItem,
) {
    ItamaeCard {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(ItamaeSpacing.md),
        ) {
            RankingBadge(position = item.position)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ItamaeSpacing.xs),
            ) {
                Text(
                    text = item.ranking.playerName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.history_group_best_score, item.ranking.bestScore),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.history_group_total_score, item.ranking.totalSushi),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.history_group_games_played, item.ranking.gamesPlayed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RankingBadge(
    position: Int,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.history_rank_position, position),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun HistoryFeatureChip(
    label: String,
) {
    Text(
        text = label,
        modifier = Modifier
            .clip(ItamaeShapes.small)
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
            .padding(horizontal = ItamaeSpacing.sm, vertical = ItamaeSpacing.xs),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
    )
}

@Composable
private fun HistoryEmptyState(
    message: String,
) {
    ItamaeCard {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val previewSoloHistory = HistoryUiState(
    selectedSection = HistorySection.SOLO,
    soloItems = listOf(
        SoloHistoryItem(
            position = 1,
            entry = SoloGameHistoryEntry(
                id = "solo-1",
                date = 1_718_000_000_000L,
                totalSushi = 42,
                randomRouletteEnabled = true,
                randomRouletteMode = "FIXED",
            ),
        ),
    ),
)

private val previewGroupHistory = HistoryUiState(
    selectedSection = HistorySection.GROUP,
    groupItems = listOf(
        GroupHistoryItem(
            position = 1,
            ranking = GroupPlayerRanking(
                playerName = "Javier",
                bestScore = 38,
                totalSushi = 120,
                gamesPlayed = 5,
            ),
        ),
    ),
)

@Preview(name = "History solo – Light", showBackground = true)
@Composable
private fun HistorySoloLightPreview() {
    ItamaePreviewTheme(darkTheme = false) {
        HistoryScreen(uiState = previewSoloHistory, onSectionSelected = {})
    }
}

@Preview(name = "History solo – Dark", showBackground = true)
@Composable
private fun HistorySoloDarkPreview() {
    ItamaePreviewTheme(darkTheme = true) {
        HistoryScreen(uiState = previewSoloHistory, onSectionSelected = {})
    }
}

@Preview(name = "History group – Light", showBackground = true)
@Composable
private fun HistoryGroupLightPreview() {
    ItamaePreviewTheme(darkTheme = false) {
        HistoryScreen(uiState = previewGroupHistory, onSectionSelected = {})
    }
}

@Preview(name = "History group – Dark", showBackground = true)
@Composable
private fun HistoryGroupDarkPreview() {
    ItamaePreviewTheme(darkTheme = true) {
        HistoryScreen(uiState = previewGroupHistory, onSectionSelected = {})
    }
}

@Preview(name = "History solo empty", showBackground = true)
@Composable
private fun HistorySoloEmptyPreview() {
    ItamaePreviewTheme {
        HistoryScreen(
            uiState = HistoryUiState(selectedSection = HistorySection.SOLO),
            onSectionSelected = {},
        )
    }
}

@Preview(name = "History persistence error – Light", showBackground = true)
@Composable
private fun HistoryPersistenceErrorLightPreview() {
    ItamaePreviewTheme(darkTheme = false) {
        HistoryScreen(
            uiState = HistoryUiState(persistenceError = true),
            onSectionSelected = {},
        )
    }
}

@Preview(name = "History persistence error – Dark", showBackground = true)
@Composable
private fun HistoryPersistenceErrorDarkPreview() {
    ItamaePreviewTheme(darkTheme = true) {
        HistoryScreen(
            uiState = HistoryUiState(persistenceError = true),
            onSectionSelected = {},
        )
    }
}
