package com.mancebolabs.sushiclash.data.repository

import com.mancebolabs.sushiclash.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushiclash.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow

class OnboardingRepositoryImpl(
    private val dataStore: AppPreferencesDataStore,
) : OnboardingRepository {

    override val hasCompletedOnboarding: Flow<Boolean> = dataStore.hasCompletedOnboarding

    override suspend fun setOnboardingCompleted() {
        dataStore.setOnboardingCompleted()
    }
}
