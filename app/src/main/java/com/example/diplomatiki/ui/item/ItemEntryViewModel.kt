package com.example.diplomatiki.ui.item

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.diplomatiki.data.Item
import com.example.diplomatiki.data.ItemsRepository
import java.text.NumberFormat

/**
 * ViewModel to validate and insert items in the Room database.
 */
class ItemEntryViewModel(private val itemsRepository: ItemsRepository) : ViewModel() {

    /**
     * Holds current item ui state
     */
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    /**
     * Updates the [itemUiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
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

    suspend fun saveItem() {
        if (validateInput()) {
            itemsRepository.insertItem(itemUiState.itemDetails.toItem())
        }
    }
}

/**
 * Represents Ui State for an Item.
 */
data class ItemUiState(
    val itemDetails: ItemDetails = ItemDetails(),
    val isEntryValid: Boolean = false
)

/**
 * Represents a form to add/edit an item
 */
data class ItemDetails(
    val id: Int = 0,
    val gfapval: String = "",
    val timestamp: Long = 0,
    val comment: String = ""
)

/**
 * Extension function to convert [ItemDetails] to [Item]. If the value is not a valid number, then
 * it returns 0.0.
 */
fun ItemDetails.toItem(): Item = Item(
    id = id,
    gfapval = gfapval.toDoubleOrNull() ?: 0.0,
    timestamp = timestamp,
    comment = comment
)

fun Item.formatedPrice(): String {
    return NumberFormat.getNumberInstance().format(gfapval)
}

/**
 * Extension function to convert [Item] to [ItemUiState]
 */
fun Item.toItemUiState(isEntryValid: Boolean = false): ItemUiState = ItemUiState(
    itemDetails = this.toItemDetails(),
    isEntryValid = isEntryValid
)

/**
 * Extension function to convert [Item] to [ItemDetails]
 */
fun Item.toItemDetails(): ItemDetails = ItemDetails(
    id = id,
    gfapval = gfapval.toString(),
    timestamp = timestamp,
    comment = comment
)
