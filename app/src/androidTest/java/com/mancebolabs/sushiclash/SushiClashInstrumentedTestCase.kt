package com.mancebolabs.sushiclash

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule

abstract class SushiClashInstrumentedTestCase {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
}
