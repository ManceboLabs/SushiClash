package com.mancebolabs.sushicounter.domain.model

data class GameState(
    val hasCompletedSetup: Boolean = false,
    val gameMode: GameMode? = null,
    val players: List<Player> = emptyList(),
) {
    val soloCount: Int
        get() = players.firstOrNull()?.sushiCount ?: 0
}
