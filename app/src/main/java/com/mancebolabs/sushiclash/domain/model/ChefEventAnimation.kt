package com.mancebolabs.sushiclash.domain.model

enum class ChefEventAnimation {
    DEVOURING,
    GIANT_SUSHI,
    COMA,
    ATTACK,
    SPICY,
    NINJA,
    MONSTER,
}

data class ChefAnimationEvent(
    val animation: ChefEventAnimation,
)
