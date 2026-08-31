package com.mancebolabs.sushiclash.support

import androidx.annotation.StringRes
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.mancebolabs.sushiclash.MainActivity
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.feature.onboarding.defaultOnboardingSteps

typealias SushiClashComposeTestRule =
    AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>

fun SushiClashComposeTestRule.string(@StringRes resId: Int): String {
    return activity.getString(resId)
}

fun SushiClashComposeTestRule.waitForText(
    @StringRes resId: Int,
    timeoutMillis: Long = 15_000L,
) {
    val text = string(resId)
    waitUntil(timeoutMillis) {
        onAllNodesWithText(text, useUnmergedTree = true)
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
            .isNotEmpty()
    }
}

fun SushiClashComposeTestRule.waitForAnyText(
    vararg resIds: Int,
    timeoutMillis: Long = 15_000L,
) {
    waitUntil(timeoutMillis) {
        resIds.any { resId ->
            val text = string(resId)
            onAllNodesWithText(text, useUnmergedTree = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
    }
}

fun SushiClashComposeTestRule.onText(@StringRes resId: Int): SemanticsNodeInteraction {
    return onNodeWithText(string(resId), useUnmergedTree = true)
}

fun SushiClashComposeTestRule.clickText(@StringRes resId: Int) {
    onText(resId).performClick()
}

fun SushiClashComposeTestRule.clickNav(@StringRes contentDescriptionResId: Int) {
    onNodeWithContentDescription(string(contentDescriptionResId), useUnmergedTree = true)
        .performClick()
}

fun SushiClashComposeTestRule.waitForMainShell(timeoutMillis: Long = 15_000L) {
    waitUntil(timeoutMillis) {
        onAllNodesWithContentDescription(string(R.string.nav_counter), useUnmergedTree = true)
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
            .isNotEmpty()
    }
}

fun SushiClashComposeTestRule.skipOnboardingIfShown() {
    waitForAnyText(
        R.string.onboarding_skip,
        R.string.counter_start_game,
        R.string.counter_no_active_game_title,
    )

    val skipVisible = onAllNodesWithText(string(R.string.onboarding_skip), useUnmergedTree = true)
        .fetchSemanticsNodes(atLeastOneRootRequired = false)
        .isNotEmpty()
    if (skipVisible) {
        clickText(R.string.onboarding_skip)
    }

    waitForText(R.string.counter_start_game)
}

fun SushiClashComposeTestRule.completeOnboarding() {
    waitForText(R.string.onboarding_step_welcome_dialogue)

    while (
        onAllNodesWithText(string(R.string.onboarding_next), useUnmergedTree = true)
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
            .isNotEmpty()
    ) {
        clickText(R.string.onboarding_next)
        waitForIdle()
    }

    clickText(R.string.onboarding_finish)
    waitForText(R.string.counter_start_game)
}

fun SushiClashComposeTestRule.navigateToLastOnboardingStep() {
    waitForText(R.string.onboarding_step_welcome_dialogue)

    repeat(defaultOnboardingSteps().lastIndex) {
        clickText(R.string.onboarding_next)
        waitForIdle()
    }

    waitForText(R.string.onboarding_step_responsible_use_title)
}

fun SushiClashComposeTestRule.startSoloGame() {
    skipOnboardingIfShown()
    clickText(R.string.counter_start_game)
    waitForText(R.string.setup_solo_title)
    clickText(R.string.setup_solo_title)
    clickText(R.string.setup_start)
    waitForText(R.string.counter_finish_game)
}

fun SushiClashComposeTestRule.tapSushi(times: Int = 1) {
    repeat(times) {
        onNodeWithContentDescription(string(R.string.counter_sushi_content_description), useUnmergedTree = true)
            .performClick()
        waitForIdle()
    }
}

fun SushiClashComposeTestRule.finishActiveGameWithoutSaving() {
    clickText(R.string.counter_finish_game)
    waitForText(R.string.finish_game_title)
    clickText(R.string.finish_game_skip_save)
    waitForText(R.string.counter_no_active_game_title)
}

fun SushiClashComposeTestRule.finishActiveGameWithSaving() {
    clickText(R.string.counter_finish_game)
    waitForText(R.string.finish_game_title)
    clickText(R.string.finish_game_save)
    waitForText(R.string.counter_no_active_game_title)
}

fun SushiClashComposeTestRule.openSettings() {
    clickNav(R.string.nav_settings)
    waitForText(R.string.settings_screen_title)
}

fun SushiClashComposeTestRule.openLanguagePicker() {
    onAllNodesWithText(string(R.string.settings_language_section), useUnmergedTree = true)[1]
        .performScrollTo()
        .performClick()
    waitForText(R.string.settings_language_picker_title)
}

fun SushiClashComposeTestRule.selectTheme(@StringRes themeLabelResId: Int) {
    onText(themeLabelResId)
        .performScrollTo()
        .performClick()
    waitForIdle()
}
