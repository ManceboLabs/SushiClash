package com.mancebolabs.sushicounter.feature.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mancebolabs.sushicounter.domain.model.FinishedGameSnapshot
import com.mancebolabs.sushicounter.domain.model.GameMode
import com.mancebolabs.sushicounter.domain.model.GameSetupConfig
import com.mancebolabs.sushicounter.domain.model.GameState
import com.mancebolabs.sushicounter.domain.model.IncrementResult
import com.mancebolabs.sushicounter.domain.model.Player
import com.mancebolabs.sushicounter.domain.repository.GameRepository
import com.mancebolabs.sushicounter.domain.repository.HistoryRepository
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
    val showSetupDialog: Boolean = false,
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
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private val startupState = MutableStateFlow<AppStartupState>(AppStartupState.Loading)
    private val playerResetRequest = MutableStateFlow<PlayerResetRequest?>(null)
    private val rouletteTriggerEvent = MutableStateFlow<RouletteTriggerEvent?>(null)
    private val showFinishGameDialog = MutableStateFlow(false)
    private val showSetupDialog = MutableStateFlow(false)

    init {
        // Resolve startup state once from persistence to prevent setup-popup flicker on launch.
        viewModelScope.launch {
            val loadedState = gameRepository.gameState.first()
            startupState.value = if (loadedState.hasActiveGame) {
                AppStartupState.ActiveGame
            } else {
                AppStartupState.NoActiveGame
            }
        }
    }

    private data class CounterScreenState(
        val startupState: AppStartupState,
        val playerResetRequest: PlayerResetRequest?,
        val rouletteTriggerEvent: RouletteTriggerEvent?,
        val showFinishGameDialog: Boolean,
        val showSetupDialog: Boolean,
    )

    val uiState: StateFlow<CounterUiState> = combine(
        gameRepository.gameState,
        combine(
            startupState,
            playerResetRequest,
            rouletteTriggerEvent,
            showFinishGameDialog,
            showSetupDialog,
        ) { startup, resetRequest, rouletteEvent, finishDialog, setupDialog ->
            CounterScreenState(
                startupState = startup,
                playerResetRequest = resetRequest,
                rouletteTriggerEvent = rouletteEvent,
                showFinishGameDialog = finishDialog,
                showSetupDialog = setupDialog,
            )
        },
    ) { gameState, screenState ->
        CounterUiState(
            gameState = gameState,
            startupState = screenState.startupState,
            playerResetRequest = screenState.playerResetRequest,
            rouletteTriggerEvent = screenState.rouletteTriggerEvent,
            showFinishGameDialog = screenState.showFinishGameDialog,
            showSetupDialog = screenState.showSetupDialog,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = CounterUiState(),
    )

    fun onStartGameRequested() {
        showSetupDialog.value = true
    }

    fun onPlayerSushiTapped(playerId: String) {
        if (startupState.value != AppStartupState.ActiveGame) return
        viewModelScope.launch {
            val state = gameRepository.gameState.first()
            val result = gameRepository.incrementPlayerCount(playerId)
            emitRouletteTriggerIfNeeded(
                gameState = state,
                playerId = playerId,
                result = result,
            )
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
            gameRepository.resetPlayerCount(request.playerId)
            playerResetRequest.value = null
        }
    }

    fun onSoloSushiTapped() {
        if (startupState.value != AppStartupState.ActiveGame) return
        viewModelScope.launch {
            val state = gameRepository.gameState.first()
            val playerId = state.players.firstOrNull()?.id ?: return@launch
            val result = gameRepository.incrementPlayerCount(playerId)
            emitRouletteTriggerIfNeeded(
                gameState = state,
                playerId = playerId,
                result = result,
            )
        }
    }

    fun onResetSoloCountConfirmed() {
        viewModelScope.launch {
            gameRepository.resetSoloCount()
        }
    }

    fun onFinishGameRequested() {
        viewModelScope.launch {
            // Active game is cleared in the repository before the save dialog is shown.
            val snapshot = gameRepository.finishActiveGame() ?: return@launch
            pendingFinishedGame = snapshot
            startupState.value = AppStartupState.NoActiveGame
            showFinishGameDialog.value = true
        }
    }

    fun onFinishGameCancelled() {
        showFinishGameDialog.value = false
        pendingFinishedGame = null
    }

    fun onFinishGameWithoutSaving() {
        showFinishGameDialog.value = false
        pendingFinishedGame = null
    }

    fun onFinishGameWithSaving() {
        viewModelScope.launch {
            val snapshot = pendingFinishedGame ?: return@launch
            historyRepository.saveFinishedGame(snapshot)
            showFinishGameDialog.value = false
            pendingFinishedGame = null
        }
    }

    fun onSetupConfirmed(config: GameSetupConfig) {
        viewModelScope.launch {
            gameRepository.completeSetup(config)
            showSetupDialog.value = false
            startupState.value = AppStartupState.ActiveGame
        }
    }

    fun onRouletteTriggerDismissed() {
        rouletteTriggerEvent.value = null
    }

    fun onRouletteTriggerConfirmed() {
        rouletteTriggerEvent.value = null
    }

    private var pendingFinishedGame: FinishedGameSnapshot? = null

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
            historyRepository: HistoryRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CounterViewModel(gameRepository, historyRepository) as T
                }
            }
        }
    }
}
