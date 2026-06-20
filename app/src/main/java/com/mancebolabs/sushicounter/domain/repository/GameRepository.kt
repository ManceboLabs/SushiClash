package com.mancebolabs.sushicounter.domain.repository

import com.mancebolabs.sushicounter.domain.model.GameSetupConfig
import com.mancebolabs.sushicounter.domain.model.GameState
import com.mancebolabs.sushicounter.domain.model.IncrementResult
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    val gameState: Flow<GameState>

    suspend fun completeSetup(config: GameSetupConfig)

    suspend fun incrementPlayerCount(playerId: String): IncrementResult

    suspend fun resetSoloCount()

    suspend fun resetPlayerCount(playerId: String)
}
