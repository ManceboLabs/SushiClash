package com.mancebolabs.sushiclash.support

import androidx.annotation.StringRes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.mancebolabs.sushiclash.MainActivity
import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.di.AppContainerTestOverrides
import com.mancebolabs.sushiclash.domain.model.ChefAnimationTriggerLogic
import com.mancebolabs.sushiclash.domain.model.GameSetupRules
import com.mancebolabs.sushiclash.feature.onboarding.ONBOARDING_PAGER_TEST_TAG
import com.mancebolabs.sushiclash.feature.onboarding.defaultOnboardingSteps
import com.mancebolabs.sushiclash.testing.SushiClashTestTags

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

fun SushiClashComposeTestRule.onText(@StringRes resId: Int) =
    onNodeWithText(string(resId), useUnmergedTree = true)

fun SushiClashComposeTestRule.clickText(@StringRes resId: Int) {
    onText(resId).performClick()
}

fun SushiClashComposeTestRule.clickFirstText(@StringRes resId: Int) {
    onAllNodesWithText(string(resId), useUnmergedTree = true)[0].performClick()
}

fun SushiClashComposeTestRule.typeIntoPlayerNameField(text: String) {
    onNodeWithTag(SushiClashTestTags.PLAYER_NAME_INPUT, useUnmergedTree = true)
        .performClick()
    onNodeWithTag(SushiClashTestTags.PLAYER_NAME_INPUT, useUnmergedTree = true)
        .performTextInput(text)
}

fun SushiClashComposeTestRule.typeIntoWheelParticipantField(text: String) {
    onNodeWithTag(SushiClashTestTags.WHEEL_PARTICIPANT_NAME_INPUT, useUnmergedTree = true)
        .performClick()
    onNodeWithTag(SushiClashTestTags.WHEEL_PARTICIPANT_NAME_INPUT, useUnmergedTree = true)
        .performTextInput(text)
}

