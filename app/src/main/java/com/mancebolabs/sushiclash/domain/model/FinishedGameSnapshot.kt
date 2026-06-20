package com.mancebolabs.sushiclash.domain.model

data class FinishedGameSnapshot(
    val gameMode: GameMode,
    val soloCount: Int?,
    val playerScores: List<PlayerScore>,
    val randomRouletteEnabled: Boolean,
    val randomRouletteTriggerType: RandomRouletteTriggerType,
    val randomRouletteFixedThreshold: Int,
    val finishedAt: Long,
)
