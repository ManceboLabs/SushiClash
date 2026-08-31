package com.mancebolabs.sushiclash.achievement

import com.mancebolabs.sushiclash.domain.model.achievement.AchievementId
import com.mancebolabs.sushiclash.domain.model.achievement.AchievementUnlock
import com.mancebolabs.sushiclash.feature.achievements.AchievementNotificationDisplayState
import com.mancebolabs.sushiclash.feature.achievements.AchievementNotificationSequenceProcessor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AchievementNotificationSequenceTest {

    @Test
    fun givenMultipleUnlocks_whenProcessed_thenEachGetsFullDisplayCycle() = runTest {
        val processor = AchievementNotificationSequenceProcessor(
            displayDurationMs = 100,
            exitAnimationDurationMs = 50,
        )
        val feedbackEvents = mutableListOf<AchievementUnlock>()
        val stateTimeline = mutableListOf<AchievementNotificationDisplayState>()

        val unlock1 = AchievementUnlock(AchievementId.SUSHI_10, unlockedAtEpochMillis = 1L)
        val unlock2 = AchievementUnlock(AchievementId.SUSHI_20, unlockedAtEpochMillis = 2L)
        val unlock3 = AchievementUnlock(AchievementId.SUSHI_30, unlockedAtEpochMillis = 3L)

        processor.process(
            events = flowOf(unlock1, unlock2, unlock3),
            onFeedback = { feedbackEvents.add(it) },
            onStateChange = { stateTimeline.add(it) },
        )

        assertEquals(listOf(unlock1, unlock2, unlock3), feedbackEvents)
        assertEquals(
            listOf(
                AchievementNotificationDisplayState(unlock1, visible = true),
                AchievementNotificationDisplayState(unlock1, visible = false),
                AchievementNotificationDisplayState(null, visible = false),
                AchievementNotificationDisplayState(unlock2, visible = true),
                AchievementNotificationDisplayState(unlock2, visible = false),
                AchievementNotificationDisplayState(null, visible = false),
                AchievementNotificationDisplayState(unlock3, visible = true),
                AchievementNotificationDisplayState(unlock3, visible = false),
                AchievementNotificationDisplayState(null, visible = false),
            ),
            stateTimeline,
        )
    }

    @Test
    fun givenDismissRequestedDuringDisplay_whenProcessed_thenExitsEarlyAndContinuesQueue() = runTest {
        val processor = AchievementNotificationSequenceProcessor(
            displayDurationMs = 1_000,
            exitAnimationDurationMs = 50,
        )
        val stateTimeline = mutableListOf<AchievementNotificationDisplayState>()

        val unlock1 = AchievementUnlock(AchievementId.SUSHI_10, unlockedAtEpochMillis = 1L)
        val unlock2 = AchievementUnlock(AchievementId.SUSHI_20, unlockedAtEpochMillis = 2L)

        val job = launch {
            processor.process(
                events = flowOf(unlock1, unlock2),
                onFeedback = {},
                onStateChange = { stateTimeline.add(it) },
            )
        }

        testScheduler.advanceTimeBy(200)
        processor.requestDismiss()
        testScheduler.advanceTimeBy(50)
        testScheduler.advanceTimeBy(1_000)
        testScheduler.advanceTimeBy(50)
        job.join()

        assertEquals(
            listOf(
                AchievementNotificationDisplayState(unlock1, visible = true),
                AchievementNotificationDisplayState(unlock1, visible = false),
                AchievementNotificationDisplayState(null, visible = false),
                AchievementNotificationDisplayState(unlock2, visible = true),
                AchievementNotificationDisplayState(unlock2, visible = false),
                AchievementNotificationDisplayState(null, visible = false),
            ),
            stateTimeline,
        )
    }

    @Test
    fun givenBlockingChefPresentation_whenUnlockArrives_thenNotificationIsDeferredUntilBlockingEnds() = runTest {
        val blockingPresentations = MutableStateFlow(true)
        val processor = AchievementNotificationSequenceProcessor(
            displayDurationMs = 100,
            exitAnimationDurationMs = 50,
        )
        val feedbackEvents = mutableListOf<AchievementUnlock>()
        val stateTimeline = mutableListOf<AchievementNotificationDisplayState>()
        val unlock = AchievementUnlock(AchievementId.SUSHI_10, unlockedAtEpochMillis = 1L)

        val job = launch {
            processor.process(
                events = flowOf(unlock),
                blockingPresentations = blockingPresentations,
                onFeedback = { feedbackEvents.add(it) },
                onStateChange = { stateTimeline.add(it) },
            )
        }

        testScheduler.advanceTimeBy(500)
        assertTrue(feedbackEvents.isEmpty())
        assertTrue(stateTimeline.isEmpty())

        blockingPresentations.value = false
        testScheduler.advanceTimeBy(200)
        job.join()

        assertEquals(listOf(unlock), feedbackEvents)
        assertTrue(stateTimeline.any { it.unlock == unlock && it.visible })
    }
}
