package com.mancebolabs.sushicounter.domain.model

data class Player(
    val id: String,
    val name: String,
    val sushiCount: Int = 0,
)
