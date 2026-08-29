package com.mancebolabs.sushiclash.feature.history

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object HistoryDateFormatter {

    fun format(timestamp: Long, locale: Locale = Locale.getDefault()): String {
        val formatter = DateTimeFormatter.ofPattern(resolvePattern(locale), locale)
        return formatter.format(
            Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()),
        )
    }

    private fun resolvePattern(locale: Locale): String {
        return when (locale.language) {
            "ja", "zh" -> "yyyy/MM/dd HH:mm"
            else -> "d MMM yyyy · HH:mm"
        }
    }
}
