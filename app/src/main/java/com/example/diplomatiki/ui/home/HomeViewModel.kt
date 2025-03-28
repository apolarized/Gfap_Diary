package com.example.diplomatiki.ui.home

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
data class HomeUiState(
    val itemList: List<Item> = listOf(),
    val startDate: Long? = null,
    val endDate: Long? = null,
    val latestItem: Item? = null,
    val averageLast30Days: Double? = null
)

class HomeViewModel(
    private val itemsRepository: ItemsRepository
) : ViewModel() {

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    init {
        getItems()
    }

    private fun getItems() {
        viewModelScope.launch {
            itemsRepository.getAllItemsStream()
                .collect { items ->
                    _homeUiState.update { currentState ->
                        val filteredItems = filterItemsByDateRange(items, currentState.startDate, currentState.endDate)
                        currentState.copy(
                            itemList = filteredItems,
                            latestItem = items.maxByOrNull { it.timestamp },
                            averageLast30Days = calculateLast30DaysAverage(items)
                        )
                    }
                }
        }
    }

    private fun filterItemsByDateRange(items: List<Item>, startDate: Long?, endDate: Long?): List<Item> {
        return if (startDate != null && endDate != null) {
            items.filter { item ->
                val itemDate = item.timestamp.normalizeToStartOfDay()
                val start = startDate.normalizeToStartOfDay()
                val end = endDate.normalizeToEndOfDay()
                itemDate in start..end
            }
        } else {
            items
        }
    }

    private fun Long.normalizeToStartOfDay(): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = this@normalizeToStartOfDay
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun Long.normalizeToEndOfDay(): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = this@normalizeToEndOfDay
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }

    private fun calculateLast30DaysAverage(items: List<Item>): Double? {
        if (items.isEmpty()) return null
        
        val thirtyDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -30)
        }.timeInMillis

        val last30DaysItems = items.filter { it.timestamp >= thirtyDaysAgo }
        if (last30DaysItems.isEmpty()) return null

        return last30DaysItems.map { it.gfapval }.average()
    }

    fun updateDateRange(startDate: Long?, endDate: Long?) {
        viewModelScope.launch {
            _homeUiState.update { currentState ->
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
}
