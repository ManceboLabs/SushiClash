package com.mancebolabs.sushicounter.domain.model

data class GameSetupConfig(
    val gameMode: GameMode,
    val playerNames: List<String> = emptyList(),
    val randomRouletteEnabled: Boolean = false,
    val randomRouletteTriggerType: RandomRouletteTriggerType = RandomRouletteTriggerType.FIXED,
    val randomRouletteFixedThreshold: Int = GameState.DEFAULT_RANDOM_ROULETTE_THRESHOLD,
)
