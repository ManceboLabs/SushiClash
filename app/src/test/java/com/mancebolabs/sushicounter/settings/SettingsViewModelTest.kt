package com.mancebolabs.sushicounter.settings

import app.cash.turbine.test
import com.mancebolabs.sushicounter.domain.model.AppThemeMode
import com.mancebolabs.sushicounter.feature.settings.SettingsViewModel
import com.mancebolabs.sushicounter.testutil.FakeHistoryRepository
import com.mancebolabs.sushicounter.testutil.FakeThemeRepository
import com.mancebolabs.sushicounter.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    fun givenClearHistoryRequested_whenConfirmed_thenHistoryRepositoryIsCleared() = runTest {
        val historyRepository = FakeHistoryRepository()
        val viewModel = SettingsViewModel(FakeThemeRepository(), historyRepository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onClearHistoryRequested()
            assertTrue(awaitItem().showClearHistoryDialog)

            viewModel.onClearHistoryConfirmed()
            assertFalse(awaitItem().showClearHistoryDialog)
            assertEquals(1, historyRepository.clearHistoryCallCount)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
