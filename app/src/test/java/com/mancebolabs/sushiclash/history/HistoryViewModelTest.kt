package com.mancebolabs.sushiclash.history

import app.cash.turbine.test
import com.mancebolabs.sushiclash.domain.model.GroupGameHistoryEntry
import com.mancebolabs.sushiclash.domain.model.PlayerScore
import com.mancebolabs.sushiclash.domain.model.SoloGameHistoryEntry
import com.mancebolabs.sushiclash.feature.history.HistorySection
import com.mancebolabs.sushiclash.feature.history.HistoryViewModel
import com.mancebolabs.sushiclash.testutil.FakeHistoryRepository
import com.mancebolabs.sushiclash.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenSoloHistory_whenObservingState_thenRanksByScore() = runTest {
        val repository = FakeHistoryRepository().apply {
            setSoloHistory(
                listOf(
                    soloEntry(totalSushi = 10, date = 100L),
                    soloEntry(totalSushi = 30, date = 200L),
                ),
            )
        }
        val viewModel = HistoryViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(30, state.soloItems.first().entry.totalSushi)
            assertEquals(1, state.soloItems.first().position)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenGroupHistory_whenObservingState_thenAggregatesPlayerRankings() = runTest {
        val repository = FakeHistoryRepository().apply {
            setGroupHistory(
                listOf(
                    GroupGameHistoryEntry(
                        id = "g1",
                        date = 100L,
                        players = listOf(PlayerScore("Ana", 20)),
                        randomRouletteEnabled = false,
                        randomRouletteMode = null,
                    ),
                    GroupGameHistoryEntry(
                        id = "g2",
                        date = 200L,
                        players = listOf(PlayerScore("Ana", 35)),
                        randomRouletteEnabled = false,
                        randomRouletteMode = null,
                    ),
                ),
            )
        }
        val viewModel = HistoryViewModel(repository)

        viewModel.uiState.test {
            val ranking = awaitItem().groupItems.first().ranking
            assertEquals("Ana", ranking.playerName)
            assertEquals(35, ranking.bestScore)
            assertEquals(55, ranking.totalSushi)
            assertEquals(2, ranking.gamesPlayed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenGroupSectionSelected_whenSwitchingSection_thenUpdatesSelectedSection() = runTest {
        val viewModel = HistoryViewModel(FakeHistoryRepository())

        viewModel.uiState.test {
            awaitItem()
            viewModel.onSectionSelected(HistorySection.GROUP)
            assertEquals(HistorySection.GROUP, awaitItem().selectedSection)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun soloEntry(totalSushi: Int, date: Long): SoloGameHistoryEntry {
        return SoloGameHistoryEntry(
            id = "solo-$totalSushi",
            date = date,
            totalSushi = totalSushi,
            randomRouletteEnabled = false,
            randomRouletteMode = null,
        )
    }
}
