package com.example.diplomatiki.ui.history

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomatiki.data.Item
import com.example.diplomatiki.data.ItemsRepository
import com.example.diplomatiki.utils.PdfUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale


/**
 * ViewModel to retrieve all items in the Room database.
 */
class HistoryViewModel(
    private val itemsRepository: ItemsRepository
) : ViewModel() {
    private val _historyUiState = MutableStateFlow(HistoryUiState())
    val historyUiState: StateFlow<HistoryUiState> = _historyUiState.asStateFlow()

    init {
        getItems()
    }

    private fun getItems() {
        viewModelScope.launch {
            itemsRepository.getAllItemsStream()
                .collect { items ->
                    _historyUiState.update { currentState ->
                        val filteredItems = filterItemsByDateRange(items, currentState.startDate, currentState.endDate)
                        currentState.copy(itemList = filteredItems)
                    }
                }
        }
    }

    private fun filterItemsByDateRange(items: List<Item>, startDate: Long?, endDate: Long?): List<Item> {
        return if (startDate != null && endDate != null) {
            items.filter { item ->
                val itemDate = item.timestamp.normalizeToStartOfDay()
                val start = startDate.normalizeToStartOfDay()
                val end = endDate.normalizeToStartOfDay()
                itemDate in start..end
            }
        } else {
            items
        }
    }

    private fun Long.normalizeToStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun updateDateRange(startDate: Long?, endDate: Long?) {
        viewModelScope.launch {
            _historyUiState.update { currentState ->
                val items = itemsRepository.getAllItemsStream().first()
                val filteredItems = filterItemsByDateRange(items, startDate, endDate)
                currentState.copy(
                    startDate = startDate,
                    endDate = endDate,
                    itemList = filteredItems
                )
            }
        }
    }

    fun shareData(context: Context) {
        viewModelScope.launch {
            val items = itemsRepository.getAllItemsStream().first()
            PdfUtils.createAndSharePdf(context, items)
        }
    }

    fun importCsvFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    var line: String?
                    
                    // Skip header if exists
                    reader.readLine()
                    
                    // Read CSV lines
                    while (reader.readLine().also { line = it } != null) {
                        if (line == null) break
                        
                        val values = line!!.split(",")
                        if (values.size >= 2) { // Assuming CSV format: timestamp,gfapValue,comment(optional)
                            try {
                                val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US)
                                    .parse(values[0])?.time ?: System.currentTimeMillis()
                                val gfapValueStr = values[1]
                                val gfapValue = gfapValueStr.toDoubleOrNull()
                                
                                if (gfapValue != null) {
                                    val comment = if (values.size > 2) values[2] else ""
                                    
                                    val item = Item(
                                        gfapval = gfapValue,
                                        timestamp = timestamp,
                                        comment = comment
                                    )
                                    itemsRepository.insertItem(item)
                                }
                            } catch (e: Exception) {
                                // Handle parsing errors
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle file reading errors
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * Ui State for HistoryScreen
 */
data class HistoryUiState(
    val itemList: List<Item> = listOf(),
    val startDate: Long? = null,
    val endDate: Long? = null
)

