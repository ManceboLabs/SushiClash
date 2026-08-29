package com.mancebolabs.sushiclash.domain.repository

import com.mancebolabs.sushiclash.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow

interface LanguageRepository {
    val appLanguage: Flow<AppLanguage>

    fun getAppLanguage(): AppLanguage

    fun setAppLanguage(language: AppLanguage)

    /** Re-read the current AppCompat application locale (e.g. after Activity recreation). */
    fun refreshAppLanguage()
}
