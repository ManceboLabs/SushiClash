package com.mancebolabs.sushiclash.feature.counter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import com.mancebolabs.sushiclash.domain.model.Player
import com.mancebolabs.sushiclash.ui.components.SushiClickerButton
import com.mancebolabs.sushiclash.ui.theme.ItamaePreviewTheme
import com.mancebolabs.sushiclash.ui.theme.ItamaeSpacing

private val GroupTextBlockHeight = 116.dp
private val MaxGroupButtonSize = 180.dp
private val MinGroupButtonSize = 72.dp

@Composable
fun GroupCounterLayout(
    players: List<Player>,
    onPlayerTapped: (String) -> Unit,
    onPlayerResetRequested: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        val spacing = ItamaeSpacing.sm
        val availableWidth = maxWidth - spacing * 2
        val availableHeight = maxHeight - spacing * 2
        val rowSpecs = groupRowSpecs(
            playerCount = players.size,
            isWide = maxWidth > maxHeight,
        )
        val rowCount = rowSpecs.size
        val maxPlayersPerRow = rowSpecs.maxOrNull() ?: 1

        val cellWidth = if (maxPlayersPerRow == 1) {
            availableWidth
        } else {
            (availableWidth - spacing * (maxPlayersPerRow - 1)) / maxPlayersPerRow
        }
        val cellHeight = (availableHeight - spacing * (rowCount - 1)) / rowCount
        val buttonSize = minOf(cellWidth, cellHeight - GroupTextBlockHeight)
            .coerceIn(MinGroupButtonSize, MaxGroupButtonSize)
        val imageSize = (buttonSize * 0.67f).coerceAtMost(120.dp)

        var playerIndex = 0
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            rowSpecs.forEach { playersInRow ->
                val rowPlayers = players.subList(playerIndex, playerIndex + playersInRow)
                playerIndex += playersInRow

                GroupPlayerRow(
                    rowPlayers = rowPlayers,
                    isCentered = playersInRow == 1 && maxPlayersPerRow > 1,
                    buttonSize = buttonSize,
                    imageSize = imageSize,
                    cellWidth = cellWidth,
                    spacing = spacing,
                    onPlayerTapped = onPlayerTapped,
                    onPlayerResetRequested = onPlayerResetRequested,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }
    }
}

private fun groupRowSpecs(playerCount: Int, isWide: Boolean): List<Int> {
    return when (playerCount) {
        2 -> listOf(1, 1)
        3 -> listOf(2, 1)
        4 -> listOf(2, 2)
        5 -> listOf(2, 2, 1)
        6 -> if (isWide) listOf(3, 3) else listOf(2, 2, 2)
        else -> emptyList()
    }
}

@Composable
private fun GroupPlayerRow(
    rowPlayers: List<Player>,
    isCentered: Boolean,
    buttonSize: Dp,
    imageSize: Dp,
    cellWidth: Dp,
    spacing: Dp,
    onPlayerTapped: (String) -> Unit,
    onPlayerResetRequested: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isCentered && rowPlayers.size == 1) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            GroupPlayerCell(
                player = rowPlayers.first(),
                buttonSize = buttonSize,
                imageSize = imageSize,
                onPlayerTapped = onPlayerTapped,
                onPlayerResetRequested = onPlayerResetRequested,
                modifier = Modifier.width(cellWidth),
            )
        }
    } else {
        Row(
            modifier = modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
        ) {
            rowPlayers.forEach { player ->
                GroupPlayerCell(
                    player = player,
                    buttonSize = buttonSize,
                    imageSize = imageSize,
                    onPlayerTapped = onPlayerTapped,
                    onPlayerResetRequested = onPlayerResetRequested,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun GroupPlayerCell(
    player: Player,
    buttonSize: Dp,
    imageSize: Dp,
    onPlayerTapped: (String) -> Unit,
    onPlayerResetRequested: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        SushiClickerButton(
            onClick = { onPlayerTapped(player.id) },
            onLongClick = { onPlayerResetRequested(player.id) },
            playerName = player.name,
            count = player.sushiCount,
            compact = true,
            buttonSize = buttonSize,
            imageSize = imageSize,
        )
    }
}

private fun previewGroupPlayers(count: Int): List<Player> {
    val names = listOf("Ana", "Luis", "Marta", "Javier", "Bea", "Carlos")
    return names.take(count).mapIndexed { index, name ->
        Player(id = "player-$index", name = name, sushiCount = (index + 1) * 3)
    }
}

@Preview(name = "Group 5 players – Small", showBackground = true, widthDp = 360, heightDp = 520)
@Composable
private fun GroupFivePlayersSmallPreview() {
    ItamaePreviewTheme {
        GroupCounterLayout(
            players = previewGroupPlayers(5),
            onPlayerTapped = {},
            onPlayerResetRequested = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(name = "Group 6 players – Small", showBackground = true, widthDp = 360, heightDp = 520)
@Composable
private fun GroupSixPlayersSmallPreview() {
    ItamaePreviewTheme {
        GroupCounterLayout(
            players = previewGroupPlayers(6),
            onPlayerTapped = {},
            onPlayerResetRequested = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(name = "Group 6 players – Wide", showBackground = true, widthDp = 780, heightDp = 360)
@Composable
private fun GroupSixPlayersWidePreview() {
    ItamaePreviewTheme {
        GroupCounterLayout(
            players = previewGroupPlayers(6),
            onPlayerTapped = {},
            onPlayerResetRequested = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
