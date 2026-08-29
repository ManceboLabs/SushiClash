package com.mancebolabs.sushiclash.domain.model

enum class AppLanguage(
    val languageTag: String?,
) {
    SYSTEM(null),
    ENGLISH("en"),
    SPANISH("es"),
    GERMAN("de"),
    FRENCH("fr"),
    CHINESE_SIMPLIFIED("zh-CN"),
    JAPANESE("ja"),
    ;

    companion object {
        val selectableLanguages: List<AppLanguage> = entries

        fun fromLanguageTag(languageTag: String?): AppLanguage {
            if (languageTag.isNullOrBlank()) return SYSTEM

            val normalizedTag = languageTag.lowercase()
            return when {
                normalizedTag.startsWith("en") -> ENGLISH
                normalizedTag.startsWith("es") -> SPANISH
                normalizedTag.startsWith("de") -> GERMAN
                normalizedTag.startsWith("fr") -> FRENCH
                normalizedTag.startsWith("zh") -> CHINESE_SIMPLIFIED
                normalizedTag.startsWith("ja") -> JAPANESE
                else -> ENGLISH
            }
        }

        /**
         * Returns the language shown in UI when [SYSTEM] is selected: the effective device locale.
         */
        fun resolveEffectiveDisplayLanguage(storedLanguage: AppLanguage): AppLanguage {
            if (storedLanguage != SYSTEM) return storedLanguage
            return fromLanguageTag(java.util.Locale.getDefault().toLanguageTag())
        }
    }
}
