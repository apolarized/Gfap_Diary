package com.example.diplomatiki.ui.graph

import android.content.Context
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
import java.util.*

class GraphViewModel(
    private val itemsRepository: ItemsRepository
) : ViewModel() {
    private val _graphUiState = MutableStateFlow(GraphUiState())
    val graphUiState: StateFlow<GraphUiState> = _graphUiState.asStateFlow()

    init {
        getItems()
    }

    private fun getItems() {
        viewModelScope.launch {
            itemsRepository.getAllItemsStream()
                .collect { items ->
                    _graphUiState.update { currentState ->
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
            _graphUiState.update { currentState ->
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

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class GraphUiState(
    val itemList: List<Item> = listOf(),
    val startDate: Long? = null,
    val endDate: Long? = null
)

