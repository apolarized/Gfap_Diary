package com.example.diplomatiki.ui.statistics

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
import java.util.Calendar

class StatisticsViewModel(
    private val itemsRepository: ItemsRepository
) : ViewModel() {
    
    private val _statisticsUiState = MutableStateFlow(StatisticsUiState())
    val statisticsUiState: StateFlow<StatisticsUiState> = _statisticsUiState.asStateFlow()
    
    init {
        getItems()
    }
    
    private fun getItems() {
        viewModelScope.launch {
            itemsRepository.getAllItemsStream()
                .collect { items ->
                    _statisticsUiState.update { currentState ->
                        val filteredItems = filterItemsByDateRange(items, currentState.startDate, currentState.endDate)
                        val stats = calculateStatistics(filteredItems)
                        val chartData = prepareChartData(filteredItems)
                        
                        currentState.copy(
                            itemList = filteredItems,
                            minValue = stats.minValue,
                            maxValue = stats.maxValue,
                            avgValue = stats.avgValue,
                            normalCount = chartData.normalCount,
                            elevatedCount = chartData.elevatedCount,
                            highCount = chartData.highCount
                        )
                    }
                }
        }
    }
    
    private fun calculateStatistics(items: List<Item>): Statistics {
        if (items.isEmpty()) {
            return Statistics(0.0, 0.0, 0.0)
        }
        
        val gfapValues = items.map { it.gfapval }
        val min = gfapValues.minOrNull() ?: 0.0
        val max = gfapValues.maxOrNull() ?: 0.0
        val avg = gfapValues.average()
        
        return Statistics(min, max, avg)
    }
    
    private fun prepareChartData(items: List<Item>): ChartData {
        var normalCount = 0
        var elevatedCount = 0
        var highCount = 0
        
        items.forEach { item ->
            when {
                item.gfapval <= 0.2 -> normalCount++
                item.gfapval <= 0.5 -> elevatedCount++
                else -> highCount++
            }
        }
        
        return ChartData(normalCount, elevatedCount, highCount)
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
            _statisticsUiState.update { currentState ->
                val items = itemsRepository.getAllItemsStream().first()
                val filteredItems = filterItemsByDateRange(items, startDate, endDate)
                val stats = calculateStatistics(filteredItems)
                val chartData = prepareChartData(filteredItems)
                
                currentState.copy(
                    startDate = startDate,
                    endDate = endDate,
                    itemList = filteredItems,
                    minValue = stats.minValue,
                    maxValue = stats.maxValue,
                    avgValue = stats.avgValue,
                    normalCount = chartData.normalCount,
                    elevatedCount = chartData.elevatedCount,
                    highCount = chartData.highCount
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
}


data class StatisticsUiState(
    val itemList: List<Item> = listOf(),
    val startDate: Long? = null,
    val endDate: Long? = null,
    val minValue: Double = 0.0,
    val maxValue: Double = 0.0,
    val avgValue: Double = 0.0,
    val normalCount: Int = 0,
    val elevatedCount: Int = 0,
    val highCount: Int = 0
)

private data class Statistics(
    val minValue: Double,
    val maxValue: Double,
    val avgValue: Double
)

private data class ChartData(
    val normalCount: Int,
    val elevatedCount: Int,
    val highCount: Int
)