fun SushiClashComposeTestRule.scrollToAndClickFirstText(@StringRes resId: Int) {
    onAllNodesWithText(string(resId), useUnmergedTree = true)[0]
        .performScrollTo()
        .performClick()
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

fun SushiClashComposeTestRule.configureAppContainerAndRelaunch(
    configure: AppContainerTestOverrides.() -> Unit = {},
) {
    AppContainerTestOverrides.apply {
        reset()
        completeGifCyclesImmediately = true
        configure()
    }
    activityRule.scenario.recreate()
    waitForIdle()
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

fun SushiClashComposeTestRule.swipeOnboardingPagerLeft() {
    onNodeWithTag(ONBOARDING_PAGER_TEST_TAG)
        .performTouchInput {
            val y = center.y
            down(Offset(right - 1f, y))
            moveTo(Offset(left + 1f, y), 200)
            up()
        }
    waitForIdle()
}

fun SushiClashComposeTestRule.swipeOnboardingPagerRight() {
    onNodeWithTag(ONBOARDING_PAGER_TEST_TAG)
        .performTouchInput {
            val y = center.y
            down(Offset(left + 1f, y))
            moveTo(Offset(right - 1f, y), 200)
            up()
        }
    waitForIdle()
}

fun SushiClashComposeTestRule.assertOnboardingProgressDotSelected(stepIndex: Int) {
    onNodeWithTag(SushiClashTestTags.onboardingProgressDot(stepIndex), useUnmergedTree = true)
        .assertIsSelected()
}

fun SushiClashComposeTestRule.assertBottomNavVisible() {
    onNodeWithTag(SushiClashTestTags.BOTTOM_NAV_BAR, useUnmergedTree = true)
        .assertIsDisplayed()
}

fun SushiClashComposeTestRule.assertBottomNavHidden() {
    onAllNodesWithTag(SushiClashTestTags.BOTTOM_NAV_BAR, useUnmergedTree = true)
        .fetchSemanticsNodes(atLeastOneRootRequired = false)
        .also { nodes ->
            check(nodes.isEmpty()) { "Bottom navigation should be hidden during onboarding" }
        }
}

fun SushiClashComposeTestRule.openOnboardingFromSettings() {
    openSettings()
    onNodeWithTag(SushiClashTestTags.SETTINGS_VIEW_TUTORIAL_ROW, useUnmergedTree = true)
        .performScrollTo()
        .performClick()
    waitForText(R.string.onboarding_step_welcome_dialogue)
}

fun SushiClashComposeTestRule.clickSettingsDestructiveAction(@StringRes labelResId: Int) {
    val nodes = onAllNodesWithText(string(labelResId), useUnmergedTree = true)
    nodes[nodes.fetchSemanticsNodes().size - 1]
        .performScrollTo()
        .performClick()
}

fun SushiClashComposeTestRule.clearHistoryFromSettings() {
    openSettings()
    clickSettingsDestructiveAction(R.string.settings_clear_history)
    waitForText(R.string.settings_clear_history_title)
    clickFirstText(R.string.settings_clear_history_confirm)
    waitForIdle()
}

fun SushiClashComposeTestRule.clearAchievementsFromSettings() {
    openSettings()
    clickSettingsDestructiveAction(R.string.settings_clear_achievements)
    waitForText(R.string.settings_clear_achievements_title)
    clickFirstText(R.string.settings_clear_achievements_confirm)
    waitForIdle()
}

fun SushiClashComposeTestRule.startSoloGame(dismissStartCelebration: Boolean = true) {
    skipOnboardingIfShown()
    clickText(R.string.counter_start_game)
    waitForText(R.string.setup_solo_title)
    clickText(R.string.setup_solo_title)
    clickText(R.string.setup_start)
    waitForText(R.string.counter_finish_game)
    if (dismissStartCelebration) {
        dismissChefCelebrationIfShown()
    }
}

fun SushiClashComposeTestRule.startGroupGame(
    playerNames: List<String>,
    dismissStartCelebration: Boolean = true,
) {
    require(playerNames.size >= GameSetupRules.MIN_GROUP_PLAYERS) {
        "Group setup requires at least ${GameSetupRules.MIN_GROUP_PLAYERS} players"
    }

    skipOnboardingIfShown()
    clickText(R.string.counter_start_game)
    waitForText(R.string.setup_group_title)
    clickText(R.string.setup_group_title)

    playerNames.forEach { name ->
        addGroupPlayer(name)
    }

    clickText(R.string.setup_start)
    waitForText(R.string.counter_finish_game)
    dismissChefCelebrationIfShown()
    playerNames.forEach { name ->
        waitUntil(15_000L) {
            onAllNodesWithText(name, useUnmergedTree = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
    }
}

fun SushiClashComposeTestRule.addGroupPlayer(name: String) {
    typeIntoPlayerNameField(name)
    onNodeWithTag(SushiClashTestTags.SETUP_ADD_PLAYER_BUTTON, useUnmergedTree = true)
        .performClick()
    waitForIdle()
}

fun SushiClashComposeTestRule.tapSushi(times: Int = 1) {
    repeat(times) {
        onNodeWithContentDescription(string(R.string.counter_sushi_content_description), useUnmergedTree = true)
            .performClick()
        waitForIdle()
    }
}

fun SushiClashComposeTestRule.tapPlayerSushi(playerName: String, times: Int = 1) {
    repeat(times) {
        onNodeWithTag(SushiClashTestTags.groupPlayerButton(playerName), useUnmergedTree = true)
            .performClick()
        waitForIdle()
    }
}

fun SushiClashComposeTestRule.longPressResetPlayer(playerName: String) {
    onNodeWithTag(SushiClashTestTags.groupPlayerButton(playerName), useUnmergedTree = true)
        .performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100)
            up()
        }
    waitForText(R.string.counter_reset_title)
    clickText(R.string.counter_reset_confirm)
    waitForIdle()
}

fun SushiClashComposeTestRule.assertSoloCount(expected: Int) {
    onNodeWithTag(SushiClashTestTags.SOLO_SUSHI_COUNT, useUnmergedTree = true)
        .assert(hasText(expected.toString()))
}

fun SushiClashComposeTestRule.assertGroupPlayerCount(playerName: String, expected: Int) {
    onNodeWithTag(SushiClashTestTags.groupPlayerCount(playerName), useUnmergedTree = true)
        .assert(hasText(expected.toString()))
}

fun SushiClashComposeTestRule.finishActiveGameWithoutSaving() {
    clickText(R.string.counter_finish_game)
    waitForText(R.string.finish_game_title)
    clickText(R.string.finish_game_skip_save)
    waitForText(R.string.counter_no_active_game_title)
}

fun SushiClashComposeTestRule.finishActiveGameWithSaving(dismissFinishCelebration: Boolean = true) {
    clickText(R.string.counter_finish_game)
    waitForText(R.string.finish_game_title)
    clickText(R.string.finish_game_save)
    if (dismissFinishCelebration) {
        waitUntil(15_000L) {
            onAllNodesWithText(string(R.string.game_finish_speech), useUnmergedTree = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty() ||
                onAllNodesWithText(string(R.string.counter_no_active_game_title), useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false)
                    .isNotEmpty()
        }
        dismissChefCelebrationIfShown()
    }
    waitForText(R.string.counter_no_active_game_title)
}

fun SushiClashComposeTestRule.finishGroupGameAndOpenHistory() {
    finishActiveGameWithSaving()
    recreateActivity()
    waitForMainShell()
    openHistory()
}

fun SushiClashComposeTestRule.finishActiveGameCancel() {
    clickText(R.string.counter_finish_game)
    waitForText(R.string.finish_game_title)
    clickText(R.string.counter_cancel)
    waitForIdle()
}

fun SushiClashComposeTestRule.waitForSettingsScreen(timeoutMillis: Long = 15_000L) {
    waitUntil(timeoutMillis) {
        onAllNodesWithTag(SushiClashTestTags.SETTINGS_VIEW_TUTORIAL_ROW, useUnmergedTree = true)
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
            .isNotEmpty()
    }
}

fun SushiClashComposeTestRule.openSettings() {
    clickNav(R.string.nav_settings)
    waitForSettingsScreen()
}

fun SushiClashComposeTestRule.openHistory() {
    clickNav(R.string.nav_history)
    waitUntil(15_000L) {
        onAllNodesWithTag(SushiClashTestTags.HISTORY_SECTION_GROUP, useUnmergedTree = true)
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
            .isNotEmpty()
    }
}

fun SushiClashComposeTestRule.openWheel() {
    clickNav(R.string.nav_wheel)
    waitForText(R.string.wheel_screen_title)
}

fun SushiClashComposeTestRule.openAchievementsFromSettings() {
    openSettings()
    onAllNodesWithText(string(R.string.settings_view_achievements_description), useUnmergedTree = true)[0]
        .performScrollTo()
        .performClick()
    waitForText(R.string.achievements_screen_title)
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

fun SushiClashComposeTestRule.dismissChefCelebrationIfShown() {
    val okLabel = string(R.string.wheel_ok)
    val hasCelebration = onAllNodesWithText(okLabel, useUnmergedTree = true)
        .fetchSemanticsNodes(atLeastOneRootRequired = false)
        .isNotEmpty()
    if (hasCelebration) {
        onNodeWithText(okLabel, useUnmergedTree = true).performClick()
        waitForIdle()
    }
}

fun SushiClashComposeTestRule.waitForChefCelebrationSpeech(@StringRes speechResId: Int) {
    waitForText(speechResId)
}

fun SushiClashComposeTestRule.assertChefCelebrationSpeechNotShown(@StringRes speechResId: Int) {
    onAllNodesWithText(string(speechResId), useUnmergedTree = true)
        .fetchSemanticsNodes(atLeastOneRootRequired = false)
        .also { nodes ->
            check(nodes.isEmpty()) { "Chef celebration speech should not be visible" }
        }
}

fun SushiClashComposeTestRule.waitForChefRandomEventOverlay(timeoutMillis: Long = 15_000L) {
    waitUntil(timeoutMillis) {
        onAllNodesWithTag(SushiClashTestTags.CHEF_RANDOM_EVENT_OVERLAY, useUnmergedTree = true)
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
            .isNotEmpty()
    }
}

fun SushiClashComposeTestRule.assertChefRandomEventOverlayHidden() {
    waitUntil(1_000L) {
        onAllNodesWithTag(SushiClashTestTags.CHEF_RANDOM_EVENT_OVERLAY, useUnmergedTree = true)
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
            .isEmpty()
    }
}

fun SushiClashComposeTestRule.waitForDisplayedText(text: String, timeoutMillis: Long = 15_000L) {
    waitUntil(timeoutMillis) {
        onAllNodesWithText(text, useUnmergedTree = true)
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
            .isNotEmpty()
    }
}

fun SushiClashComposeTestRule.selectHistorySection(@StringRes sectionResId: Int) {
    val tag = when (sectionResId) {
        R.string.history_section_solo -> SushiClashTestTags.HISTORY_SECTION_SOLO
        R.string.history_section_group -> SushiClashTestTags.HISTORY_SECTION_GROUP
        else -> error("Unsupported history section: $sectionResId")
    }
    onNodeWithTag(tag, useUnmergedTree = true)
        .performScrollTo()
        .performClick()
    if (sectionResId == R.string.history_section_group) {
        waitUntil(5_000L) {
            onAllNodesWithText(string(R.string.history_solo_empty), useUnmergedTree = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isEmpty()
        }
    } else {
        waitUntil(5_000L) {
            onAllNodesWithText(string(R.string.history_solo_empty), useUnmergedTree = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty() ||
                onAllNodesWithTag(SushiClashTestTags.HISTORY_SECTION_SOLO, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false)
                    .isNotEmpty()
        }
    }
    waitForIdle()
}

fun SushiClashComposeTestRule.waitForGroupHistoryEntry(
    playerName: String,
    bestScore: Int,
    timeoutMillis: Long = 20_000L,
) {
    selectHistorySection(R.string.history_section_group)
    val bestScoreLabel = activity.getString(R.string.history_group_best_score, bestScore)
    waitUntil(timeoutMillis) {
        onAllNodesWithText(bestScoreLabel, useUnmergedTree = true)
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
            .isNotEmpty()
    }
    onNodeWithTag(SushiClashTestTags.groupHistoryPlayerName(playerName), useUnmergedTree = true)
        .performScrollTo()
        .assertIsDisplayed()
}

fun SushiClashComposeTestRule.addWheelParticipant(name: String) {
    typeIntoWheelParticipantField(name)
    clickText(R.string.wheel_add)
    waitForIdle()
}

fun SushiClashComposeTestRule.configureWheelDeterministicWinner(
    winnerIndex: Int = 0,
    extraSpins: Int = 4,
) {
    configureAppContainerAndRelaunch {
        wheelRandomProvider = FakeRandomProvider().apply {
            enqueue(winnerIndex)
            enqueue(extraSpins)
        }
        wheelSpinDurationMs = 100L
    }
}

fun SushiClashComposeTestRule.spinWheelAndWaitForWinner(winnerName: String) {
    clickText(R.string.wheel_spin)
    val winnerSpeech = activity.getString(R.string.wheel_winner_speech, winnerName)
    waitUntil(15_000L) {
        onAllNodesWithText(winnerSpeech, useUnmergedTree = true)
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
            .isNotEmpty()
    }
}

fun SushiClashComposeTestRule.waitForAchievementBanner(timeoutMillis: Long = 15_000L) {
    waitUntil(timeoutMillis) {
        onAllNodesWithText(string(R.string.achievement_unlocked_banner), useUnmergedTree = true)
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
            .isNotEmpty()
    }
}

fun SushiClashComposeTestRule.configureChefRandomTriggerAt(target: Int) {
    configureAppContainerAndRelaunch {
        chefRandomProvider = FakeRandomProvider().apply {
            enqueue(target)
            enqueue(0)
            enqueue(ChefAnimationTriggerLogic.MIN_INTERVAL)
        }
    }
}

fun SushiClashComposeTestRule.configureGroupChefRandomTriggers(vararg targets: Int) {
    configureAppContainerAndRelaunch {
        chefRandomProvider = FakeRandomProvider().apply {
            targets.forEach { enqueue(it) }
            repeat(targets.size) {
                enqueue(0)
                enqueue(ChefAnimationTriggerLogic.MIN_INTERVAL)
            }
        }
    }
}

fun SushiClashComposeTestRule.assertAddGroupPlayerDisabled() {
    onNodeWithTag(SushiClashTestTags.SETUP_ADD_PLAYER_BUTTON, useUnmergedTree = true)
        .assertIsNotEnabled()
}

fun SushiClashComposeTestRule.assertAddGroupPlayerEnabled() {
    onNodeWithTag(SushiClashTestTags.SETUP_ADD_PLAYER_BUTTON, useUnmergedTree = true)
        .assertIsEnabled()
}

fun SushiClashComposeTestRule.recreateActivity() {
    activityRule.scenario.recreate()
    waitForIdle()
}

fun SushiClashComposeTestRule.enableFixedRouletteInSetup(threshold: Int = 3) {
    onText(R.string.setup_roulette_section)
        .performScrollTo()
    onNodeWithTag(SushiClashTestTags.SETUP_RANDOM_ROULETTE_SWITCH, useUnmergedTree = true)
        .performScrollTo()
        .performClick()
    onText(R.string.setup_roulette_trigger_fixed)
        .performScrollTo()
        .performClick()
    waitForIdle()
}
