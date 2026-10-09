package com.mancebolabs.sushiclash.screenshots

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.SushiClashInstrumentedTestCase
import com.mancebolabs.sushiclash.di.AppContainer
import com.mancebolabs.sushiclash.domain.model.AppLanguage
import com.mancebolabs.sushiclash.support.addWheelParticipant
import com.mancebolabs.sushiclash.support.clickText
import com.mancebolabs.sushiclash.support.configureAppContainerAndRelaunch
import com.mancebolabs.sushiclash.support.dismissChefCelebrationIfShown
import com.mancebolabs.sushiclash.support.finishActiveGameWithSaving
import com.mancebolabs.sushiclash.support.openAchievementsFromSettings
import com.mancebolabs.sushiclash.support.openHistory
import com.mancebolabs.sushiclash.support.openSettings
import com.mancebolabs.sushiclash.support.openWheel
import com.mancebolabs.sushiclash.support.recreateActivity
import com.mancebolabs.sushiclash.support.selectHistorySection
import com.mancebolabs.sushiclash.support.skipOnboardingIfShown
import com.mancebolabs.sushiclash.support.startGroupGame
import com.mancebolabs.sushiclash.support.startSoloGame
import com.mancebolabs.sushiclash.support.tapPlayerSushi
import com.mancebolabs.sushiclash.support.tapSushi
import com.mancebolabs.sushiclash.support.waitForMainShell
import com.mancebolabs.sushiclash.support.waitForText
import com.mancebolabs.sushiclash.support.waitUntilAchievementBannerGone
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented suite that captures store/README PNGs for each supported locale.
 *
 * Run alone via Gradle filter on this class; English only via
 * `StoreScreenshotTest#captureEnglishScreenshots`.
 *
 * Output on device under getExternalFilesDir("screenshots")/<locale>/.
 */
@RunWith(AndroidJUnit4::class)
class StoreScreenshotTest : SushiClashInstrumentedTestCase() {

    @Test
    fun captureEnglishScreenshots() {
        captureScreenshotsFor(AppLanguage.ENGLISH)
    }

    @Test
    fun captureSpanishScreenshots() {
        captureScreenshotsFor(AppLanguage.SPANISH)
    }

    @Test
    fun captureGermanScreenshots() {
        captureScreenshotsFor(AppLanguage.GERMAN)
    }

    @Test
    fun captureFrenchScreenshots() {
        captureScreenshotsFor(AppLanguage.FRENCH)
    }

    @Test
    fun captureChineseScreenshots() {
        captureScreenshotsFor(AppLanguage.CHINESE_SIMPLIFIED)
    }

    @Test
    fun captureJapaneseScreenshots() {
        captureScreenshotsFor(AppLanguage.JAPANESE)
    }

    private fun captureScreenshotsFor(language: AppLanguage) {
        val locale = ScreenshotDemoState.localeFolder(language)
        val names = ScreenshotDemoState.demoPlayerNames(language)

        prepareLocaleForOnboarding(language)
        capture(locale, "01_onboarding")

        composeTestRule.skipOnboardingIfShown()
        suppressChefOverlays()

        composeTestRule.startSoloGame()
        composeTestRule.tapSushi(times = 12)
        composeTestRule.waitForIdle()
        composeTestRule.waitUntilAchievementBannerGone()
        capture(locale, "02_solo")
        composeTestRule.finishActiveGameWithSaving(dismissFinishCelebration = true)
        composeTestRule.dismissChefCelebrationIfShown()
        composeTestRule.waitUntilAchievementBannerGone()
        composeTestRule.waitForIdle()

        composeTestRule.startGroupGame(names)
        composeTestRule.tapPlayerSushi(names[0], times = 3)
        composeTestRule.tapPlayerSushi(names[1], times = 7)
        composeTestRule.tapPlayerSushi(names[2], times = 5)
        composeTestRule.tapPlayerSushi(names[3], times = 2)
        composeTestRule.waitForIdle()
        composeTestRule.waitUntilAchievementBannerGone()
        capture(locale, "03_group")
        composeTestRule.finishActiveGameWithSaving(dismissFinishCelebration = true)
        composeTestRule.dismissChefCelebrationIfShown()
        composeTestRule.waitUntilAchievementBannerGone()
        composeTestRule.waitForIdle()

        composeTestRule.openWheel()
        names.forEach { name ->
            composeTestRule.addWheelParticipant(name)
        }
        composeTestRule.waitForText(R.string.wheel_spin)
        composeTestRule.waitForIdle()
        capture(locale, "04_roulette")

        composeTestRule.openAchievementsFromSettings()
        composeTestRule.waitForIdle()
        capture(locale, "05_achievements")
        composeTestRule.clickText(R.string.achievements_back)
        composeTestRule.waitForIdle()

        composeTestRule.openHistory()
        composeTestRule.waitForText(R.string.history_section_solo)
        // Group rankings with multiple players are more representative for store shots.
        composeTestRule.selectHistorySection(R.string.history_section_group)
        composeTestRule.waitForIdle()
        capture(locale, "06_history")

        // Re-enable so Settings shows the default ON toggle; keep disabled during counting.
        ScreenshotDemoState.enableRandomChefAnimations(
            InstrumentationRegistry.getInstrumentation().targetContext,
        )
        composeTestRule.openSettings()
        composeTestRule.waitForText(R.string.settings_screen_title)
        composeTestRule.waitForIdle()
        capture(locale, "07_settings")
    }

    /**
     * Sets the app language while onboarding is still pending (fresh package data).
     *
     * Prefer waiting for the welcome string after [AppCompatDelegate.setApplicationLocales]
     * without a second forced recreate — a double recreate can flake.
     */
    private fun prepareLocaleForOnboarding(language: AppLanguage) {
        AppContainer.languageRepository().setAppLanguage(language)
        // Avoid waitForIdle here: AppCompat locale apply recreates while onboarding GIFs
        // keep Choreographer busy and Espresso never idles.
        Thread.sleep(500L)

        val welcomeVisible = runCatching {
            composeTestRule.waitForText(R.string.onboarding_step_welcome_dialogue, timeoutMillis = 8_000L)
            true
        }.getOrDefault(false)

        if (!welcomeVisible) {
            composeTestRule.recreateActivity()
            composeTestRule.waitForText(R.string.onboarding_step_welcome_dialogue)
        }
    }

    private fun suppressChefOverlays() {
        ScreenshotDemoState.disableRandomChefAnimations(
            InstrumentationRegistry.getInstrumentation().targetContext,
        )
        // Rebuild GameRepository with a deterministic high chef target; preference disable
        // is the primary suppression path for counts above the 3..5 interval.
        composeTestRule.configureAppContainerAndRelaunch {
            chefRandomProvider = ScreenshotDemoState.highChefTargetRandomProvider()
        }
        composeTestRule.waitForMainShell()
        composeTestRule.waitForText(R.string.counter_start_game)
    }

    private fun capture(locale: String, filename: String) {
        ScreenshotCapture.capture(
            localeFolder = locale,
            filename = filename,
            waitForIdle = {
                // Best-effort only; looping chef GIFs may never report idle.
                runCatching { composeTestRule.waitForIdle() }
            },
        )
    }
}
