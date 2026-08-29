package com.mancebolabs.sushiclash.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mancebolabs.sushiclash.domain.model.AppLanguage
import com.mancebolabs.sushiclash.domain.model.AppThemeMode
import com.mancebolabs.sushiclash.domain.repository.AchievementRepository
import com.mancebolabs.sushiclash.domain.repository.FeedbackSettingsRepository
import com.mancebolabs.sushiclash.domain.repository.HistoryRepository
import com.mancebolabs.sushiclash.domain.repository.LanguageRepository
import com.mancebolabs.sushiclash.domain.repository.ThemeRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val activeAppLanguage: AppLanguage = AppLanguage.SYSTEM,
    val showLanguagePickerDialog: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val showClearHistoryDialog: Boolean = false,
    val showClearAchievementsDialog: Boolean = false,
    val persistenceError: Boolean = false,
    val isPersistenceRetrying: Boolean = false,
)

class SettingsViewModel(
    private val themeRepository: ThemeRepository,
    private val languageRepository: LanguageRepository,
    private val historyRepository: HistoryRepository,
    private val feedbackSettingsRepository: FeedbackSettingsRepository,
    private val achievementRepository: AchievementRepository,
) : ViewModel() {

    private val showLanguagePickerDialog = MutableStateFlow(false)

    private val showClearHistoryDialog = MutableStateFlow(false)
    private val showClearAchievementsDialog = MutableStateFlow(false)
    private val persistenceError = MutableStateFlow(false)
    private val isPersistenceRetrying = MutableStateFlow(false)
    private var pendingSettingsWrite: PendingSettingsWrite? = null

    val uiState: StateFlow<SettingsUiState> = combine(
        themeRepository.themeMode,
        languageRepository.appLanguage,
        showLanguagePickerDialog,
        combine(
            feedbackSettingsRepository.soundEnabled,
            feedbackSettingsRepository.vibrationEnabled,
            combine(
                showClearHistoryDialog,
                showClearAchievementsDialog,
                persistenceError,
                isPersistenceRetrying,
            ) { clearHistoryDialog, clearAchievementsDialog, hasError, isRetrying ->
                SettingsPersistenceUiState(
                    showClearHistoryDialog = clearHistoryDialog,
                    showClearAchievementsDialog = clearAchievementsDialog,
                    persistenceError = hasError,
                    isPersistenceRetrying = isRetrying,
                )
            },
        ) { soundEnabled, vibrationEnabled, persistence ->
            SettingsPreferencesUiState(
                soundEnabled = soundEnabled,
                vibrationEnabled = vibrationEnabled,
                persistence = persistence,
            )
        },
    ) { themeMode, activeLanguage, languageDialogVisible, preferences ->
        SettingsUiState(
            themeMode = themeMode,
            activeAppLanguage = activeLanguage,
            showLanguagePickerDialog = languageDialogVisible,
            soundEnabled = preferences.soundEnabled,
            vibrationEnabled = preferences.vibrationEnabled,
            showClearHistoryDialog = preferences.persistence.showClearHistoryDialog,
            showClearAchievementsDialog = preferences.persistence.showClearAchievementsDialog,
            persistenceError = preferences.persistence.persistenceError,
            isPersistenceRetrying = preferences.persistence.isPersistenceRetrying,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(
            activeAppLanguage = languageRepository.getAppLanguage(),
        ),
    )

    fun onThemeModeSelected(themeMode: AppThemeMode) {
        viewModelScope.launch {
            persistThemeMode(themeMode)
        }
    }

    fun onLanguagePickerRequested() {
        showLanguagePickerDialog.value = true
    }

    fun onLanguagePickerDismissed() {
        showLanguagePickerDialog.value = false
    }

    fun onLanguageSelected(language: AppLanguage) {
        showLanguagePickerDialog.value = false
        if (language == languageRepository.getAppLanguage()) return

        languageRepository.setAppLanguage(language)
    }

    fun onAppLanguageRefreshRequested() {
        languageRepository.refreshAppLanguage()
    }

    fun onSoundEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            persistSoundEnabled(enabled)
        }
    }

    fun onVibrationEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            persistVibrationEnabled(enabled)
        }
    }

    fun onClearHistoryRequested() {
        showClearHistoryDialog.value = true
    }

    fun onClearHistoryDismissed() {
        showClearHistoryDialog.value = false
    }

    fun onClearHistoryConfirmed() {
        viewModelScope.launch {
            persistClearHistory()
        }
    }

    fun onClearAchievementsRequested() {
        showClearAchievementsDialog.value = true
    }

    fun onClearAchievementsDismissed() {
        showClearAchievementsDialog.value = false
    }

    fun onClearAchievementsConfirmed() {
        viewModelScope.launch {
            persistClearAchievements()
        }
    }

    fun onPersistenceRetry() {
        val pending = pendingSettingsWrite ?: return
        if (!isPersistenceRetrying.compareAndSet(expect = false, update = true)) return
        viewModelScope.launch {
            try {
                when (pending) {
                    is PendingSettingsWrite.Theme -> persistThemeMode(pending.themeMode)
                    is PendingSettingsWrite.Sound -> persistSoundEnabled(pending.enabled)
                    is PendingSettingsWrite.Vibration -> persistVibrationEnabled(pending.enabled)
                    PendingSettingsWrite.ClearHistory -> persistClearHistory()
                    PendingSettingsWrite.ClearAchievements -> persistClearAchievements()
                }
            } finally {
                isPersistenceRetrying.value = false
            }
        }
    }

    private suspend fun persistClearHistory() {
        try {
            historyRepository.clearHistory()
            pendingSettingsWrite = null
            showClearHistoryDialog.value = false
            persistenceError.value = false
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: IOException) {
            pendingSettingsWrite = PendingSettingsWrite.ClearHistory
            persistenceError.value = true
        }
    }

    private suspend fun persistClearAchievements() {
        try {
            achievementRepository.clearAchievements()
            pendingSettingsWrite = null
            showClearAchievementsDialog.value = false
            persistenceError.value = false
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: IOException) {
            pendingSettingsWrite = PendingSettingsWrite.ClearAchievements
            persistenceError.value = true
        }
    }

    private suspend fun persistThemeMode(themeMode: AppThemeMode) {
        try {
            themeRepository.setThemeMode(themeMode)
            pendingSettingsWrite = null
            persistenceError.value = false
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: IOException) {
            pendingSettingsWrite = PendingSettingsWrite.Theme(themeMode)
            persistenceError.value = true
        }
    }

    private suspend fun persistSoundEnabled(enabled: Boolean) {
        try {
            feedbackSettingsRepository.setSoundEnabled(enabled)
            pendingSettingsWrite = null
            persistenceError.value = false
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: IOException) {
            pendingSettingsWrite = PendingSettingsWrite.Sound(enabled)
            persistenceError.value = true
        }
    }

    private suspend fun persistVibrationEnabled(enabled: Boolean) {
        try {
            feedbackSettingsRepository.setVibrationEnabled(enabled)
            pendingSettingsWrite = null
            persistenceError.value = false
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: IOException) {
            pendingSettingsWrite = PendingSettingsWrite.Vibration(enabled)
            persistenceError.value = true
        }
    }

    companion object {
        fun factory(
            themeRepository: ThemeRepository,
            languageRepository: LanguageRepository,
            historyRepository: HistoryRepository,
            feedbackSettingsRepository: FeedbackSettingsRepository,
            achievementRepository: AchievementRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(
                        themeRepository,
                        languageRepository,
                        historyRepository,
                        feedbackSettingsRepository,
                        achievementRepository,
                    ) as T
                }
            }
        }
    }
}

private sealed interface PendingSettingsWrite {
    data class Theme(val themeMode: AppThemeMode) : PendingSettingsWrite
    data class Sound(val enabled: Boolean) : PendingSettingsWrite
    data class Vibration(val enabled: Boolean) : PendingSettingsWrite
    data object ClearHistory : PendingSettingsWrite
    data object ClearAchievements : PendingSettingsWrite
}

private data class SettingsPreferencesUiState(
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val persistence: SettingsPersistenceUiState,
)

private data class SettingsPersistenceUiState(
    val showClearHistoryDialog: Boolean,
    val showClearAchievementsDialog: Boolean,
    val persistenceError: Boolean,
    val isPersistenceRetrying: Boolean,
)
