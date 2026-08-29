package com.mancebolabs.sushiclash.data.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.mancebolabs.sushiclash.domain.model.AppLanguage
import com.mancebolabs.sushiclash.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class LanguageRepositoryImpl : LanguageRepository {

    private val refreshSignal = MutableStateFlow(0)

    override val appLanguage: Flow<AppLanguage> = refreshSignal.map {
        getAppLanguage()
    }

    override fun getAppLanguage(): AppLanguage {
        return AppLanguage.fromLanguageTag(readPrimaryLanguageTag())
    }

    override fun setAppLanguage(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(language.toLocaleListCompat())
        refreshAppLanguage()
    }

    override fun refreshAppLanguage() {
        refreshSignal.value = refreshSignal.value + 1
    }

    private fun readPrimaryLanguageTag(): String? {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return null
        return locales[0]?.toLanguageTag()
    }
}

private fun AppLanguage.toLocaleListCompat(): LocaleListCompat {
    return if (languageTag == null) {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(languageTag)
    }
}
