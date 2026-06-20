package com.mancebolabs.sushicounter.di

import android.content.Context
import com.mancebolabs.sushicounter.data.datastore.AppPreferencesDataStore
import com.mancebolabs.sushicounter.data.repository.GameRepositoryImpl
import com.mancebolabs.sushicounter.data.repository.ParticipantsRepositoryImpl
import com.mancebolabs.sushicounter.domain.repository.GameRepository
import com.mancebolabs.sushicounter.domain.repository.ParticipantsRepository

object AppContainer {
    private fun dataStore(context: Context): AppPreferencesDataStore {
        return AppPreferencesDataStore(context.applicationContext)
    }

    fun gameRepository(context: Context): GameRepository {
        return GameRepositoryImpl(dataStore(context))
    }

    fun participantsRepository(context: Context): ParticipantsRepository {
        val store = dataStore(context)
        val gameRepository = GameRepositoryImpl(store)
        return ParticipantsRepositoryImpl(store, gameRepository)
    }
}
