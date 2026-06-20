package com.mancebolabs.sushicounter.domain.model

data class IncrementResult(
    val newCount: Int,
    val shouldTriggerRoulette: Boolean,
)
