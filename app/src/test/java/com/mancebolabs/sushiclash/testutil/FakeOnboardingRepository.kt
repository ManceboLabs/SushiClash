package com.mancebolabs.sushiclash.testutil

import com.mancebolabs.sushiclash.domain.model.PersistenceReadState
import com.mancebolabs.sushiclash.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeOnboardingRepository(
    completed: Boolean = true,
) : OnboardingRepository {
    private val _hasCompletedOnboardingState = MutableStateFlow<PersistenceReadState<Boolean>>(
        PersistenceReadState.Data(completed),
    )

    override val hasCompletedOnboardingState: Flow<PersistenceReadState<Boolean>> =
        _hasCompletedOnboardingState.asStateFlow()

    override val hasCompletedOnboarding: Flow<Boolean> = _hasCompletedOnboardingState.map { state ->
        when (state) {
            is PersistenceReadState.Data -> state.value
            PersistenceReadState.Missing,
            PersistenceReadState.Corrupted,
            PersistenceReadState.Unavailable -> false
        }
    }

    var setOnboardingCompletedCallCount: Int = 0
        private set

    override suspend fun setOnboardingCompleted() {
        setOnboardingCompletedCallCount++
        _hasCompletedOnboardingState.value = PersistenceReadState.Data(true)
    }

    fun setHasCompletedOnboardingUnreadable() {
        _hasCompletedOnboardingState.value = PersistenceReadState.Unavailable
    }
}
