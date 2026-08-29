package com.mancebolabs.sushiclash.feature.feedback

interface GameFeedbackController {
    fun playSushiIncrement(
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
    )

    fun playRouletteTriggered(
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
    )

    fun playAchievementUnlocked(
        vibrationEnabled: Boolean,
    )
}

object NoOpGameFeedbackController : GameFeedbackController {
    override fun playSushiIncrement(
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
    ) = Unit

    override fun playRouletteTriggered(
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
    ) = Unit

    override fun playAchievementUnlocked(
        vibrationEnabled: Boolean,
    ) = Unit
}
