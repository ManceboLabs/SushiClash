package com.mancebolabs.sushiclash.feature.settings

import com.mancebolabs.sushiclash.R
import com.mancebolabs.sushiclash.domain.model.AppLanguage

fun AppLanguage.labelRes(): Int {
    return when (this) {
        AppLanguage.SYSTEM -> R.string.settings_language_system
        AppLanguage.ENGLISH -> R.string.settings_language_english
        AppLanguage.SPANISH -> R.string.settings_language_spanish
        AppLanguage.GERMAN -> R.string.settings_language_german
        AppLanguage.FRENCH -> R.string.settings_language_french
        AppLanguage.CHINESE_SIMPLIFIED -> R.string.settings_language_chinese_simplified
        AppLanguage.JAPANESE -> R.string.settings_language_japanese
    }
}
