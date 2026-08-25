package com.mancebolabs.sushiclash.domain.repository

import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    val hasCompletedOnboarding: Flow<Boolean>

    val hasCompletedOnboardingState: Flow<PersistenceReadState<Boolean>>

    suspend fun setOnboardingCompleted()
}
