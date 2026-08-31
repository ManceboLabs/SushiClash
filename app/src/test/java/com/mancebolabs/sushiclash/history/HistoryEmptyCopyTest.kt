package com.mancebolabs.sushiclash.history

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import com.mancebolabs.sushiclash.feature.history.shouldShowHistoryEmptyCopy

class HistoryEmptyCopyTest {

    @Test
    fun givenPersistenceErrorWithoutItems_whenCheckingEmptyCopy_thenHidesEmptyState() {
        assertFalse(shouldShowHistoryEmptyCopy(persistenceError = true, hasItems = false))
    }

    @Test
    fun givenHealthyHistoryWithoutItems_whenCheckingEmptyCopy_thenShowsEmptyState() {
        assertTrue(shouldShowHistoryEmptyCopy(persistenceError = false, hasItems = false))
    }
}
