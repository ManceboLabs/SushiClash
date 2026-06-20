package com.mancebolabs.sushiclash.history

import com.mancebolabs.sushiclash.domain.model.GroupGameHistoryEntry
import com.mancebolabs.sushiclash.domain.model.GroupHistoryRanking
import com.mancebolabs.sushiclash.domain.model.PlayerScore
import com.mancebolabs.sushiclash.domain.model.SoloGameHistoryEntry
import com.mancebolabs.sushiclash.domain.model.SoloHistoryRanking
import org.junit.Assert.assertEquals
import org.junit.Test

class GroupHistoryRankingTest {

    @Test
    fun givenMultipleGroupGames_whenAggregating_thenCombinesScoresByPlayerName() {
        val entries = listOf(
            groupEntry(
                players = listOf(
                    PlayerScore("Javier", 20),
                    PlayerScore("Marta", 15),
                ),
            ),
            groupEntry(
                players = listOf(
                    PlayerScore("Javier", 38),
                    PlayerScore("Marta", 29),
                ),
            ),
        )

        val rankings = GroupHistoryRanking.aggregate(entries)

        assertEquals("Javier", rankings[0].playerName)
        assertEquals(38, rankings[0].bestScore)
        assertEquals(58, rankings[0].totalSushi)
        assertEquals(2, rankings[0].gamesPlayed)

        assertEquals("Marta", rankings[1].playerName)
        assertEquals(29, rankings[1].bestScore)
        assertEquals(44, rankings[1].totalSushi)
        assertEquals(2, rankings[1].gamesPlayed)
    }

    @Test
    fun givenEqualBestScores_whenAggregating_thenSortsByTotalSushiDescending() {
        val entries = listOf(
            groupEntry(players = listOf(PlayerScore("Ana", 20), PlayerScore("Luis", 30))),
            groupEntry(players = listOf(PlayerScore("Ana", 30), PlayerScore("Luis", 10))),
        )

        val rankings = GroupHistoryRanking.aggregate(entries)

        assertEquals("Ana", rankings[0].playerName)
        assertEquals(30, rankings[0].bestScore)
        assertEquals(50, rankings[0].totalSushi)
    }

    private fun groupEntry(players: List<PlayerScore>): GroupGameHistoryEntry {
        return GroupGameHistoryEntry(
            id = "entry-${players.hashCode()}",
            date = 1_700_000_000_000L,
            players = players,
            randomRouletteEnabled = false,
            randomRouletteMode = null,
        )
    }
}

class SoloHistoryRankingTest {

    @Test
    fun givenSoloEntries_whenSorting_thenOrdersByScoreThenDate() {
        val entries = listOf(
            soloEntry(totalSushi = 42, date = 100L),
            soloEntry(totalSushi = 50, date = 200L),
            soloEntry(totalSushi = 42, date = 300L),
        )

        val sorted = SoloHistoryRanking.sort(entries)

        assertEquals(50, sorted[0].totalSushi)
        assertEquals(42, sorted[1].totalSushi)
        assertEquals(300L, sorted[1].date)
        assertEquals(42, sorted[2].totalSushi)
        assertEquals(100L, sorted[2].date)
    }

    private fun soloEntry(totalSushi: Int, date: Long): SoloGameHistoryEntry {
        return SoloGameHistoryEntry(
            id = "solo-$totalSushi-$date",
            date = date,
            totalSushi = totalSushi,
            randomRouletteEnabled = false,
            randomRouletteMode = null,
        )
    }
}
