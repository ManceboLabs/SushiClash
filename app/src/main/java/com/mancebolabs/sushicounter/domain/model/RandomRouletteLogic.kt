package com.mancebolabs.sushicounter.domain.model

import kotlin.random.Random

object RandomRouletteLogic {
    fun generateInitialTarget(): Int {
        return Random.nextInt(
            GameState.MIN_RANDOM_ROULETTE_THRESHOLD,
            GameState.MAX_RANDOM_ROULETTE_THRESHOLD + 1,
        )
    }

    fun generateNextTargetAfterTrigger(lastTrigger: Int): Int {
        val nextMinimum = lastTrigger + 1
        val nextMaximum = lastTrigger + 11
        return Random.nextInt(nextMinimum, nextMaximum + 1)
    }

    fun shouldTriggerFixed(count: Int, fixedThreshold: Int): Boolean {
        return count > 0 && count % fixedThreshold == 0
    }

    fun shouldTriggerRandom(count: Int, target: Int): Boolean {
        return count == target
    }
}
