package com.mancebolabs.sushicounter.data.repository

import com.mancebolabs.sushicounter.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushicounter.domain.model.GameMode
import com.mancebolabs.sushicounter.domain.model.Player
import com.mancebolabs.sushicounter.domain.repository.GameRepository
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class GameRepositoryImpl(
    private val dataStore: AppPreferencesDataStore,
) : GameRepository {

    override val gameState: Flow<com.mancebolabs.sushicounter.domain.model.GameState> = dataStore.gameState

    override suspend fun completeSetup(
        gameMode: GameMode,
        playerNames: List<String>,
    ) {
        val players = when (gameMode) {
            GameMode.SOLO -> listOf(
                Player(
                    id = AppPreferencesDataStore.SOLO_PLAYER_ID,
                    name = "",
                    sushiCount = 0,
                ),
            )
            GameMode.GROUP -> playerNames.map { name ->
                Player(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    sushiCount = 0,
                )
            }
        }

        dataStore.saveGameState(
            hasCompletedSetup = true,
            gameMode = gameMode,
            players = players,
        )

        if (gameMode == GameMode.GROUP) {
            dataStore.setParticipants(players.map { it.name })
        }
    }

    override suspend fun incrementPlayerCount(playerId: String) {
        val currentState = dataStore.gameState.first()
        val updatedPlayers = currentState.players.map { player ->
            if (player.id == playerId) {
                player.copy(sushiCount = player.sushiCount + 1)
            } else {
                player
            }
        }
        dataStore.setPlayers(updatedPlayers)
    }

    override suspend fun resetSoloCount() {
        val currentState = dataStore.gameState.first()
        if (currentState.gameMode != GameMode.SOLO) return

        val updatedPlayers = currentState.players.map { player ->
            player.copy(sushiCount = 0)
        }
        dataStore.setPlayers(updatedPlayers)
    }

    override suspend fun resetPlayerCount(playerId: String) {
        val currentState = dataStore.gameState.first()
        if (currentState.gameMode != GameMode.GROUP) return

        val updatedPlayers = currentState.players.map { player ->
            if (player.id == playerId) {
                player.copy(sushiCount = 0)
            } else {
                player
            }
        }
        dataStore.setPlayers(updatedPlayers)
    }
}
