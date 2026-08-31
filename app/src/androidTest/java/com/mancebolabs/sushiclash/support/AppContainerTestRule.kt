package com.mancebolabs.sushiclash.support

import com.mancebolabs.sushiclash.di.AppContainerTestOverrides
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class AppContainerTestRule : TestWatcher() {
    override fun starting(description: Description) {
        AppContainerTestOverrides.reset()
        AppContainerTestOverrides.completeGifCyclesImmediately = true
    }

    override fun finished(description: Description) {
        AppContainerTestOverrides.reset()
    }
}
