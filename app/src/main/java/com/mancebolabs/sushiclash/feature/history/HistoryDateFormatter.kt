package com.mancebolabs.sushiclash.feature.history

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HistoryDateFormatter {
    private val formatter = SimpleDateFormat("d MMM yyyy · HH:mm", Locale.forLanguageTag("es-ES"))

    fun format(timestamp: Long): String = formatter.format(Date(timestamp))
}
