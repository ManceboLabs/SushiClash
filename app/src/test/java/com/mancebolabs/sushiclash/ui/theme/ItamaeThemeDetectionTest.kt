package com.mancebolabs.sushiclash.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ItamaeThemeDetectionTest {

    @Test
    fun givenLightBackgroundColor_whenCheckingLuminance_thenThemeIsLight() {
        assertFalse(Color.White.luminance() < 0.5f)
    }

    @Test
    fun givenDarkBackgroundColor_whenCheckingLuminance_thenThemeIsDark() {
        assertTrue(Color.Black.luminance() < 0.5f)
    }
}
