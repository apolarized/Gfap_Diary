package com.example.diplomatiki.data

import android.content.Context

interface AppContainer {
    val itemsRepository: ItemsRepository
    val userPreferencesRepository: UserPreferencesRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val itemsRepository: ItemsRepository by lazy {
        OfflineItemsRepository(DiplomatikiDatabase.getDatabase(context).itemDao())
    }
    
    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }
}