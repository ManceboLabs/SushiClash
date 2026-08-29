package com.mancebolabs.sushiclash.testutil

import com.mancebolabs.sushiclash.feature.feedback.GameFeedbackController

class RecordingGameFeedbackController : GameFeedbackController {
    var sushiIncrementSoundEnabled: Boolean? = null
    var sushiIncrementVibrationEnabled: Boolean? = null
    var rouletteSoundEnabled: Boolean? = null
    var rouletteVibrationEnabled: Boolean? = null
    var achievementVibrationEnabled: Boolean? = null
    var achievementUnlockCallCount = 0

    override fun playSushiIncrement(
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
    ) {
        sushiIncrementSoundEnabled = soundEnabled
        sushiIncrementVibrationEnabled = vibrationEnabled
    }

    override fun playRouletteTriggered(
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
    ) {
        rouletteSoundEnabled = soundEnabled
        rouletteVibrationEnabled = vibrationEnabled
    }

    override fun playAchievementUnlocked(
        vibrationEnabled: Boolean,
    ) {
        achievementUnlockCallCount++
        achievementVibrationEnabled = vibrationEnabled
    }
}
