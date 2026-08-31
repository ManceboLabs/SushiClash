package com.mancebolabs.sushiclash

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.support.clearAchievementsFromSettings
import com.mancebolabs.sushiclash.support.clearHistoryFromSettings
import com.mancebolabs.sushiclash.support.clickNav
import com.mancebolabs.sushiclash.support.clickText
import com.mancebolabs.sushiclash.support.openLanguagePicker
import com.mancebolabs.sushiclash.support.openOnboardingFromSettings
import com.mancebolabs.sushiclash.support.openSettings
import com.mancebolabs.sushiclash.support.selectTheme
import com.mancebolabs.sushiclash.support.skipOnboardingIfShown
import com.mancebolabs.sushiclash.support.waitForMainShell
import com.mancebolabs.sushiclash.support.waitForSettingsScreen
import com.mancebolabs.sushiclash.support.waitForText
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsSmokeUiTest : SushiClashInstrumentedTestCase() {

    @Before
    fun prepareMainApp() {
        composeTestRule.skipOnboardingIfShown()
    }

    @Test
    fun givenCompletedOnboarding_whenOpeningTutorialFromSettings_thenOnboardingIsShown() {
        composeTestRule.openOnboardingFromSettings()
        composeTestRule.waitForText(R.string.onboarding_step_welcome_dialogue)
        composeTestRule.clickText(R.string.onboarding_skip)
        composeTestRule.waitForSettingsScreen()
    }

    @Test
    fun givenHistorySaved_whenClearHistoryConfirmed_thenFlowCompletesWithoutCrash() {
        composeTestRule.clearHistoryFromSettings()
        composeTestRule.waitForText(R.string.settings_screen_title)
    }

    @Test
    fun givenAchievementsProgress_whenClearAchievementsConfirmed_thenFlowCompletesWithoutCrash() {
        composeTestRule.clearAchievementsFromSettings()
        composeTestRule.waitForText(R.string.settings_screen_title)
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
}
