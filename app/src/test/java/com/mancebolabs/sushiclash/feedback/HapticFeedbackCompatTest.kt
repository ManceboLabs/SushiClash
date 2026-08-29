package com.mancebolabs.sushiclash.feedback

import android.os.Build
import android.view.HapticFeedbackConstants
import com.mancebolabs.sushiclash.feature.feedback.HapticFeedbackCompat
import org.junit.Assert.assertEquals
import org.junit.Test

class HapticFeedbackCompatTest {

    @Test
    fun givenApi30OrAbove_whenResolvingConstants_thenUsesConfirmAndReject() {
        assertEquals(HapticFeedbackConstants.CONFIRM, HapticFeedbackCompat.confirmConstant(Build.VERSION_CODES.R))
        assertEquals(HapticFeedbackConstants.REJECT, HapticFeedbackCompat.rejectConstant(Build.VERSION_CODES.R))
    }

    @Test
    fun givenApiBelow30_whenResolvingConstants_thenUsesCompatibleFallbacks() {
        assertEquals(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackCompat.confirmConstant(Build.VERSION_CODES.P))
        assertEquals(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackCompat.rejectConstant(Build.VERSION_CODES.P))
    }
}
