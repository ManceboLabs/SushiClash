package com.mancebolabs.sushiclash.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mancebolabs.sushiclash.domain.model.AppThemeMode
import com.mancebolabs.sushiclash.domain.repository.FeedbackSettingsRepository
import com.mancebolabs.sushiclash.domain.repository.HistoryRepository
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
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val showClearHistoryDialog: Boolean = false,
    val persistenceError: Boolean = false,
    val isPersistenceRetrying: Boolean = false,
)

class SettingsViewModel(
    private val themeRepository: ThemeRepository,
    private val historyRepository: HistoryRepository,
    private val feedbackSettingsRepository: FeedbackSettingsRepository,
) : ViewModel() {

    private val showClearHistoryDialog = MutableStateFlow(false)
    private val persistenceError = MutableStateFlow(false)
    private val isPersistenceRetrying = MutableStateFlow(false)
    private var pendingSettingsWrite: PendingSettingsWrite? = null

    val uiState: StateFlow<SettingsUiState> = combine(
        themeRepository.themeMode,
        feedbackSettingsRepository.soundEnabled,
        feedbackSettingsRepository.vibrationEnabled,
        combine(
            showClearHistoryDialog,
            persistenceError,
            isPersistenceRetrying,
        ) { clearDialog, hasError, isRetrying ->
            SettingsPersistenceUiState(
                showClearHistoryDialog = clearDialog,
                persistenceError = hasError,
                isPersistenceRetrying = isRetrying,
            )
        },
    ) { themeMode, soundEnabled, vibrationEnabled, persistence ->
        SettingsUiState(
            themeMode = themeMode,
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled,
            showClearHistoryDialog = persistence.showClearHistoryDialog,
            persistenceError = persistence.persistenceError,
            isPersistenceRetrying = persistence.isPersistenceRetrying,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun onThemeModeSelected(themeMode: AppThemeMode) {
        viewModelScope.launch {
            persistThemeMode(themeMode)
        }
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
                }
            } finally {
                isPersistenceRetrying.value = false
            }
        }
    }

    private suspend fun persistClearHistory() {
        try {
            // Only persisted history lists are cleared. Active game, theme, and onboarding
            // completion live in separate preferences and are intentionally untouched here.
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
            historyRepository: HistoryRepository,
            feedbackSettingsRepository: FeedbackSettingsRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(
                        themeRepository,
                        historyRepository,
                        feedbackSettingsRepository,
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
}

private data class SettingsPersistenceUiState(
    val showClearHistoryDialog: Boolean,
    val persistenceError: Boolean,
    val isPersistenceRetrying: Boolean,
)
