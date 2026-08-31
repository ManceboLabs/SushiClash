package com.mancebolabs.sushiclash.di

import android.content.Context
import com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushiclash.data.repository.AchievementRepositoryImpl
import com.mancebolabs.sushiclash.data.repository.FeedbackSettingsRepositoryImpl
import com.mancebolabs.sushiclash.data.repository.FrequentPlayersRepositoryImpl
import com.mancebolabs.sushiclash.data.locale.LanguageRepositoryImpl
import com.mancebolabs.sushiclash.data.repository.GameRepositoryImpl
import com.mancebolabs.sushiclash.data.repository.HistoryRepositoryImpl
import com.mancebolabs.sushiclash.data.repository.OnboardingRepositoryImpl
import com.mancebolabs.sushiclash.data.repository.ParticipantsRepositoryImpl
import com.mancebolabs.sushiclash.data.repository.ThemeRepositoryImpl
import com.mancebolabs.sushiclash.domain.model.ChefAnimationTriggerLogic
import com.mancebolabs.sushiclash.domain.model.ChefEventAnimationSelector
import com.mancebolabs.sushiclash.domain.model.RandomRouletteLogic
import com.mancebolabs.sushiclash.domain.repository.AchievementRepository
import com.mancebolabs.sushiclash.domain.repository.FeedbackSettingsRepository
import com.mancebolabs.sushiclash.domain.repository.FrequentPlayersRepository
import com.mancebolabs.sushiclash.domain.repository.GameRepository
import com.mancebolabs.sushiclash.domain.repository.HistoryRepository
import com.mancebolabs.sushiclash.domain.repository.LanguageRepository
import com.mancebolabs.sushiclash.domain.repository.OnboardingRepository
import com.mancebolabs.sushiclash.domain.repository.ParticipantsRepository
import com.mancebolabs.sushiclash.domain.repository.ThemeRepository

object AppContainer {
    private fun dataStore(context: Context): AppPreferencesDataStore {
        return AppPreferencesDataStore(context.applicationContext)
    }

    fun gameRepository(context: Context): GameRepository {
        val chefRandom = AppContainerTestOverrides.chefRandomProviderOrDefault()
        val rouletteRandom = AppContainerTestOverrides.rouletteRandomProviderOrDefault()
        return GameRepositoryImpl(
            dataStore = dataStore(context),
            randomRouletteLogic = RandomRouletteLogic(rouletteRandom),
            chefAnimationTriggerLogic = ChefAnimationTriggerLogic(chefRandom),
            chefEventAnimationSelector = ChefEventAnimationSelector(chefRandom),
        )
    }

    fun languageRepository(): LanguageRepository = sharedLanguageRepository

    private val sharedLanguageRepository: LanguageRepository = LanguageRepositoryImpl()

    fun historyRepository(context: Context): HistoryRepository {
        return HistoryRepositoryImpl(dataStore(context))
    }

    fun participantsRepository(context: Context): ParticipantsRepository {
        val store = dataStore(context)
        return ParticipantsRepositoryImpl(store, gameRepository(context))
    }

    fun themeRepository(context: Context): ThemeRepository {
        return ThemeRepositoryImpl(dataStore(context))
    }

    fun onboardingRepository(context: Context): OnboardingRepository {
        return OnboardingRepositoryImpl(dataStore(context))
    }

    fun feedbackSettingsRepository(context: Context): FeedbackSettingsRepository {
        return FeedbackSettingsRepositoryImpl(dataStore(context))
    }

    fun frequentPlayersRepository(context: Context): FrequentPlayersRepository {
        return FrequentPlayersRepositoryImpl(dataStore(context))
    }

    fun achievementRepository(context: Context): AchievementRepository {
        return AchievementRepositoryImpl(dataStore(context))
    }
}
