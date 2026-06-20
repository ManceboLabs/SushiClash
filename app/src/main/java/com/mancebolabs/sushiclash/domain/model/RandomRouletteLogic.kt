package com.mancebolabs.sushiclash.domain.model

/**
 * Pure roulette trigger rules plus target generation backed by [RandomProvider].
 *
 * Progressive random mode: after a trigger at X, the next target is drawn from [X+1, X+11].
 */
class RandomRouletteLogic(
    private val random: RandomProvider = DefaultRandomProvider(),
) {
    fun generateInitialTarget(): Int {
        return random.nextInt(
            GameState.MIN_RANDOM_ROULETTE_THRESHOLD,
            GameState.MAX_RANDOM_ROULETTE_THRESHOLD + 1,
        )
    }

    fun generateNextTargetAfterTrigger(lastTrigger: Int): Int {
        val nextMinimum = lastTrigger + 1
        val nextMaximum = lastTrigger + 11
        return random.nextInt(nextMinimum, nextMaximum + 1)
    }

    fun shouldTriggerFixed(count: Int, fixedThreshold: Int): Boolean {
        return count > 0 && count % fixedThreshold == 0
    }

    fun shouldTriggerRandom(count: Int, target: Int): Boolean {
        return count == target
    }

    companion object {
        val Default = RandomRouletteLogic()
    }
}
