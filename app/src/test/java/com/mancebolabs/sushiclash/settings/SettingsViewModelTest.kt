package com.mancebolabs.sushiclash.settings

import app.cash.turbine.test
import com.mancebolabs.sushiclash.domain.model.AppThemeMode
import com.mancebolabs.sushiclash.domain.model.GroupGameHistoryEntry
import com.mancebolabs.sushiclash.domain.model.SoloGameHistoryEntry
import com.mancebolabs.sushiclash.feature.settings.SettingsViewModel
import com.mancebolabs.sushiclash.navigation.OnboardingSource
import com.mancebolabs.sushiclash.navigation.SushiDestination
import com.mancebolabs.sushiclash.testutil.FakeHistoryRepository
import com.mancebolabs.sushiclash.testutil.FakeThemeRepository
import com.mancebolabs.sushiclash.testutil.MainDispatcherRule
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
        )

        viewModel.uiState.test {
            assertEquals(AppThemeMode.LIGHT, awaitItem().themeMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun givenDarkThemeSelected_whenUpdatingTheme_thenStateReflectsDarkMode() = runTest {
        val themeRepository = FakeThemeRepository(AppThemeMode.LIGHT)
        val viewModel = SettingsViewModel(themeRepository, FakeHistoryRepository())

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
        val viewModel = SettingsViewModel(FakeThemeRepository(), historyRepository)

        viewModel.onClearHistoryRequested()
        viewModel.onClearHistoryConfirmed()

        assertEquals(1, historyRepository.clearHistoryCallCount)
        assertTrue(historyRepository.soloHistory.first().isEmpty())
        assertTrue(historyRepository.groupHistory.first().isEmpty())
    }

    @Test
    fun givenClearHistoryRequested_whenConfirmed_thenDialogIsDismissed() = runTest {
        val historyRepository = FakeHistoryRepository()
        val viewModel = SettingsViewModel(FakeThemeRepository(), historyRepository)

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
        val viewModel = SettingsViewModel(FakeThemeRepository(), historyRepository)

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
        val viewModel = SettingsViewModel(themeRepository, FakeHistoryRepository())

        viewModel.onClearHistoryConfirmed()

        assertEquals(AppThemeMode.DARK, themeRepository.themeMode.first())
    }

    @Test
    fun givenSettingsTutorialRoute_whenResolved_thenUsesSettingsOnboardingSource() {
        val route = SushiDestination.Onboarding.route(OnboardingSource.SETTINGS)

        assertEquals("onboarding/SETTINGS", route)
    }
}
