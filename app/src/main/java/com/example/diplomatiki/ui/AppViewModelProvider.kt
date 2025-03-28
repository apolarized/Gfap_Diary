package com.example.diplomatiki.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.diplomatiki.DiplomatikiApplication
import com.example.diplomatiki.ui.graph.GraphViewModel
import com.example.diplomatiki.ui.history.HistoryViewModel
import com.example.diplomatiki.ui.home.HomeViewModel
import com.example.diplomatiki.ui.item.ItemDetailsViewModel
import com.example.diplomatiki.ui.item.ItemEditViewModel
import com.example.diplomatiki.ui.item.ItemEntryViewModel
import com.example.diplomatiki.ui.settings.SettingsViewModel
import com.example.diplomatiki.ui.statistics.StatisticsViewModel
import com.example.diplomatiki.ui.theme.ThemeViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire Diplomatiki app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for ItemEditViewModel
        initializer {
            ItemEditViewModel(
                this.createSavedStateHandle(),
                diplomatikiApplication().container.itemsRepository
            )
        }
        // Initializer for ItemEntryViewModel
        initializer {
            ItemEntryViewModel(diplomatikiApplication().container.itemsRepository)
        }

        // Initializer for ItemDetailsViewModel
        initializer {
            ItemDetailsViewModel(
                this.createSavedStateHandle(),
                diplomatikiApplication().container.itemsRepository
            )
        }

        // Initializer for HistoryViewModel
        initializer {
            HistoryViewModel(diplomatikiApplication().container.itemsRepository)
        }

        initializer {
            HomeViewModel(diplomatikiApplication().container.itemsRepository)
        }

        // initializer for GraphViewModel
        initializer {
            GraphViewModel(diplomatikiApplication().container.itemsRepository)
        }
        // initializer for StatisticsViewModel

        initializer {
            StatisticsViewModel(diplomatikiApplication().container.itemsRepository)
        }

        // initializer for SettingsViewModel
        initializer {
            SettingsViewModel(
                diplomatikiApplication().container.itemsRepository,
                diplomatikiApplication().container.userPreferencesRepository
            )
        }

        // initializer for ThemeViewModel
        initializer {
            ThemeViewModel(
                diplomatikiApplication().container.userPreferencesRepository
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [diplomatikiApplication].
 */
fun CreationExtras.diplomatikiApplication(): DiplomatikiApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as DiplomatikiApplication)
