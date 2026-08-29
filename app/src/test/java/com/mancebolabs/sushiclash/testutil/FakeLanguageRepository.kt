package com.mancebolabs.sushiclash.testutil

import com.mancebolabs.sushiclash.domain.model.AppLanguage
import com.mancebolabs.sushiclash.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeLanguageRepository(
    initialLanguage: AppLanguage = AppLanguage.SYSTEM,
) : LanguageRepository {

    var setAppLanguageCallCount = 0
    var lastSetLanguage: AppLanguage? = null

    private var currentLanguage = initialLanguage
    private val refreshSignal = MutableStateFlow(0)

    override val appLanguage: Flow<AppLanguage> = refreshSignal.map { currentLanguage }

    override fun getAppLanguage(): AppLanguage = currentLanguage

    override fun setAppLanguage(language: AppLanguage) {
        setAppLanguageCallCount++
        lastSetLanguage = language
        currentLanguage = language
        refreshAppLanguage()
    }

    override fun refreshAppLanguage() {
        refreshSignal.value = refreshSignal.value + 1
    }

    fun setCurrentLanguage(language: AppLanguage) {
        currentLanguage = language
    }
}
