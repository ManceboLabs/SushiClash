package com.mancebolabs.sushiclash.counter

import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.GameSetupConfig
import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.Player
import com.mancebolabs.sushiclash.feature.counter.AppStartupState
import com.mancebolabs.sushiclash.feature.counter.CounterViewModel
import com.mancebolabs.sushiclash.feature.counter.RouletteTriggerEvent
import com.mancebolabs.sushiclash.testutil.FakeGameRepository
import com.mancebolabs.sushiclash.testutil.FakeHistoryRepository
import com.mancebolabs.sushiclash.testutil.FakeOnboardingRepository
import com.mancebolabs.sushiclash.testutil.MainDispatcherRule
import com.mancebolabs.sushiclash.testutil.TestGameStates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CounterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenNoActiveGameOnLaunch_whenInitialized_thenShowsNoActiveGameWithoutSetupPopup() = runTest {
        val viewModel = createViewModel(initialState = GameState(hasActiveGame = false))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AppStartupState.NoActiveGame, state.startupState)
        assertFalse(state.showSetupDialog)
    }

    @Test
    fun givenActiveGameOnLaunch_whenInitialized_thenShowsActiveGame() = runTest {
        val viewModel = createViewModel(initialState = TestGameStates.soloActive(count = 3))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AppStartupState.ActiveGame, state.startupState)
        assertEquals(3, state.soloCount)
    }

    @Test
    fun givenNoActiveGame_whenStartGameRequested_thenShowsSetupDialogOnly() = runTest {
        val viewModel = createViewModel(initialState = GameState(hasActiveGame = false))
        advanceUntilIdle()

        viewModel.onStartGameRequested()

        val state = viewModel.uiState.value
        assertTrue(state.showSetupDialog)
        assertEquals(AppStartupState.NoActiveGame, state.startupState)
    }

    @Test
    fun givenSetupConfirmed_whenStartingSoloGame_thenActivatesGameAndHidesSetup() = runTest {
        val gameRepository = FakeGameRepository(GameState(hasActiveGame = false))
        val viewModel = CounterViewModel(gameRepository, FakeHistoryRepository(), FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onSetupConfirmed(
            GameSetupConfig(gameMode = GameMode.SOLO),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AppStartupState.ActiveGame, state.startupState)
        assertFalse(state.showSetupDialog)
        assertEquals(GameMode.SOLO, state.gameMode)
    }

    @Test
    fun givenActiveSoloGame_whenSushiTapped_thenCounterIncrements() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 2))
        val viewModel = CounterViewModel(gameRepository, FakeHistoryRepository(), FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onSoloSushiTapped()
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.soloCount)
    }

    @Test
    fun givenNoActiveGame_whenSushiTapped_thenCounterDoesNotIncrement() = runTest {
        val gameRepository = FakeGameRepository(GameState(hasActiveGame = false))
        val viewModel = CounterViewModel(gameRepository, FakeHistoryRepository(), FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onSoloSushiTapped()
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.soloCount)
    }

    @Test
    fun givenGroupPlayer_whenResetRequestedAndConfirmed_thenOnlyThatPlayerResets() = runTest {
        val players = listOf(
            Player(id = "p1", name = "Ana", sushiCount = 5),
            Player(id = "p2", name = "Luis", sushiCount = 2),
        )
        val gameRepository = FakeGameRepository(TestGameStates.groupActive(players))
        val viewModel = CounterViewModel(gameRepository, FakeHistoryRepository(), FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onPlayerResetRequested("p1")
        assertEquals("Ana", viewModel.uiState.value.playerResetRequest?.playerName)

        viewModel.onPlayerResetConfirmed()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.playerResetRequest)
        assertEquals(0, viewModel.uiState.value.players.first { it.id == "p1" }.sushiCount)
        assertEquals(2, viewModel.uiState.value.players.first { it.id == "p2" }.sushiCount)
    }

    @Test
    fun givenActiveGame_whenFinishRequested_thenKeepsActiveGameAndShowsDialogOnly() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 9))
        val viewModel = CounterViewModel(gameRepository, FakeHistoryRepository(), FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onFinishGameRequested()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AppStartupState.ActiveGame, state.startupState)
        assertTrue(state.showFinishGameDialog)
        assertTrue(state.gameState.hasActiveGame)
        assertEquals(9, state.soloCount)
        assertEquals(0, gameRepository.createFinishedGameSnapshotCallCount)
        assertEquals(0, gameRepository.clearActiveGameCallCount)
    }

    @Test
    fun givenFinishDialog_whenSaving_thenPersistsHistoryClearsGameAndClosesDialog() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 9))
        val historyRepository = FakeHistoryRepository()
        val viewModel = CounterViewModel(gameRepository, historyRepository, FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onFinishGameRequested()
        advanceUntilIdle()
        viewModel.onFinishGameWithSaving()
        advanceUntilIdle()

        assertEquals(1, gameRepository.createFinishedGameSnapshotCallCount)
        assertEquals(1, gameRepository.clearActiveGameCallCount)
        assertEquals(1, historyRepository.savedSnapshots.size)
        assertEquals(9, historyRepository.savedSnapshots.first().soloCount)
        assertFalse(viewModel.uiState.value.showFinishGameDialog)
        assertEquals(AppStartupState.NoActiveGame, viewModel.uiState.value.startupState)
        assertFalse(viewModel.uiState.value.gameState.hasActiveGame)
    }

    @Test
    fun givenFinishDialog_whenNotSaving_thenClearsGameWithoutHistory() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 9))
        val historyRepository = FakeHistoryRepository()
        val viewModel = CounterViewModel(gameRepository, historyRepository, FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onFinishGameRequested()
        advanceUntilIdle()
        viewModel.onFinishGameWithoutSaving()
        advanceUntilIdle()

        assertEquals(0, gameRepository.createFinishedGameSnapshotCallCount)
        assertEquals(1, gameRepository.clearActiveGameCallCount)
        assertTrue(historyRepository.savedSnapshots.isEmpty())
        assertFalse(viewModel.uiState.value.showFinishGameDialog)
        assertEquals(AppStartupState.NoActiveGame, viewModel.uiState.value.startupState)
        assertFalse(viewModel.uiState.value.gameState.hasActiveGame)
    }

    @Test
    fun givenFinishDialog_whenCancelled_thenKeepsActiveGameUnchanged() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 9))
        val viewModel = CounterViewModel(gameRepository, FakeHistoryRepository(), FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onFinishGameRequested()
        advanceUntilIdle()
        viewModel.onFinishGameCancelled()

        val state = viewModel.uiState.value
        assertFalse(state.showFinishGameDialog)
        assertEquals(AppStartupState.ActiveGame, state.startupState)
        assertTrue(state.gameState.hasActiveGame)
        assertEquals(9, state.soloCount)
        assertEquals(0, gameRepository.createFinishedGameSnapshotCallCount)
        assertEquals(0, gameRepository.clearActiveGameCallCount)
    }

    @Test
    fun givenFinishDialogVisible_whenViewModelRecreated_thenActiveGameIsRestoredWithoutDialog() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 7))
        val viewModel = CounterViewModel(gameRepository, FakeHistoryRepository(), FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onFinishGameRequested()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showFinishGameDialog)
        assertTrue(viewModel.uiState.value.gameState.hasActiveGame)

        // Dialog visibility is in-memory only; closing the app must not clear persisted active state.
        val restartedViewModel = CounterViewModel(gameRepository, FakeHistoryRepository(), FakeOnboardingRepository())
        advanceUntilIdle()

        assertFalse(restartedViewModel.uiState.value.showFinishGameDialog)
        assertEquals(AppStartupState.ActiveGame, restartedViewModel.uiState.value.startupState)
        assertEquals(7, restartedViewModel.uiState.value.soloCount)
        assertTrue(restartedViewModel.uiState.value.gameState.hasActiveGame)
    }

    @Test
    fun givenFixedRouletteTrigger_whenThresholdReached_thenEmitsRouletteEvent() = runTest {
        val gameRepository = FakeGameRepository(
            TestGameStates.soloActive(
                count = 4,
                randomRouletteEnabled = true,
                fixedThreshold = 5,
            ),
        )
        val viewModel = CounterViewModel(gameRepository, FakeHistoryRepository(), FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onSoloSushiTapped()
        advanceUntilIdle()

        assertEquals(RouletteTriggerEvent.Solo(count = 5), viewModel.uiState.value.rouletteTriggerEvent)
    }

    private fun createViewModel(initialState: GameState): CounterViewModel {
        return CounterViewModel(
            FakeGameRepository(initialState),
            FakeHistoryRepository(),
            FakeOnboardingRepository(completed = true),
        )
    }
}
