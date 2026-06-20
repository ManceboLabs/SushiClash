package com.mancebolabs.sushiclash.testutil

import com.mancebolabs.sushiclash.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeOnboardingRepository(
    completed: Boolean = true,
) : OnboardingRepository {
    private val _hasCompletedOnboarding = MutableStateFlow(completed)
    override val hasCompletedOnboarding: Flow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    var setOnboardingCompletedCallCount: Int = 0
        private set

    override suspend fun setOnboardingCompleted() {
        setOnboardingCompletedCallCount++
        _hasCompletedOnboarding.value = true
    }
}
