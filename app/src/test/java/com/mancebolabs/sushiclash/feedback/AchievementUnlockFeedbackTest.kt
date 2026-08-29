package com.mancebolabs.sushiclash.feedback

import com.mancebolabs.sushiclash.testutil.RecordingGameFeedbackController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementUnlockFeedbackTest {

    @Test
    fun givenVibrationEnabled_whenAchievementUnlockFeedback_thenRecordsVibrationPreference() {
        val controller = RecordingGameFeedbackController()

        controller.playAchievementUnlocked(vibrationEnabled = true)

        assertEquals(1, controller.achievementUnlockCallCount)
        assertTrue(controller.achievementVibrationEnabled == true)
    }

    @Test
    fun givenVibrationDisabled_whenAchievementUnlockFeedback_thenRecordsDisabledVibration() {
        val controller = RecordingGameFeedbackController()

        controller.playAchievementUnlocked(vibrationEnabled = false)

        assertEquals(1, controller.achievementUnlockCallCount)
        assertTrue(controller.achievementVibrationEnabled == false)
    }

    @Test
    fun givenSushiIncrementFeedback_whenInvoked_thenStillUsesSoundSetting() {
        val controller = RecordingGameFeedbackController()

        controller.playSushiIncrement(soundEnabled = true, vibrationEnabled = false)

        assertTrue(controller.sushiIncrementSoundEnabled == true)
        assertTrue(controller.sushiIncrementVibrationEnabled == false)
        assertNull(controller.achievementVibrationEnabled)
    }
}
