package com.mancebolabs.sushiclash.domain.repository

import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    val hasCompletedOnboarding: Flow<Boolean>

    suspend fun setOnboardingCompleted()
}
