package com.mancebolabs.sushiclash.domain.model

sealed interface RestoreGameResult {
    data class Restored(val gameState: GameState) : RestoreGameResult

    data object Unavailable : RestoreGameResult
}
