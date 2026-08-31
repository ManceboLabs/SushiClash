package com.mancebolabs.sushiclash.ui.theme

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class FloatingNavBarMetricsTest {

    @Test
    fun givenItemIndex_whenIndicatorOffsetCalculated_thenCentersOnItem() {
        assertEquals(4.dp, FloatingNavBarMetrics.indicatorOffsetForIndex(0))
        assertEquals(70.dp, FloatingNavBarMetrics.indicatorOffsetForIndex(1))
        assertEquals(136.dp, FloatingNavBarMetrics.indicatorOffsetForIndex(2))
        assertEquals(202.dp, FloatingNavBarMetrics.indicatorOffsetForIndex(3))
    }
}
