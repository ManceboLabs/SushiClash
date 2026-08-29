package com.mancebolabs.sushiclash.locale

import com.mancebolabs.sushiclash.domain.model.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {

    @Test
    fun givenNullTag_whenResolvingLanguage_thenUsesSystemDefault() {
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromLanguageTag(null))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromLanguageTag(""))
    }

    @Test
    fun givenSupportedTags_whenResolvingLanguage_thenMapsCorrectly() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromLanguageTag("en"))
        assertEquals(AppLanguage.SPANISH, AppLanguage.fromLanguageTag("es-ES"))
        assertEquals(AppLanguage.GERMAN, AppLanguage.fromLanguageTag("de"))
        assertEquals(AppLanguage.FRENCH, AppLanguage.fromLanguageTag("fr-FR"))
        assertEquals(AppLanguage.CHINESE_SIMPLIFIED, AppLanguage.fromLanguageTag("zh-CN"))
        assertEquals(AppLanguage.JAPANESE, AppLanguage.fromLanguageTag("ja-JP"))
    }

    @Test
    fun givenUnsupportedTag_whenResolvingLanguage_thenFallsBackToEnglish() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromLanguageTag("pt-BR"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromLanguageTag("it"))
    }

    @Test
    fun givenSystemLanguage_whenResolvingDisplayLanguage_thenUsesDeviceLocale() {
        val effective = AppLanguage.resolveEffectiveDisplayLanguage(AppLanguage.SYSTEM)
        val deviceLanguage = AppLanguage.fromLanguageTag(java.util.Locale.getDefault().toLanguageTag())

        assertEquals(deviceLanguage, effective)
    }

    @Test
    fun givenExplicitLanguage_whenResolvingDisplayLanguage_thenReturnsStoredLanguage() {
        assertEquals(
            AppLanguage.FRENCH,
            AppLanguage.resolveEffectiveDisplayLanguage(AppLanguage.FRENCH),
        )
    }
}
