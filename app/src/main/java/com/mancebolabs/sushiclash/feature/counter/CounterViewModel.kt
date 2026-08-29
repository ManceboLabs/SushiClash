package com.mancebolabs.sushiclash.feature.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mancebolabs.sushiclash.domain.model.FinishGameResult
import com.mancebolabs.sushiclash.domain.model.GameMode
import com.mancebolabs.sushiclash.domain.model.GameSetupConfig
import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.IncrementResult
import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import com.mancebolabs.sushiclash.domain.model.Player
import com.mancebolabs.sushiclash.domain.model.RestoreGameResult
import com.mancebolabs.sushiclash.domain.repository.GameRepository
import com.mancebolabs.sushiclash.domain.repository.OnboardingRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlayerResetRequest(
    val playerId: String,
    val playerName: String,
)

sealed interface RouletteTriggerEvent {
    data class Solo(
        val count: Int,
    ) : RouletteTriggerEvent

    data class Group(
        val playerName: String,
        val count: Int,
    ) : RouletteTriggerEvent
}

data class CounterUiState(
    val gameState: GameState = GameState(),
    val startupState: AppStartupState = AppStartupState.Loading,
    val playerResetRequest: PlayerResetRequest? = null,
    val rouletteTriggerEvent: RouletteTriggerEvent? = null,
    val showFinishGameDialog: Boolean = false,
    val isFinishGameSaving: Boolean = false,
    val finishGameSaveError: Boolean = false,
    val showSetupDialog: Boolean = false,
    val persistenceError: Boolean = false,
    val isPersistenceRetrying: Boolean = false,
) {
    val gameMode: GameMode?
        get() = gameState.gameMode

    val players: List<Player>
        get() = gameState.players

    val soloCount: Int
        get() = gameState.soloCount
}

