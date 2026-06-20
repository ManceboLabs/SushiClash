package com.mancebolabs.sushiclash.onboarding

import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.feature.counter.AppStartupState
import com.mancebolabs.sushiclash.feature.counter.CounterViewModel
import com.mancebolabs.sushiclash.testutil.FakeGameRepository
import com.mancebolabs.sushiclash.testutil.FakeHistoryRepository
import com.mancebolabs.sushiclash.testutil.FakeOnboardingRepository
import com.mancebolabs.sushiclash.testutil.MainDispatcherRule
import com.mancebolabs.sushiclash.testutil.TestGameStates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingStartupTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenOnboardingNotCompleted_whenCounterInitialized_thenStaysLoadingUntilCompleted() = runTest {
        val onboardingRepository = FakeOnboardingRepository(completed = false)
        val viewModel = CounterViewModel(
            gameRepository = FakeGameRepository(GameState(hasActiveGame = false)),
            historyRepository = FakeHistoryRepository(),
            onboardingRepository = onboardingRepository,
        )
        advanceUntilIdle()

        assertEquals(AppStartupState.Loading, viewModel.uiState.value.startupState)
        assertFalse(viewModel.uiState.value.showSetupDialog)

        onboardingRepository.setOnboardingCompleted()
        advanceUntilIdle()

        assertEquals(AppStartupState.NoActiveGame, viewModel.uiState.value.startupState)
        assertFalse(viewModel.uiState.value.showSetupDialog)
    }

    @Test
    fun givenOnboardingCompleted_whenCounterInitialized_thenSkipsLoadingForGameplayState() = runTest {
        val viewModel = CounterViewModel(
            gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 4)),
            historyRepository = FakeHistoryRepository(),
            onboardingRepository = FakeOnboardingRepository(completed = true),
        )
        advanceUntilIdle()

        assertEquals(AppStartupState.ActiveGame, viewModel.uiState.value.startupState)
    }

    @Test
    fun givenOnboardingCompleted_whenSkippedOrFinished_thenPersistsCompletionFlag() = runTest {
        val onboardingRepository = FakeOnboardingRepository(completed = false)

        onboardingRepository.setOnboardingCompleted()

        assertTrue(onboardingRepository.hasCompletedOnboarding.first())
        assertEquals(1, onboardingRepository.setOnboardingCompletedCallCount)
    }

    @Test
    fun givenActiveGame_whenOnboardingCompletes_thenActiveGameRemainsPrioritizedOverNoActiveGame() = runTest {
        val onboardingRepository = FakeOnboardingRepository(completed = false)
        val viewModel = CounterViewModel(
            gameRepository = FakeGameRepository(TestGameStates.soloActive(count = 9)),
            historyRepository = FakeHistoryRepository(),
            onboardingRepository = onboardingRepository,
        )
        advanceUntilIdle()
        assertEquals(AppStartupState.Loading, viewModel.uiState.value.startupState)

        onboardingRepository.setOnboardingCompleted()
        advanceUntilIdle()

        assertEquals(AppStartupState.ActiveGame, viewModel.uiState.value.startupState)
        assertEquals(9, viewModel.uiState.value.soloCount)
    }
}
