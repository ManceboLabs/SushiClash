package com.mancebolabs.sushiclash.domain.model

data class Player(
    val id: String,
    val name: String,
    val sushiCount: Int = 0,
    val nextRandomRouletteTarget: Int? = null,
    val lastRandomRouletteTrigger: Int = 0,
    val nextChefAnimationTarget: Int? = null,
    val lastChefAnimationTrigger: Int = 0,
    val lastChefEventAnimation: ChefEventAnimation? = null,
)
