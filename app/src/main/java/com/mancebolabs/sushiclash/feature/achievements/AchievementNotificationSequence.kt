package com.mancebolabs.sushiclash.feature.achievements

import com.mancebolabs.sushiclash.domain.model.achievement.AchievementUnlock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select

const val ACHIEVEMENT_NOTIFICATION_DISPLAY_MS = 3_000L
const val ACHIEVEMENT_NOTIFICATION_EXIT_ANIMATION_MS = 300L

data class AchievementNotificationDisplayState(
    val unlock: AchievementUnlock?,
    val visible: Boolean,
)

/**
 * Processes achievement unlock events sequentially so each notification completes its own
 * enter → visible → exit cycle before the next one is shown.
 */
class AchievementNotificationSequenceProcessor(
    private val displayDurationMs: Long = ACHIEVEMENT_NOTIFICATION_DISPLAY_MS,
    private val exitAnimationDurationMs: Long = ACHIEVEMENT_NOTIFICATION_EXIT_ANIMATION_MS,
) {
    private val dismissSignal = Channel<Unit>(Channel.CONFLATED)

    fun requestDismiss() {
        dismissSignal.trySend(Unit)
    }

    suspend fun process(
        events: Flow<AchievementUnlock>,
        blockingPresentations: Flow<Boolean> = flowOf(false),
        onFeedback: suspend (AchievementUnlock) -> Unit,
        onStateChange: suspend (AchievementNotificationDisplayState) -> Unit,
    ) {
        events.collect { unlock ->
            waitUntilBlockingPresentationInactive(blockingPresentations)

            drainPendingDismissSignals()

            onFeedback(unlock)
            onStateChange(AchievementNotificationDisplayState(unlock = unlock, visible = true))
            waitForDisplayEnd()
            onStateChange(AchievementNotificationDisplayState(unlock = unlock, visible = false))
            delay(exitAnimationDurationMs)
            onStateChange(AchievementNotificationDisplayState(unlock = null, visible = false))
        }
    }

    private suspend fun waitForDisplayEnd() {
        select {
            dismissSignal.onReceive { }
            onTimeout(displayDurationMs) { }
        }
    }

    private fun drainPendingDismissSignals() {
        while (dismissSignal.tryReceive().isSuccess) {
            // Drop stale dismiss requests from a previous notification cycle.
        }
    }

    private suspend fun waitUntilBlockingPresentationInactive(
        blockingPresentations: Flow<Boolean>,
    ) {
        if (!blockingPresentations.first()) return

        blockingPresentations.first { !it }
    }
}
