package com.mancebolabs.sushiclash.counter

import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.FinishGameResult
import com.mancebolabs.sushiclash.domain.model.GameSetupConfig
import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.Player
import com.mancebolabs.sushiclash.domain.model.RestoreGameResult
import com.mancebolabs.sushiclash.feature.counter.AppStartupState
import com.mancebolabs.sushiclash.feature.counter.CounterViewModel
import com.mancebolabs.sushiclash.feature.counter.RouletteTriggerEvent
import com.mancebolabs.sushiclash.testutil.FakeGameRepository
import com.mancebolabs.sushiclash.testutil.FakeOnboardingRepository
import com.mancebolabs.sushiclash.testutil.MainDispatcherRule
import com.mancebolabs.sushiclash.testutil.TestGameStates
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
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
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 3))
        val viewModel = CounterViewModel(
            gameRepository,
            FakeOnboardingRepository(completed = true),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AppStartupState.ActiveGame, state.startupState)
        assertEquals(3, state.soloCount)
        assertEquals(1, gameRepository.restoreGameStateCallCount)
    }

    @Test
    fun givenInvalidPersistedGameOnLaunch_whenInitialized_thenRecoversWithoutTouchingOnboarding() = runTest {
        val invalidState = GameState(
            hasActiveGame = true,
            gameMode = GameMode.GROUP,
            players = emptyList(),
        )
        val gameRepository = FakeGameRepository(invalidState)
        val onboardingRepository = FakeOnboardingRepository(completed = true)
        val viewModel = CounterViewModel(gameRepository, onboardingRepository)

        advanceUntilIdle()

        assertEquals(AppStartupState.NoActiveGame, viewModel.uiState.value.startupState)
        assertFalse(viewModel.uiState.value.gameState.hasActiveGame)
        assertEquals(1, gameRepository.restoreGameStateCallCount)
        assertEquals(1, gameRepository.clearActiveGameCallCount)
        assertEquals(0, onboardingRepository.setOnboardingCompletedCallCount)
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
    fun givenSetupVisible_whenDismissed_thenHidesSetupWithoutChangingGameState() = runTest {
        val gameRepository = FakeGameRepository(GameState(hasActiveGame = false))
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onStartGameRequested()
        assertTrue(viewModel.uiState.value.showSetupDialog)

        viewModel.onSetupDismissed()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showSetupDialog)
        assertEquals(AppStartupState.NoActiveGame, state.startupState)
        assertFalse(state.gameState.hasActiveGame)
        assertEquals(0, gameRepository.completeSetupCallCount)
    }

    @Test
    fun givenActiveGame_whenSetupDismissedAfterOpen_thenActiveGameRemains() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 5))
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onStartGameRequested()
        viewModel.onSetupDismissed()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showSetupDialog)
        assertEquals(AppStartupState.ActiveGame, state.startupState)
        assertEquals(5, state.soloCount)
    }

    @Test
    fun givenSetupConfirmed_whenStartingSoloGame_thenActivatesGameAndHidesSetup() = runTest {
        val gameRepository = FakeGameRepository(GameState(hasActiveGame = false))
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
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
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onSoloSushiTapped()
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.soloCount)
    }

    @Test
    fun givenNoActiveGame_whenSushiTapped_thenCounterDoesNotIncrement() = runTest {
        val gameRepository = FakeGameRepository(GameState(hasActiveGame = false))
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
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
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
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
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onFinishGameRequested()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AppStartupState.ActiveGame, state.startupState)
        assertTrue(state.showFinishGameDialog)
        assertTrue(state.gameState.hasActiveGame)
        assertEquals(9, state.soloCount)
        assertEquals(0, gameRepository.finishGameWithSavingCallCount)
        assertEquals(0, gameRepository.clearActiveGameCallCount)
    }

    @Test
    fun givenFinishDialog_whenSaving_thenPersistsHistoryClearsGameAndClosesDialog() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 9))
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onFinishGameRequested()
        advanceUntilIdle()
        viewModel.onFinishGameWithSaving()
        advanceUntilIdle()

        assertEquals(1, gameRepository.finishGameWithSavingCallCount)
        assertEquals(1, gameRepository.clearActiveGameCallCount)
        assertFalse(viewModel.uiState.value.showFinishGameDialog)
        assertEquals(AppStartupState.NoActiveGame, viewModel.uiState.value.startupState)
        assertFalse(viewModel.uiState.value.gameState.hasActiveGame)
        assertFalse(viewModel.uiState.value.isFinishGameSaving)
        assertFalse(viewModel.uiState.value.finishGameSaveError)
    }

    @Test
    fun givenFinishDialog_whenSavingReturnsNoActiveGame_thenClosesDialog() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 9)).apply {
            finishGameWithSavingResults += FinishGameResult.NoActiveGame
        }
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()
        viewModel.onFinishGameRequested()
        gameRepository.setGameState(GameState(hasActiveGame = false))

        viewModel.onFinishGameWithSaving()
        advanceUntilIdle()

        assertEquals(1, gameRepository.finishGameWithSavingCallCount)
        assertFalse(viewModel.uiState.value.showFinishGameDialog)
        assertEquals(AppStartupState.NoActiveGame, viewModel.uiState.value.startupState)
        assertFalse(viewModel.uiState.value.isFinishGameSaving)
        assertFalse(viewModel.uiState.value.finishGameSaveError)
    }

    @Test
    fun givenFinishDialog_whenSavingIsRequestedConcurrently_thenRepositoryIsInvokedOnce() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 9))
        val finishGate = CompletableDeferred<Unit>()
        gameRepository.finishGameWithSavingGate = finishGate
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()
        viewModel.onFinishGameRequested()

        viewModel.onFinishGameWithSaving()
        viewModel.onFinishGameWithSaving()
        runCurrent()

        assertEquals(1, gameRepository.finishGameWithSavingCallCount)
        assertTrue(viewModel.uiState.value.isFinishGameSaving)

        finishGate.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun givenFinishDialog_whenSavingFails_thenKeepsGameAndExposesRecoverableError() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 9)).apply {
            finishGameWithSavingResults += FinishGameResult.Failure(IllegalStateException("test"))
        }
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()
        viewModel.onFinishGameRequested()

        viewModel.onFinishGameWithSaving()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, gameRepository.finishGameWithSavingCallCount)
        assertTrue(state.gameState.hasActiveGame)
        assertEquals(AppStartupState.ActiveGame, state.startupState)
        assertTrue(state.showFinishGameDialog)
        assertFalse(state.isFinishGameSaving)
        assertTrue(state.finishGameSaveError)
    }

    @Test
    fun givenPreviousSaveFailure_whenRetrySucceeds_thenFinishesExactlyOnceMore() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 9)).apply {
            finishGameWithSavingResults += FinishGameResult.Failure(IllegalStateException("test"))
            finishGameWithSavingResults += FinishGameResult.Success
        }
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()
        viewModel.onFinishGameRequested()
        viewModel.onFinishGameWithSaving()
        advanceUntilIdle()

        val retryGate = CompletableDeferred<Unit>()
        gameRepository.finishGameWithSavingGate = retryGate
        viewModel.onFinishGameWithSaving()
        runCurrent()

        assertEquals(2, gameRepository.finishGameWithSavingCallCount)
        assertTrue(viewModel.uiState.value.isFinishGameSaving)
        assertFalse(viewModel.uiState.value.finishGameSaveError)

        retryGate.complete(Unit)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showFinishGameDialog)
        assertEquals(AppStartupState.NoActiveGame, viewModel.uiState.value.startupState)
        assertFalse(viewModel.uiState.value.finishGameSaveError)
    }

    @Test
    fun givenSavingInProgress_whenFinishActionsAreRequested_thenTheyAreIgnored() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 9))
        val finishGate = CompletableDeferred<Unit>()
        gameRepository.finishGameWithSavingGate = finishGate
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()
        viewModel.onFinishGameRequested()
        viewModel.onFinishGameWithSaving()
        runCurrent()

        viewModel.onFinishGameCancelled()
        viewModel.onFinishGameWithoutSaving()
        viewModel.onFinishGameWithSaving()
        runCurrent()

        assertEquals(1, gameRepository.finishGameWithSavingCallCount)
        assertEquals(0, gameRepository.finishGameWithoutSavingCallCount)
        assertTrue(viewModel.uiState.value.showFinishGameDialog)
        assertTrue(viewModel.uiState.value.isFinishGameSaving)

        finishGate.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun givenFinishDialog_whenNotSaving_thenClearsGameWithoutHistory() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 9))
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onFinishGameRequested()
        advanceUntilIdle()
        viewModel.onFinishGameWithoutSaving()
        advanceUntilIdle()

        assertEquals(0, gameRepository.finishGameWithSavingCallCount)
        assertEquals(1, gameRepository.finishGameWithoutSavingCallCount)
        assertEquals(1, gameRepository.clearActiveGameCallCount)
        assertFalse(viewModel.uiState.value.showFinishGameDialog)
        assertEquals(AppStartupState.NoActiveGame, viewModel.uiState.value.startupState)
        assertFalse(viewModel.uiState.value.gameState.hasActiveGame)
        assertFalse(viewModel.uiState.value.isFinishGameSaving)
        assertFalse(viewModel.uiState.value.finishGameSaveError)
    }

    @Test
    fun givenFinishDialog_whenNotSavingFails_thenKeepsGameAndExposesError() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 9)).apply {
            finishGameWithoutSavingResults += FinishGameResult.Failure(IllegalStateException("test"))
        }
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()
        viewModel.onFinishGameRequested()

        viewModel.onFinishGameWithoutSaving()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, gameRepository.finishGameWithSavingCallCount)
        assertEquals(1, gameRepository.finishGameWithoutSavingCallCount)
        assertTrue(state.gameState.hasActiveGame)
        assertEquals(AppStartupState.ActiveGame, state.startupState)
        assertTrue(state.showFinishGameDialog)
        assertFalse(state.isFinishGameSaving)
        assertTrue(state.finishGameSaveError)
    }

    @Test
    fun givenFinishDialog_whenCancelled_thenKeepsActiveGameUnchanged() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 9))
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onFinishGameRequested()
        advanceUntilIdle()
        viewModel.onFinishGameCancelled()

        val state = viewModel.uiState.value
        assertFalse(state.showFinishGameDialog)
        assertEquals(AppStartupState.ActiveGame, state.startupState)
        assertTrue(state.gameState.hasActiveGame)
        assertEquals(9, state.soloCount)
        assertEquals(0, gameRepository.finishGameWithSavingCallCount)
        assertEquals(0, gameRepository.finishGameWithoutSavingCallCount)
        assertEquals(0, gameRepository.clearActiveGameCallCount)
        assertFalse(state.finishGameSaveError)
    }

    @Test
    fun givenFinishFailure_whenCancelled_thenClosesDialogAndClearsErrorWithoutPersistence() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 9)).apply {
            finishGameWithSavingResults += FinishGameResult.Failure(IllegalStateException("test"))
        }
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()
        viewModel.onFinishGameRequested()
        viewModel.onFinishGameWithSaving()
        advanceUntilIdle()
        val finishCallsBeforeCancel = gameRepository.finishGameWithSavingCallCount

        viewModel.onFinishGameCancelled()

        assertFalse(viewModel.uiState.value.showFinishGameDialog)
        assertFalse(viewModel.uiState.value.finishGameSaveError)
        assertEquals(finishCallsBeforeCancel, gameRepository.finishGameWithSavingCallCount)
        assertEquals(0, gameRepository.finishGameWithoutSavingCallCount)
    }

    @Test
    fun givenFinishDialog_whenSavingIsCancelled_thenRestoresInteractiveState() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 9)).apply {
            finishGameWithSavingThrowable = CancellationException("test")
        }
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()
        viewModel.onFinishGameRequested()

        viewModel.onFinishGameWithSaving()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, gameRepository.finishGameWithSavingCallCount)
        assertTrue(state.gameState.hasActiveGame)
        assertEquals(AppStartupState.ActiveGame, state.startupState)
        assertTrue(state.showFinishGameDialog)
        assertFalse(state.isFinishGameSaving)
        assertFalse(state.finishGameSaveError)
    }

    @Test
    fun givenFinishDialogVisible_whenViewModelRecreated_thenActiveGameIsRestoredWithoutDialog() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 7))
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onFinishGameRequested()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showFinishGameDialog)
        assertTrue(viewModel.uiState.value.gameState.hasActiveGame)

        // Dialog visibility is in-memory only; closing the app must not clear persisted active state.
        val restartedViewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
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
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onSoloSushiTapped()
        advanceUntilIdle()

        assertEquals(RouletteTriggerEvent.Solo(count = 5), viewModel.uiState.value.rouletteTriggerEvent)
    }

    @Test
    fun givenUnreadableOnboarding_whenInitialized_thenLeavesLoadingWithoutHanging() = runTest {
        val onboardingRepository = FakeOnboardingRepository(completed = false).apply {
            setHasCompletedOnboardingUnreadable()
        }
        val viewModel = CounterViewModel(
            gameRepository = FakeGameRepository(GameState(hasActiveGame = false)),
            onboardingRepository = onboardingRepository,
        )
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(AppStartupState.NoActiveGame, state.startupState)
        assertTrue(state.persistenceError)
        assertEquals(0, onboardingRepository.setOnboardingCompletedCallCount)
    }

    @Test
    fun givenUnavailableRestore_whenInitialized_thenShowsPersistenceErrorWithoutCompletingSetup() = runTest {
        val gameRepository = FakeGameRepository(GameState(hasActiveGame = false)).apply {
            restoreResult = RestoreGameResult.Unavailable
        }
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AppStartupState.NoActiveGame, state.startupState)
        assertTrue(state.persistenceError)
        assertEquals(0, gameRepository.completeSetupCallCount)
        assertNull(gameRepository.lastSetupConfig)
    }

    @Test
    fun givenUnavailableRestore_whenRetrySucceeds_thenRestoresActiveGame() = runTest {
        val restoredState = TestGameStates.soloActive(count = 6)
        val gameRepository = FakeGameRepository(GameState(hasActiveGame = false)).apply {
            restoreResult = RestoreGameResult.Unavailable
        }
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()

        gameRepository.restoreResult = RestoreGameResult.Restored(restoredState)
        gameRepository.setGameState(restoredState)
        viewModel.onPersistenceRetry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AppStartupState.ActiveGame, state.startupState)
        assertFalse(state.persistenceError)
        assertEquals(6, state.soloCount)
        assertEquals(2, gameRepository.restoreGameStateCallCount)
    }

    @Test
    fun givenIncrementThrows_whenSushiTapped_thenDoesNotIncrementOrEmitRoulette() = runTest {
        val gameRepository = FakeGameRepository(
            TestGameStates.soloActive(
                count = 4,
                randomRouletteEnabled = true,
                fixedThreshold = 5,
            ),
        ).apply {
            incrementThrowable = IOException("disk")
        }
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onSoloSushiTapped()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(4, state.soloCount)
        assertNull(state.rouletteTriggerEvent)
        assertTrue(state.persistenceError)
        assertEquals(AppStartupState.ActiveGame, state.startupState)
    }

    @Test
    fun givenCompleteSetupThrows_whenSetupConfirmed_thenDoesNotActivateGame() = runTest {
        val gameRepository = FakeGameRepository(GameState(hasActiveGame = false)).apply {
            completeSetupThrowable = IOException("disk")
        }
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()
        viewModel.onStartGameRequested()

        viewModel.onSetupConfirmed(GameSetupConfig(gameMode = GameMode.SOLO))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AppStartupState.NoActiveGame, state.startupState)
        assertTrue(state.persistenceError)
        assertFalse(state.showSetupDialog)
        assertNull(state.rouletteTriggerEvent)
        assertFalse(state.gameState.hasActiveGame)
        assertNull(gameRepository.lastSetupConfig)
    }

    @Test
    fun givenIncrementThrows_whenRetrySucceeds_thenRetriesIncrementWithoutRestoring() = runTest {
        val gameRepository = FakeGameRepository(
            TestGameStates.soloActive(count = 4),
        ).apply {
            incrementThrowable = IOException("disk")
        }
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()
        val restoreCountAfterInit = gameRepository.restoreGameStateCallCount

        viewModel.onSoloSushiTapped()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.persistenceError)
        assertEquals(4, viewModel.uiState.value.soloCount)
        assertEquals(1, gameRepository.incrementPlayerCountCallCount)

        gameRepository.incrementThrowable = null
        viewModel.onPersistenceRetry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.persistenceError)
        assertEquals(5, state.soloCount)
        assertEquals(2, gameRepository.incrementPlayerCountCallCount)
        assertEquals(restoreCountAfterInit, gameRepository.restoreGameStateCallCount)
    }

    @Test
    fun givenCompleteSetupThrows_whenRetrySucceeds_thenCompletesSetupWithoutRestoring() = runTest {
        val config = GameSetupConfig(gameMode = GameMode.SOLO)
        val gameRepository = FakeGameRepository(GameState(hasActiveGame = false)).apply {
            completeSetupThrowable = IOException("disk")
        }
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()
        viewModel.onStartGameRequested()

        viewModel.onSetupConfirmed(config)
        advanceUntilIdle()

        val failed = viewModel.uiState.value
        assertTrue(failed.persistenceError)
        assertFalse(failed.showSetupDialog)
        assertEquals(AppStartupState.NoActiveGame, failed.startupState)
        val restoreCountAfterSetup = gameRepository.restoreGameStateCallCount

        gameRepository.completeSetupThrowable = null
        viewModel.onPersistenceRetry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AppStartupState.ActiveGame, state.startupState)
        assertFalse(state.persistenceError)
        assertFalse(state.showSetupDialog)
        assertEquals(GameMode.SOLO, state.gameMode)
        assertEquals(config, gameRepository.lastSetupConfig)
        assertEquals(2, gameRepository.completeSetupCallCount)
        assertEquals(restoreCountAfterSetup, gameRepository.restoreGameStateCallCount)
    }

    @Test
    fun givenResetSoloThrows_whenResetConfirmed_thenShowsPersistenceError() = runTest {
        val gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 4)).apply {
            resetSoloCountThrowable = IOException("disk")
        }
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onResetSoloCountConfirmed()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(4, state.soloCount)
        assertTrue(state.persistenceError)
    }

    @Test
    fun givenResetPlayerThrows_whenResetConfirmed_thenShowsPersistenceError() = runTest {
        val players = listOf(
            Player(id = "p1", name = "Ana", sushiCount = 5),
            Player(id = "p2", name = "Luis", sushiCount = 2),
        )
        val gameRepository = FakeGameRepository(TestGameStates.groupActive(players)).apply {
            resetPlayerCountThrowable = IOException("disk")
        }
        val viewModel = CounterViewModel(gameRepository, FakeOnboardingRepository())
        advanceUntilIdle()

        viewModel.onPlayerResetRequested("p1")
        viewModel.onPlayerResetConfirmed()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(5, state.players.first { it.id == "p1" }.sushiCount)
        assertTrue(state.persistenceError)
    }

    private fun createViewModel(initialState: GameState): CounterViewModel {
        return CounterViewModel(
            FakeGameRepository(initialState),
            FakeOnboardingRepository(completed = true),
        )
    }
}
