package com.example.diplomatiki.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomatiki.data.ItemsRepository
import com.example.diplomatiki.data.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val itemsRepository: ItemsRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    var settingsUiState by mutableStateOf(SettingsUiState())
        private set
    
    init {
        viewModelScope.launch {
            settingsUiState = SettingsUiState(
                isDarkTheme = userPreferencesRepository.isDarkThemeEnabled.first()
            )
        }
    }
    
    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDarkThemeEnabled(enabled)
            settingsUiState = settingsUiState.copy(isDarkTheme = enabled)
        }
    }
    
    fun deleteAllData() {
        viewModelScope.launch {
            itemsRepository.deleteAllItems()
            settingsUiState = settingsUiState.copy(showDeleteConfirmation = false, dataDeleted = true)
        }
    }
    
    fun showDeleteConfirmation() {
        settingsUiState = settingsUiState.copy(showDeleteConfirmation = true)
    }
    
    fun dismissDeleteConfirmation() {
        settingsUiState = settingsUiState.copy(showDeleteConfirmation = false)
    }
    
    fun resetDataDeletedFlag() {
        settingsUiState = settingsUiState.copy(dataDeleted = false)
    }
}

data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val dataDeleted: Boolean = false
)