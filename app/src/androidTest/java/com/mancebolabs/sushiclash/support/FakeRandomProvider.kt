package com.mancebolabs.sushiclash.support

import com.mancebolabs.sushiclash.domain.model.RandomProvider
import java.util.ArrayDeque

class FakeRandomProvider(
    private val values: ArrayDeque<Int> = ArrayDeque(),
) : RandomProvider {
    override fun nextInt(from: Int, until: Int): Int {
        require(from < until) { "Invalid range: $from until $until" }
        return if (values.isEmpty()) {
            from
        } else {
            values.removeFirst().also { value ->
                require(value in from until until) {
                    "Queued value $value is outside range [$from, $until)"
                }
            }
        }
    }

    fun enqueue(value: Int) {
        values.addLast(value)
    }
}
