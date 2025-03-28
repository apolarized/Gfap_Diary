package com.example.diplomatiki.ui.item

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomatiki.data.ItemsRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel to retrieve and update an item from the [ItemsRepository]'s data source.
 */
class ItemEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository
) : ViewModel() {

    /**
     * Holds current item ui state
     */
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    private val itemId: Int = checkNotNull(savedStateHandle[ItemEditDestination.itemIdArg])

    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            gfapval.isNotBlank() && 
            timestamp > 0 && 
            isValidDecimalNumber(gfapval)
        }
    }
    
    /**
     * Validates that the input string is a proper decimal number:
     * - Contains only digits and at most one decimal point
     * - No commas or other invalid characters
     */
    private fun isValidDecimalNumber(input: String): Boolean {
        // Check if empty
        if (input.isEmpty()) return false
        
        // Count decimal points
        val decimalPoints = input.count { it == '.' }
        if (decimalPoints > 1) return false
        
        // Check for invalid characters (only digits and at most one decimal point allowed)
        return input.all { it.isDigit() || it == '.' }
    }

    init {
        viewModelScope.launch {
            itemUiState = itemsRepository.getItemStream(itemId)
                .filterNotNull()
                .first()
                .toItemUiState(true)
        }
    }

    fun updateUiState(itemDetails: ItemDetails) {
        itemUiState =
            ItemUiState(itemDetails = itemDetails, isEntryValid = validateInput(itemDetails))
    }

    /**
     * Updates the timestamp in the itemDetails
     */
    fun updateDateTime(timestamp: Long) {
        val currentDetails = itemUiState.itemDetails
        updateUiState(currentDetails.copy(timestamp = timestamp))
    }

    suspend fun updateItem() {
        if (validateInput(itemUiState.itemDetails)) {
            itemsRepository.updateItem(itemUiState.itemDetails.toItem())
        }
    }
}
