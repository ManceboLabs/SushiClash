package com.mancebolabs.sushiclash.onboarding

import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.feature.onboarding.OnboardingChefRole
import com.mancebolabs.sushiclash.feature.onboarding.OnboardingIllustration
import com.mancebolabs.sushiclash.feature.onboarding.defaultOnboardingSteps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingStepsTest {

    @Test
    fun givenDefaultSteps_whenLoaded_thenAchievementsIsBeforeResponsibleUseFinalStep() {
        val steps = defaultOnboardingSteps()

        val achievementsIndex = steps.indexOfFirst { it.titleRes == R.string.onboarding_step_achievements_title }
        val responsibleUseIndex = steps.indexOfFirst { it.titleRes == R.string.onboarding_step_responsible_use_title }

        assertEquals(8, steps.size)
        assertEquals(achievementsIndex + 1, responsibleUseIndex)
        assertEquals(steps.lastIndex, responsibleUseIndex)
    }

    @Test
    fun givenDefaultSteps_whenLoaded_thenHistoryPrecedesAchievements() {
        val steps = defaultOnboardingSteps()

        val historyIndex = steps.indexOfFirst { it.titleRes == R.string.onboarding_step_history_title }
        val achievementsIndex = steps.indexOfFirst { it.titleRes == R.string.onboarding_step_achievements_title }

        assertTrue(historyIndex < achievementsIndex)
    }

    @Test
    fun givenDefaultSteps_whenLoaded_thenResponsibleUseIsFinalStep() {
        val steps = defaultOnboardingSteps()
        val lastStep = steps.last()

        assertEquals(R.string.onboarding_step_responsible_use_title, lastStep.titleRes)
        assertEquals(R.string.onboarding_step_responsible_use_description, lastStep.descriptionRes)
        assertEquals(OnboardingChefRole.Greeting, lastStep.chefRole)
    }

    @Test
    fun givenDefaultSteps_whenLoaded_thenWelcomeUsesChefDialogueWithoutDuplicateTitle() {
        val welcome = defaultOnboardingSteps().first()

        assertEquals(R.string.onboarding_step_welcome_dialogue, welcome.dialogueRes)
        assertEquals(OnboardingChefRole.Greeting, welcome.chefRole)
        assertNull(welcome.titleRes)
        assertNull(welcome.descriptionRes)
        assertNull(welcome.illustration)
    }

    @Test
    fun givenDefaultSteps_whenLoaded_thenTutorialStepsKeepFeatureIllustrations() {
        val solo = defaultOnboardingSteps()[1]

        assertEquals(OnboardingChefRole.Tutorial, solo.chefRole)
        assertEquals(R.string.onboarding_step_solo_title, solo.titleRes)
        assertTrue(solo.illustration is OnboardingIllustration.DrawableResource)
        assertEquals(R.string.onboarding_step_solo_description, solo.descriptionRes)
    }
}
