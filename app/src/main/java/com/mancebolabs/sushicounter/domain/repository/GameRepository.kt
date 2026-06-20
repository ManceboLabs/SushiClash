package com.mancebolabs.sushicounter.domain.repository

import com.mancebolabs.sushicounter.domain.model.GameMode
import com.mancebolabs.sushicounter.domain.model.GameState
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    val gameState: Flow<GameState>

    suspend fun completeSetup(
        gameMode: GameMode,
        playerNames: List<String>,
    )

    suspend fun incrementPlayerCount(playerId: String)

    suspend fun resetSoloCount()

    suspend fun resetPlayerCount(playerId: String)
}
