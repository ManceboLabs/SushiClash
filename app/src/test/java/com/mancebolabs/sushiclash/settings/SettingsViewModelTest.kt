package com.mancebolabs.sushiclash.settings

import app.cash.turbine.test
import com.mancebolabs.sushiclash.domain.model.AppThemeMode
import com.mancebolabs.sushiclash.domain.model.GroupGameHistoryEntry
import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import com.mancebolabs.sushiclash.domain.model.SoloGameHistoryEntry
import com.mancebolabs.sushiclash.feature.settings.SettingsViewModel
import com.mancebolabs.sushiclash.navigation.OnboardingSource
import com.mancebolabs.sushiclash.navigation.SushiDestination
import com.mancebolabs.sushiclash.testutil.FakeFeedbackSettingsRepository
import com.mancebolabs.sushiclash.testutil.FakeHistoryRepository
import com.mancebolabs.sushiclash.testutil.FakeThemeRepository
import com.mancebolabs.sushiclash.testutil.MainDispatcherRule
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenPersistedLightTheme_whenObservingState_thenThemeModeIsLight() = runTest {
        val viewModel = SettingsViewModel(
            themeRepository = FakeThemeRepository(AppThemeMode.LIGHT),
            historyRepository = FakeHistoryRepository(),
            feedbackSettingsRepository = FakeFeedbackSettingsRepository(),
        )

        viewModel.uiState.test {
            assertEquals(AppThemeMode.LIGHT, awaitItem().themeMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenDarkThemeSelected_whenUpdatingTheme_thenStateReflectsDarkMode() = runTest {
        val themeRepository = FakeThemeRepository(AppThemeMode.LIGHT)
        val viewModel = SettingsViewModel(themeRepository, FakeHistoryRepository(), FakeFeedbackSettingsRepository())

        viewModel.uiState.test {
            awaitItem()
            viewModel.onThemeModeSelected(AppThemeMode.DARK)
            assertEquals(AppThemeMode.DARK, awaitItem().themeMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenClearHistoryRequested_whenConfirmed_thenSoloAndGroupHistoryAreCleared() = runTest {
        val historyRepository = FakeHistoryRepository()
        historyRepository.setSoloHistory(
            listOf(
                SoloGameHistoryEntry(
                    id = "solo-1",
                    date = 1L,
                    totalSushi = 5,
                    randomRouletteEnabled = false,
                    randomRouletteMode = null,
                ),
            ),
        )
        historyRepository.setGroupHistory(
            listOf(
                GroupGameHistoryEntry(
                    id = "group-1",
                    date = 2L,
                    players = emptyList(),
                    randomRouletteEnabled = false,
                    randomRouletteMode = null,
                ),
            ),
        )
        val viewModel = SettingsViewModel(FakeThemeRepository(), historyRepository, FakeFeedbackSettingsRepository())

        viewModel.onClearHistoryRequested()
        viewModel.onClearHistoryConfirmed()

        assertEquals(1, historyRepository.clearHistoryCallCount)
        assertTrue(isClearedHistory(historyRepository.soloHistory.first()))
        assertTrue(isClearedHistory(historyRepository.groupHistory.first()))
    }

    @Test
    fun givenClearHistoryRequested_whenConfirmed_thenDialogIsDismissed() = runTest {
        val historyRepository = FakeHistoryRepository()
        val viewModel = SettingsViewModel(FakeThemeRepository(), historyRepository, FakeFeedbackSettingsRepository())

        viewModel.uiState.test {
            awaitItem()
            viewModel.onClearHistoryRequested()
            assertTrue(awaitItem().showClearHistoryDialog)

            viewModel.onClearHistoryConfirmed()
            assertFalse(awaitItem().showClearHistoryDialog)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenClearHistoryRequested_whenDismissed_thenHistoryIsNotCleared() = runTest {
        val historyRepository = FakeHistoryRepository()
        val viewModel = SettingsViewModel(FakeThemeRepository(), historyRepository, FakeFeedbackSettingsRepository())

        viewModel.uiState.test {
            awaitItem()
            viewModel.onClearHistoryRequested()
            assertTrue(awaitItem().showClearHistoryDialog)

            viewModel.onClearHistoryDismissed()
            assertFalse(awaitItem().showClearHistoryDialog)
            assertEquals(0, historyRepository.clearHistoryCallCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenClearHistoryConfirmed_whenThemeWasDark_thenThemeRemainsDark() = runTest {
        val themeRepository = FakeThemeRepository(AppThemeMode.DARK)
        val viewModel = SettingsViewModel(themeRepository, FakeHistoryRepository(), FakeFeedbackSettingsRepository())

        viewModel.onClearHistoryConfirmed()

        assertEquals(AppThemeMode.DARK, themeRepository.themeMode.first())
    }

    @Test
    fun givenDefaultFeedbackSettings_whenObservingState_thenSoundAndVibrationEnabled() = runTest {
        val viewModel = SettingsViewModel(
            themeRepository = FakeThemeRepository(),
            historyRepository = FakeHistoryRepository(),
            feedbackSettingsRepository = FakeFeedbackSettingsRepository(),
        )

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.soundEnabled)
            assertTrue(state.vibrationEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenSoundDisabled_whenUpdating_thenStateReflectsChange() = runTest {
        val feedbackSettingsRepository = FakeFeedbackSettingsRepository()
        val viewModel = SettingsViewModel(
            FakeThemeRepository(),
            FakeHistoryRepository(),
            feedbackSettingsRepository,
        )

        viewModel.uiState.test {
            awaitItem()
            viewModel.onSoundEnabledChanged(false)
            assertFalse(awaitItem().soundEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenSoundWriteFails_whenDisabling_thenKeepsPreviousValueAndShowsError() = runTest {
        val feedbackSettingsRepository = FakeFeedbackSettingsRepository().apply {
            setSoundEnabledThrow = IOException("disk")
        }
        val viewModel = SettingsViewModel(
            FakeThemeRepository(),
            FakeHistoryRepository(),
            feedbackSettingsRepository,
        )

        viewModel.uiState.test {
            assertTrue(awaitItem().soundEnabled)
            viewModel.onSoundEnabledChanged(false)
            val state = awaitItem()
            assertTrue(state.soundEnabled)
            assertTrue(state.persistenceError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenSoundWriteFails_whenRetrySucceeds_thenAppliesDisabledSound() = runTest {
        val feedbackSettingsRepository = FakeFeedbackSettingsRepository().apply {
            setSoundEnabledThrow = IOException("disk")
        }
        val viewModel = SettingsViewModel(
            FakeThemeRepository(),
            FakeHistoryRepository(),
            feedbackSettingsRepository,
        )

        viewModel.uiState.test {
            awaitItem()
            viewModel.onSoundEnabledChanged(false)
            assertTrue(awaitItem().persistenceError)

            feedbackSettingsRepository.setSoundEnabledThrow = null
            viewModel.onPersistenceRetry()
            val state = expectMostRecentItem()
            assertFalse(state.soundEnabled)
            assertFalse(state.persistenceError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenSettingsTutorialRoute_whenResolved_thenUsesSettingsOnboardingSource() {
        val route = SushiDestination.Onboarding.route(OnboardingSource.SETTINGS)

        assertEquals("onboarding/SETTINGS", route)
    }

    @Test
    fun givenThemeWriteFails_whenSelectingDark_thenKeepsPreviousThemeAndShowsError() = runTest {
        val themeRepository = FakeThemeRepository(AppThemeMode.LIGHT).apply {
            setThemeModeThrow = IOException("disk")
        }
        val viewModel = SettingsViewModel(themeRepository, FakeHistoryRepository(), FakeFeedbackSettingsRepository())

        viewModel.uiState.test {
            assertEquals(AppThemeMode.LIGHT, awaitItem().themeMode)
            viewModel.onThemeModeSelected(AppThemeMode.DARK)
            val state = awaitItem()
            assertEquals(AppThemeMode.LIGHT, state.themeMode)
            assertTrue(state.persistenceError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenThemeWriteFails_whenRetrySucceeds_thenAppliesDarkTheme() = runTest {
        val themeRepository = FakeThemeRepository(AppThemeMode.LIGHT).apply {
            setThemeModeThrow = IOException("disk")
        }
        val viewModel = SettingsViewModel(themeRepository, FakeHistoryRepository(), FakeFeedbackSettingsRepository())

        viewModel.uiState.test {
            awaitItem()
            viewModel.onThemeModeSelected(AppThemeMode.DARK)
            assertTrue(awaitItem().persistenceError)

            themeRepository.setThemeModeThrow = null
            viewModel.onPersistenceRetry()
            val state = expectMostRecentItem()
            assertEquals(AppThemeMode.DARK, state.themeMode)
            assertFalse(state.persistenceError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenClearHistoryThrows_whenConfirmed_thenKeepsDialogAndHistory() = runTest {
        val historyRepository = FakeHistoryRepository()
        val soloEntry = SoloGameHistoryEntry(
            id = "solo-1",
            date = 1L,
            totalSushi = 5,
            randomRouletteEnabled = false,
            randomRouletteMode = null,
        )
        historyRepository.setSoloHistory(listOf(soloEntry))
        historyRepository.clearHistoryThrowable = IOException("disk")
        val viewModel = SettingsViewModel(FakeThemeRepository(), historyRepository, FakeFeedbackSettingsRepository())

        viewModel.uiState.test {
            awaitItem()
            viewModel.onClearHistoryRequested()
            assertTrue(awaitItem().showClearHistoryDialog)

            viewModel.onClearHistoryConfirmed()
            val state = expectMostRecentItem()
            assertTrue(state.showClearHistoryDialog)
            assertTrue(state.persistenceError)
            assertEquals(1, historyRepository.clearHistoryCallCount)
            assertEquals(
                PersistenceReadState.Data(listOf(soloEntry)),
                historyRepository.soloHistory.first(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenClearHistoryThrows_whenRetrySucceeds_thenHistoryClearedDialogClosedAndErrorCleared() = runTest {
        val historyRepository = FakeHistoryRepository()
        val soloEntry = SoloGameHistoryEntry(
            id = "solo-1",
            date = 1L,
            totalSushi = 5,
            randomRouletteEnabled = false,
            randomRouletteMode = null,
        )
        historyRepository.setSoloHistory(listOf(soloEntry))
        historyRepository.clearHistoryThrowable = IOException("disk")
        val viewModel = SettingsViewModel(FakeThemeRepository(), historyRepository, FakeFeedbackSettingsRepository())

        viewModel.uiState.test {
            awaitItem()
            viewModel.onClearHistoryRequested()
            assertTrue(awaitItem().showClearHistoryDialog)

            viewModel.onClearHistoryConfirmed()
            val failed = expectMostRecentItem()
            assertTrue(failed.showClearHistoryDialog)
            assertTrue(failed.persistenceError)

            historyRepository.clearHistoryThrowable = null
            viewModel.onPersistenceRetry()
            val retried = expectMostRecentItem()
            assertFalse(retried.showClearHistoryDialog)
            assertFalse(retried.persistenceError)
            assertEquals(2, historyRepository.clearHistoryCallCount)
            assertTrue(isClearedHistory(historyRepository.soloHistory.first()))
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun <T> isClearedHistory(state: PersistenceReadState<List<T>>): Boolean {
        return when (state) {
            PersistenceReadState.Missing -> true
            is PersistenceReadState.Data -> state.value.isEmpty()
            PersistenceReadState.Corrupted,
            PersistenceReadState.Unavailable -> false
        }
    }
}
