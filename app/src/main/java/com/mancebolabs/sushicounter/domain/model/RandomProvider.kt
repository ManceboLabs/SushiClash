package com.mancebolabs.sushicounter.domain.model

import kotlin.random.Random

/**
 * Abstraction over [Random] so roulette target generation can be faked in unit tests.
 */
fun interface RandomProvider {
    fun nextInt(from: Int, until: Int): Int
}

class DefaultRandomProvider : RandomProvider {
    override fun nextInt(from: Int, until: Int): Int = Random.nextInt(from, until)
}
