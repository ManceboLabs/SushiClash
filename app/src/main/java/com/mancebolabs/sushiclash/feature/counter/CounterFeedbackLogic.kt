package com.mancebolabs.sushiclash.feature.counter

import com.mancebolabs.sushiclash.domain.model.IncrementResult

internal fun wasCounterIncrementSuccessful(
    previousCount: Int?,
    result: IncrementResult,
): Boolean {
    if (previousCount == null) return false
    return result.newCount == previousCount + 1
}
