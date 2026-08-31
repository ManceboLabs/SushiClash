package com.mancebolabs.sushiclash.domain.model

class ChefEventAnimationSelector(
    private val random: RandomProvider = DefaultRandomProvider(),
) {
    fun select(lastAnimation: ChefEventAnimation?): ChefEventAnimation {
        val pool = ChefEventAnimation.entries
        if (pool.size == 1) {
            return pool.first()
        }

        val candidates = if (lastAnimation != null) {
            pool.filter { it != lastAnimation }
        } else {
            pool
        }
        val index = random.nextInt(0, candidates.size)
        return candidates[index]
    }

    companion object {
        val Default = ChefEventAnimationSelector()
    }
}
