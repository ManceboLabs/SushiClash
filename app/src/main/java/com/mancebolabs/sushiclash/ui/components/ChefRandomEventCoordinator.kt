package com.mancebolabs.sushiclash.ui.components

import com.mancebolabs.sushiclash.domain.model.ChefEventAnimation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ActiveChefRandomEvent(
    val animation: ChefEventAnimation,
    val onComplete: () -> Unit,
)

/**
 * Presents blocking chef random events at app-shell level so overlays cover destinations
 * and the floating bottom navigation bar.
 */
object ChefRandomEventCoordinator {
    private val _activeEvent = MutableStateFlow<ActiveChefRandomEvent?>(null)
    private val _isBlockingPresentationActive = MutableStateFlow(false)

    val activeEvent: StateFlow<ActiveChefRandomEvent?> = _activeEvent.asStateFlow()

    val isBlockingPresentationActive: StateFlow<Boolean> =
        _isBlockingPresentationActive.asStateFlow()

    internal fun activate(
        animation: ChefEventAnimation,
        onComplete: () -> Unit,
    ) {
        _activeEvent.value = ActiveChefRandomEvent(
            animation = animation,
            onComplete = onComplete,
        )
        _isBlockingPresentationActive.value = true
    }

    internal fun complete() {
        val event = _activeEvent.value ?: return
        _activeEvent.value = null
        _isBlockingPresentationActive.value = false
        event.onComplete()
    }

    internal fun deactivate() {
        _activeEvent.value = null
        _isBlockingPresentationActive.value = false
    }
}
