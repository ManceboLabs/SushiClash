package com.mancebolabs.sushiclash.domain.model

data class IncrementResult(
    val newCount: Int,
    val shouldTriggerRoulette: Boolean,
    val chefAnimationEvent: ChefAnimationEvent? = null,
)
