package com.mancebolabs.sushiclash.domain.model

/**
 * Progressive chef surprise-event trigger rules backed by [RandomProvider].
 *
 * After a trigger at count X, the next target is X plus a random interval in [MIN_INTERVAL, MAX_INTERVAL].
 */
class ChefAnimationTriggerLogic(
    private val random: RandomProvider = DefaultRandomProvider(),
) {
    fun generateInitialTarget(): Int {
        return random.nextInt(MIN_INTERVAL, MAX_INTERVAL + 1)
    }

    fun generateNextTargetAfterTrigger(currentCount: Int): Int {
        return currentCount + random.nextInt(MIN_INTERVAL, MAX_INTERVAL + 1)
    }

    fun resolveStoredTarget(player: Player): Int {
        return player.nextChefAnimationTarget ?: if (player.sushiCount == 0) {
            generateInitialTarget()
        } else {
            generateNextTargetAfterTrigger(player.sushiCount)
        }
    }

    fun shouldTrigger(count: Int, target: Int): Boolean {
        return count == target
    }

    companion object {
        const val MIN_INTERVAL = 3
        const val MAX_INTERVAL = 5

        val Default = ChefAnimationTriggerLogic()
    }
}
