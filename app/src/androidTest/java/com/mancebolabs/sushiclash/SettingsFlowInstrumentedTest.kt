package com.mancebolabs.sushiclash

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.support.clickNav
import com.mancebolabs.sushiclash.support.clickText
import com.mancebolabs.sushiclash.support.openLanguagePicker
import com.mancebolabs.sushiclash.support.openSettings
import com.mancebolabs.sushiclash.support.selectTheme
import com.mancebolabs.sushiclash.support.skipOnboardingIfShown
import com.mancebolabs.sushiclash.support.waitForMainShell
import com.mancebolabs.sushiclash.support.waitForText
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsFlowInstrumentedTest : SushiClashInstrumentedTestCase() {

    @Before
    fun prepareMainApp() {
        composeTestRule.skipOnboardingIfShown()
    }

    @Test
    fun givenDarkThemeSelected_whenActivityRecreated_thenSettingsRemainUsable() {
        composeTestRule.openSettings()
        composeTestRule.selectTheme(R.string.settings_theme_dark)

        composeTestRule.activityRule.scenario.recreate()
        composeTestRule.waitForIdle()
        composeTestRule.waitForMainShell()

        composeTestRule.openSettings()
        composeTestRule.waitForText(R.string.settings_screen_title)
        composeTestRule.selectTheme(R.string.settings_theme_light)
        composeTestRule.waitForText(R.string.settings_theme_light)
    }

    @Test
    fun givenLanguagePicker_whenSelectingSpanish_thenUiUpdatesToSpanish() {
        composeTestRule.openSettings()
        composeTestRule.openLanguagePicker()
        composeTestRule.clickText(R.string.settings_language_spanish)

        composeTestRule.waitForText(R.string.settings_screen_title)

        composeTestRule.clickNav(R.string.nav_counter)
        composeTestRule.waitForText(R.string.counter_start_game)
    }
}
