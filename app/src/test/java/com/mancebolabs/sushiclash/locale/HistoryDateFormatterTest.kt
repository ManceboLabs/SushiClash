package com.mancebolabs.sushiclash.locale

import com.mancebolabs.sushiclash.feature.history.HistoryDateFormatter
import java.util.Locale
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryDateFormatterTest {

    @Test
    fun givenEnglishLocale_whenFormattingDate_thenUsesReadablePattern() {
        val formatted = HistoryDateFormatter.format(
            timestamp = 1_700_000_000_000L,
            locale = Locale.UK,
        )

        assertTrue(formatted.contains("2023"))
    }

    @Test
    fun givenJapaneseLocale_whenFormattingDate_thenUsesCompactPattern() {
        val formatted = HistoryDateFormatter.format(
            timestamp = 1_700_000_000_000L,
            locale = Locale.JAPAN,
        )

        assertTrue(formatted.contains("/"))
        assertTrue(formatted.contains("2023"))
    }
}
