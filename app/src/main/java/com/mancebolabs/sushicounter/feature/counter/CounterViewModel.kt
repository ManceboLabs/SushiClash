package com.mancebolabs.sushicounter.feature.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mancebolabs.sushicounter.domain.model.GameMode
import com.mancebolabs.sushicounter.domain.model.GameState
import com.mancebolabs.sushicounter.domain.model.Player
import com.mancebolabs.sushicounter.domain.repository.GameRepository
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

data class CounterUiState(
    val gameState: GameState = GameState(),
    val startupState: AppStartupState = AppStartupState.Loading,
    val playerResetRequest: PlayerResetRequest? = null,
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
) : ViewModel() {

    private val startupState = MutableStateFlow<AppStartupState>(AppStartupState.Loading)
    private val playerResetRequest = MutableStateFlow<PlayerResetRequest?>(null)

    init {
        viewModelScope.launch {
            val loadedState = gameRepository.gameState.first()
            startupState.value = if (loadedState.hasCompletedSetup) {
                AppStartupState.Ready
            } else {
                AppStartupState.SetupRequired
            }
        }
    }

    val uiState: StateFlow<CounterUiState> = combine(
        gameRepository.gameState,
        startupState,
        playerResetRequest,
    ) { gameState, startup, resetRequest ->
        CounterUiState(
            gameState = gameState,
            startupState = startup,
            playerResetRequest = resetRequest,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = CounterUiState(),
    )

    fun onPlayerSushiTapped(playerId: String) {
        viewModelScope.launch {
            gameRepository.incrementPlayerCount(playerId)
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
        viewModelScope.launch {
            val playerId = uiState.value.players.firstOrNull()?.id ?: return@launch
            gameRepository.incrementPlayerCount(playerId)
        }
    }

    fun onResetSoloCountConfirmed() {
        viewModelScope.launch {
            gameRepository.resetSoloCount()
        }
    }

    fun onRestartRequested() {
        startupState.value = AppStartupState.SetupRequired
    }

    fun onSetupConfirmed(
        gameMode: GameMode,
        playerNames: List<String>,
    ) {
        viewModelScope.launch {
            gameRepository.completeSetup(
                gameMode = gameMode,
                playerNames = playerNames,
            )
            startupState.value = AppStartupState.Ready
        }
    }

    companion object {
        fun factory(gameRepository: GameRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CounterViewModel(gameRepository) as T
                }
            }
        }
    }
}
