package com.mancebolabs.sushiclash.feature.achievements

import com.mancebolabs.sushiclash.domain.model.achievement.AchievementUnlock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

object AchievementNotificationDispatcher {
    private val eventChannel = Channel<AchievementUnlock>(capacity = Channel.UNLIMITED)

    val events: Flow<AchievementUnlock> = eventChannel.receiveAsFlow()

    fun notify(unlock: AchievementUnlock) {
        eventChannel.trySend(unlock)
    }

    fun notifyAll(unlocks: List<AchievementUnlock>) {
        unlocks.forEach(::notify)
    }
}
