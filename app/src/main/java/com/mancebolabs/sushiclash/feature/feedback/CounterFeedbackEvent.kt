package com.mancebolabs.sushiclash.feature.feedback

sealed interface CounterFeedbackEvent {
    data object SushiIncrement : CounterFeedbackEvent

    data object RouletteTriggered : CounterFeedbackEvent
}
