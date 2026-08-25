package com.mancebolabs.sushiclash.domain.repository

import com.mancebolabs.sushiclash.domain.model.FinishGameResult
import com.mancebolabs.sushiclash.domain.model.GameSetupConfig
import com.mancebolabs.sushiclash.domain.model.GameState
import com.mancebolabs.sushiclash.domain.model.IncrementResult
import com.mancebolabs.sushiclash.domain.model.RestoreGameResult
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    val gameState: Flow<GameState>

    suspend fun restoreGameState(): RestoreGameResult

    suspend fun finishGameWithSaving(): FinishGameResult

    suspend fun finishGameWithoutSaving(): FinishGameResult

    suspend fun completeSetup(config: GameSetupConfig)

    suspend fun incrementPlayerCount(playerId: String): IncrementResult

    suspend fun resetSoloCount()

    suspend fun resetPlayerCount(playerId: String)
}
