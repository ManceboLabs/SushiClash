package com.mancebolabs.sushiclash.domain.model

data class GameState(
    val hasActiveGame: Boolean = false,
    val sessionId: String? = null,
    val gameMode: GameMode? = null,
    val players: List<Player> = emptyList(),
    val randomRouletteEnabled: Boolean = false,
    val randomRouletteTriggerType: RandomRouletteTriggerType = RandomRouletteTriggerType.FIXED,
    val randomRouletteFixedThreshold: Int = DEFAULT_RANDOM_ROULETTE_THRESHOLD,
) {
    val soloCount: Int
        get() = players.firstOrNull()?.sushiCount ?: 0

    val soloNextRandomRouletteTarget: Int?
        get() = players.firstOrNull()?.nextRandomRouletteTarget

    val soloLastRandomRouletteTrigger: Int
        get() = players.firstOrNull()?.lastRandomRouletteTrigger ?: 0

    companion object {
        const val DEFAULT_RANDOM_ROULETTE_THRESHOLD = 5
        const val MIN_RANDOM_ROULETTE_THRESHOLD = 1
        const val MAX_RANDOM_ROULETTE_THRESHOLD = 10
    }
}
