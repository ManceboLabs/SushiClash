package com.mancebolabs.sushiclash.screenshots

import android.content.Context
import androidx.annotation.StringRes
import com.mancebolabs.sushiclash.di.AppContainer
import com.mancebolabs.sushiclash.domain.model.AppLanguage
import com.mancebolabs.sushiclash.domain.model.ChefAnimationTriggerLogic
import com.mancebolabs.sushiclash.support.FakeRandomProvider
import com.mancebolabs.sushiclash.support.SushiClashComposeTestRule
import com.mancebolabs.sushiclash.support.recreateActivity
import com.mancebolabs.sushiclash.support.waitForText
import kotlinx.coroutines.runBlocking

/**
 * Shared demo data and setup helpers for deterministic store/README screenshot captures.
 *
 * Locale folder names match [AppLanguage.languageTag] for the six selectable languages:
 * `en`, `es`, `de`, `fr`, `zh-CN`, `ja`.
 *
 * ## Suppressing random chef overlays
 *
 * Callers should prepare the app before seeding game state:
 * 1. Prefer [ScreenshotDemoState.disableRandomChefAnimations] so the persisted preference
 *    blocks overlays.
 * 2. Set `AppContainerTestOverrides.completeGifCyclesImmediately = true` so any celebration
 *    GIF can be dismissed without waiting for full playback (also applied by
 *    `AppContainerTestRule` / `configureAppContainerAndRelaunch`).
 * 3. Optionally assign [ScreenshotDemoState.highChefTargetRandomProvider] to
 *    `AppContainerTestOverrides.chefRandomProvider` before the activity builds
 *    `AppContainer.gameRepository`. Initial targets only fall in
 *    `[ChefAnimationTriggerLogic.MIN_INTERVAL, ChefAnimationTriggerLogic.MAX_INTERVAL]`
 *    (3..5), so preference disable is the reliable suppression path for counts above that.
 */
object ScreenshotDemoState {

    /** Selectable screenshot locales in generation order. */
    val screenshotLanguages: List<AppLanguage> = listOf(
        AppLanguage.ENGLISH,
        AppLanguage.SPANISH,
        AppLanguage.GERMAN,
        AppLanguage.FRENCH,
        AppLanguage.CHINESE_SIMPLIFIED,
        AppLanguage.JAPANESE,
    )

    fun localeFolder(language: AppLanguage): String {
        return requireNotNull(language.languageTag) {
            "Screenshot locale requires a non-null languageTag; got $language"
        }
    }

    /**
     * Short group-player / wheel-participant names per locale.
     * ASCII for Latin locales; natural CJK for zh-CN / ja.
     */
    fun demoPlayerNames(language: AppLanguage): List<String> {
        return when (language) {
            AppLanguage.ENGLISH -> listOf("Alex", "Sam", "Riley", "Jordan")
            AppLanguage.SPANISH -> listOf("Alex", "Sam", "Luis", "Ana")
            AppLanguage.GERMAN -> listOf("Alex", "Sam", "Lea", "Max")
            AppLanguage.FRENCH -> listOf("Alex", "Sam", "Leo", "Mia")
            AppLanguage.CHINESE_SIMPLIFIED -> listOf("小明", "小红", "阿杰", "小美")
            AppLanguage.JAPANESE -> listOf("アキ", "ユキ", "ケン", "ミカ")
            AppLanguage.SYSTEM -> demoPlayerNames(AppLanguage.ENGLISH)
        }
    }

    /**
     * Disables random chef animations in persisted feedback settings.
     *
     * Uses [runBlocking] because the repository API is suspending; safe on the
     * instrumented-test thread before UI interaction.
     */
    fun disableRandomChefAnimations(context: Context) {
        runBlocking {
            AppContainer.feedbackSettingsRepository(context)
                .setRandomChefAnimationsEnabled(false)
        }
    }

    /**
     * Re-enables random chef animations so Settings shows the default ON toggle.
     * Call only after counting phases that need overlays suppressed.
     */
    fun enableRandomChefAnimations(context: Context) {
        runBlocking {
            AppContainer.feedbackSettingsRepository(context)
                .setRandomChefAnimationsEnabled(true)
        }
    }

    /**
     * Deterministic chef [FakeRandomProvider] that always returns the max initial interval (5).
     * Prefer [disableRandomChefAnimations] for suppressing overlays during long demo counts.
     */
    fun highChefTargetRandomProvider(
        queuedValues: Int = ChefAnimationTriggerLogic.MAX_INTERVAL,
        enqueueCount: Int = 32,
    ): FakeRandomProvider {
        require(queuedValues in ChefAnimationTriggerLogic.MIN_INTERVAL..ChefAnimationTriggerLogic.MAX_INTERVAL) {
            "Chef initial targets must be in " +
                "${ChefAnimationTriggerLogic.MIN_INTERVAL}..${ChefAnimationTriggerLogic.MAX_INTERVAL}"
        }
        return FakeRandomProvider().apply {
            repeat(enqueueCount) { enqueue(queuedValues) }
        }
    }
}

/**
 * Applies [language] via [AppContainer.languageRepository], recreates the activity, and
 * waits until [translatedStringRes] is visible so strings match the new locale.
 */
fun SushiClashComposeTestRule.applyAppLanguage(
    language: AppLanguage,
    @StringRes translatedStringRes: Int,
) {
    AppContainer.languageRepository().setAppLanguage(language)
    recreateActivity()
    waitForText(translatedStringRes)
}
