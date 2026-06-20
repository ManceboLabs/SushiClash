package com.mancebolabs.sushiclash.domain.repository

import com.mancebolabs.sushiclash.domain.model.FinishedGameSnapshot
import com.mancebolabs.sushiclash.domain.model.GameSetupConfig
import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.IncrementResult
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    val gameState: Flow<GameState>

    suspend fun createFinishedGameSnapshot(): FinishedGameSnapshot?

    suspend fun clearActiveGame()

    suspend fun completeSetup(config: GameSetupConfig)

    suspend fun incrementPlayerCount(playerId: String): IncrementResult

    suspend fun resetSoloCount()

    suspend fun resetPlayerCount(playerId: String)
}
