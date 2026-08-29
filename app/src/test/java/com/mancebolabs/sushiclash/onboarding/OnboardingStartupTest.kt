package com.mancebolabs.sushiclash.onboarding

import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import com.mancebolabs.sushiclash.feature.counter.AppStartupState
import com.mancebolabs.sushiclash.feature.counter.CounterViewModel
import com.mancebolabs.sushiclash.navigation.resolveOnboardingCompleted
import com.mancebolabs.sushiclash.testutil.FakeFeedbackSettingsRepository
import com.mancebolabs.sushiclash.testutil.FakeFrequentPlayersRepository
import com.mancebolabs.sushiclash.testutil.FakeAchievementRepository
import com.mancebolabs.sushiclash.testutil.FakeGameRepository
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
            onboardingRepository = onboardingRepository,
            feedbackSettingsRepository = FakeFeedbackSettingsRepository(),
            achievementRepository = FakeAchievementRepository(),
            frequentPlayersRepository = FakeFrequentPlayersRepository(),
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
            onboardingRepository = FakeOnboardingRepository(completed = true),
            feedbackSettingsRepository = FakeFeedbackSettingsRepository(),
            achievementRepository = FakeAchievementRepository(),
            frequentPlayersRepository = FakeFrequentPlayersRepository(),
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
            onboardingRepository = onboardingRepository,
            feedbackSettingsRepository = FakeFeedbackSettingsRepository(),
            achievementRepository = FakeAchievementRepository(),
            frequentPlayersRepository = FakeFrequentPlayersRepository(),
        )
        advanceUntilIdle()
        assertEquals(AppStartupState.Loading, viewModel.uiState.value.startupState)

        onboardingRepository.setOnboardingCompleted()
        advanceUntilIdle()

        assertEquals(AppStartupState.ActiveGame, viewModel.uiState.value.startupState)
        assertEquals(9, viewModel.uiState.value.soloCount)
    }

    @Test
    fun givenMissingOnboarding_whenResolvingCompletion_thenShowsOnboarding() {
        assertEquals(false, resolveOnboardingCompleted(previous = null, state = PersistenceReadState.Missing))
        assertEquals(true, resolveOnboardingCompleted(previous = true, state = PersistenceReadState.Missing))
    }

    @Test
    fun givenOnboardingData_whenResolvingCompletion_thenUsesPersistedValue() {
        assertEquals(true, resolveOnboardingCompleted(previous = null, state = PersistenceReadState.Data(true)))
        assertEquals(false, resolveOnboardingCompleted(previous = true, state = PersistenceReadState.Data(false)))
    }

    @Test
    fun givenUnreadableOnboardingWithoutPrevious_whenResolvingCompletion_thenKeepsStartupLoading() {
        assertEquals(null, resolveOnboardingCompleted(previous = null, state = PersistenceReadState.Corrupted))
        assertEquals(null, resolveOnboardingCompleted(previous = null, state = PersistenceReadState.Unavailable))
    }

    @Test
    fun givenFirstLaunchReadFailureThenMissing_whenResolvingCompletion_thenShowsOnboarding() {
        var resolved = resolveOnboardingCompleted(previous = null, state = PersistenceReadState.Unavailable)
        assertEquals(null, resolved)

        resolved = resolveOnboardingCompleted(resolved, PersistenceReadState.Missing)
        assertEquals(false, resolved)
    }

    @Test
    fun givenCompletedOnboarding_whenTransientReadFailure_thenDoesNotFlashOnboarding() {
        var resolved = resolveOnboardingCompleted(previous = null, state = PersistenceReadState.Data(true))
        assertEquals(true, resolved)

        resolved = resolveOnboardingCompleted(resolved, PersistenceReadState.Unavailable)
        assertEquals(true, resolved)

        resolved = resolveOnboardingCompleted(resolved, PersistenceReadState.Missing)
        assertEquals(true, resolved)
    }

    @Test
    fun givenUnreadableOnboarding_whenResolvingCompletion_thenKeepsLastKnownValue() {
        assertEquals(true, resolveOnboardingCompleted(previous = true, state = PersistenceReadState.Corrupted))
        assertEquals(true, resolveOnboardingCompleted(previous = true, state = PersistenceReadState.Unavailable))
        assertEquals(false, resolveOnboardingCompleted(previous = false, state = PersistenceReadState.Corrupted))
        assertEquals(false, resolveOnboardingCompleted(previous = false, state = PersistenceReadState.Unavailable))
    }
}