class CounterViewModel(
    private val gameRepository: GameRepository,
    private val onboardingRepository: OnboardingRepository,
) : ViewModel() {

    private val startupState = MutableStateFlow<AppStartupState>(AppStartupState.Loading)
    private val playerResetRequest = MutableStateFlow<PlayerResetRequest?>(null)
    private val rouletteTriggerEvent = MutableStateFlow<RouletteTriggerEvent?>(null)
    private val showFinishGameDialog = MutableStateFlow(false)
    private val isFinishGameSaving = MutableStateFlow(false)
    private val finishGameSaveError = MutableStateFlow(false)
    private val showSetupDialog = MutableStateFlow(false)
    private val persistenceError = MutableStateFlow(false)
    private val isPersistenceRetrying = MutableStateFlow(false)
    private var lastFailedPersistenceAction: LastFailedPersistenceAction? = null

    init {
        // Resolve startup after onboarding is known. Unreadable onboarding must not hang Loading.
        viewModelScope.launch {
            awaitOnboardingOrUnreadable()
            resolveStartupStateFromPersistence(clearErrorOnSuccess = false)
        }
    }

    fun onPersistenceRetry() {
        if (!isPersistenceRetrying.compareAndSet(expect = false, update = true)) return
        viewModelScope.launch {
            try {
                when (val failedAction = lastFailedPersistenceAction) {
                    is LastFailedPersistenceAction.Increment -> incrementCount(failedAction.playerId)
                    is LastFailedPersistenceAction.Setup -> completeConfirmedSetup(failedAction.config)
                    LastFailedPersistenceAction.Restore,
                    null -> resolveStartupStateFromPersistence(clearErrorOnSuccess = true)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: IOException) {
                persistenceError.value = true
            } finally {
                isPersistenceRetrying.value = false
            }
        }
    }

    private suspend fun awaitOnboardingOrUnreadable() {
        val onboardingState = onboardingRepository.hasCompletedOnboardingState.first { state ->
            when (state) {
                is PersistenceReadState.Data -> state.value
                PersistenceReadState.Missing -> false
                PersistenceReadState.Corrupted,
                PersistenceReadState.Unavailable -> true
            }
        }
        if (onboardingState is PersistenceReadState.Corrupted ||
            onboardingState is PersistenceReadState.Unavailable
        ) {
            persistenceError.value = true
        }
    }

    private suspend fun resolveStartupStateFromPersistence(clearErrorOnSuccess: Boolean) {
        when (val loadedState = gameRepository.restoreGameState()) {
            is RestoreGameResult.Restored -> {
                if (clearErrorOnSuccess) {
                    lastFailedPersistenceAction = null
                    persistenceError.value = false
                }
                startupState.value = if (loadedState.gameState.hasActiveGame) {
                    AppStartupState.ActiveGame
                } else {
                    AppStartupState.NoActiveGame
                }
            }
            RestoreGameResult.Unavailable -> {
                lastFailedPersistenceAction = LastFailedPersistenceAction.Restore
                persistenceError.value = true
                startupState.value = AppStartupState.NoActiveGame
            }
        }
    }

    private data class CounterScreenState(
        val startupState: AppStartupState,
        val playerResetRequest: PlayerResetRequest?,
        val rouletteTriggerEvent: RouletteTriggerEvent?,
        val finishGame: FinishGameUiState,
        val showSetupDialog: Boolean,
    )

    private data class FinishGameUiState(
        val showDialog: Boolean,
        val isSaving: Boolean,
        val hasError: Boolean,
    )

    private data class PersistenceUiState(
        val hasError: Boolean,
        val isRetrying: Boolean,
    )

    private val finishGameUiState = combine(
        showFinishGameDialog,
        isFinishGameSaving,
        finishGameSaveError,
    ) { showDialog, isSaving, hasError ->
        FinishGameUiState(
            showDialog = showDialog,
            isSaving = isSaving,
            hasError = hasError,
        )
    }

    val uiState: StateFlow<CounterUiState> = combine(
        gameRepository.gameState,
        combine(
            startupState,
            playerResetRequest,
            rouletteTriggerEvent,
            finishGameUiState,
            showSetupDialog,
        ) { startup, resetRequest, rouletteEvent, finishGame, setupDialog ->
            CounterScreenState(
                startupState = startup,
                playerResetRequest = resetRequest,
                rouletteTriggerEvent = rouletteEvent,
                finishGame = finishGame,
                showSetupDialog = setupDialog,
            )
        },
        combine(persistenceError, isPersistenceRetrying) { hasError, isRetrying ->
            PersistenceUiState(
                hasError = hasError,
                isRetrying = isRetrying,
            )
        },
    ) { gameState, screenState, persistence ->
        CounterUiState(
            gameState = gameState,
            startupState = screenState.startupState,
            playerResetRequest = screenState.playerResetRequest,
            rouletteTriggerEvent = screenState.rouletteTriggerEvent,
            showFinishGameDialog = screenState.finishGame.showDialog,
            isFinishGameSaving = screenState.finishGame.isSaving,
            finishGameSaveError = screenState.finishGame.hasError,
            showSetupDialog = screenState.showSetupDialog,
            persistenceError = persistence.hasError,
            isPersistenceRetrying = persistence.isRetrying,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = CounterUiState(),
    )

    fun onStartGameRequested() {
        showSetupDialog.value = true
    }

    fun onSetupDismissed() {
        showSetupDialog.value = false
    }

    fun onPlayerSushiTapped(playerId: String) {
        if (startupState.value != AppStartupState.ActiveGame) return
        viewModelScope.launch {
            incrementCount(playerId)
        }
    }

    fun onPlayerResetRequested(playerId: String) {
        val player = uiState.value.players.find { it.id == playerId } ?: return
        playerResetRequest.value = PlayerResetRequest(
            playerId = player.id,
            playerName = player.name,
        )
    }

    fun onPlayerResetDismissed() {
        playerResetRequest.value = null
    }

    fun onPlayerResetConfirmed() {
        val request = playerResetRequest.value ?: return
        viewModelScope.launch {
            try {
                gameRepository.resetPlayerCount(request.playerId)
                playerResetRequest.value = null
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: IOException) {
                persistenceError.value = true
            }
        }
    }

    fun onSoloSushiTapped() {
        if (startupState.value != AppStartupState.ActiveGame) return
        viewModelScope.launch {
            val playerId = gameRepository.gameState.first().players.firstOrNull()?.id ?: return@launch
            incrementCount(playerId)
        }
    }

    private suspend fun incrementCount(playerId: String) {
        try {
            val state = gameRepository.gameState.first()
            val result = gameRepository.incrementPlayerCount(playerId)
            emitRouletteTriggerIfNeeded(
                gameState = state,
                playerId = playerId,
                result = result,
            )
            lastFailedPersistenceAction = null
            persistenceError.value = false
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: IOException) {
            lastFailedPersistenceAction = LastFailedPersistenceAction.Increment(playerId)
            persistenceError.value = true
        }
    }

    fun onResetSoloCountConfirmed() {
        viewModelScope.launch {
            try {
                gameRepository.resetSoloCount()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: IOException) {
                persistenceError.value = true
            }
        }
    }

    fun onFinishGameRequested() {
        if (startupState.value != AppStartupState.ActiveGame) return
        // Only open the dialog; the active game stays in persistence so Cancel and process death are safe.
        finishGameSaveError.value = false
        showFinishGameDialog.value = true
    }

    fun onFinishGameCancelled() {
        if (isFinishGameSaving.value) return
        finishGameSaveError.value = false
        showFinishGameDialog.value = false
    }

    fun onFinishGameWithoutSaving() {
        finishGame(gameRepository::finishGameWithoutSaving)
    }

    fun onFinishGameWithSaving() {
        finishGame(gameRepository::finishGameWithSaving)
    }

    private fun finishGame(operation: suspend () -> FinishGameResult) {
        // Set the guard before launching so rapid taps cannot enqueue duplicate persistence work.
        if (!isFinishGameSaving.compareAndSet(expect = false, update = true)) return
        finishGameSaveError.value = false

        viewModelScope.launch {
            try {
                when (operation()) {
                    FinishGameResult.Success,
                    FinishGameResult.NoActiveGame,
                    -> {
                        showFinishGameDialog.value = false
                        finishGameSaveError.value = false
                        startupState.value = AppStartupState.NoActiveGame
                    }
                    is FinishGameResult.Failure -> {
                        finishGameSaveError.value = true
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: IOException) {
                finishGameSaveError.value = true
            } finally {
                isFinishGameSaving.value = false
            }
        }
    }

    fun onSetupConfirmed(config: GameSetupConfig) {
        viewModelScope.launch {
            completeConfirmedSetup(config)
        }
    }

    private suspend fun completeConfirmedSetup(config: GameSetupConfig) {
        try {
            gameRepository.completeSetup(config)
            showSetupDialog.value = false
            startupState.value = AppStartupState.ActiveGame
            lastFailedPersistenceAction = null
            persistenceError.value = false
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: IOException) {
            showSetupDialog.value = false
            lastFailedPersistenceAction = LastFailedPersistenceAction.Setup(config)
            persistenceError.value = true
        }
    }

    fun onRouletteTriggerDismissed() {
        rouletteTriggerEvent.value = null
    }

    fun onRouletteTriggerConfirmed() {
        rouletteTriggerEvent.value = null
    }

    private fun emitRouletteTriggerIfNeeded(
        gameState: GameState,
        playerId: String,
        result: IncrementResult,
    ) {
        if (!result.shouldTriggerRoulette) return

        rouletteTriggerEvent.value = when (gameState.gameMode) {
            GameMode.SOLO -> RouletteTriggerEvent.Solo(count = result.newCount)
            GameMode.GROUP -> {
                val playerName = gameState.players.find { it.id == playerId }?.name ?: return
                RouletteTriggerEvent.Group(
                    playerName = playerName,
                    count = result.newCount,
                )
            }
            null -> null
        }
    }

    companion object {
        fun factory(
            gameRepository: GameRepository,
            onboardingRepository: OnboardingRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CounterViewModel(
                        gameRepository,
                        onboardingRepository,
                    ) as T
                }
            }
        }
    }
}

private sealed interface LastFailedPersistenceAction {
    data object Restore : LastFailedPersistenceAction
    data class Increment(val playerId: String) : LastFailedPersistenceAction
    data class Setup(val config: GameSetupConfig) : LastFailedPersistenceAction
}
