package com.example.diplomatiki.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diplomatiki.DiplomatikiBottomAppBar
import com.example.diplomatiki.DiplomatikiTopAppBar
import com.example.diplomatiki.GFAPInfoDialog
import com.example.diplomatiki.R
import com.example.diplomatiki.ui.AppViewModelProvider
import com.example.diplomatiki.ui.components.DateRangePickerDialog
import com.example.diplomatiki.ui.navigation.NavigationDestination
import com.example.diplomatiki.utils.PdfUtils
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.max


object StatisticsDestination : NavigationDestination {
    override val route = "statistics"
    override val titleRes = R.string.app_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    navigateToHome: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToGraph: () -> Unit,
    navigateToHistory: () -> Unit,
    viewModel: StatisticsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val statisticsUiState by viewModel.statisticsUiState.collectAsState()
    val showDialog = remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    if (showDateRangePicker) {
        DateRangePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            onDateRangeSelected = { startDate, endDate ->
                showDateRangePicker = false
                viewModel.updateDateRange(startDate, endDate)
            }
        )
    }

    Scaffold(
        topBar = {
            DiplomatikiTopAppBar(
                title = stringResource(StatisticsDestination.titleRes),
                canNavigateBack = false,
                showInfoButton = true,
                onInfoClick = { showDialog.value = true },
                onShareClick = { viewModel.shareData(context) },
                onSettingsClick = navigateToSettings,
                onExportCsvClick = { 
                    viewModel.statisticsUiState.value.itemList.let { items ->
                        PdfUtils.exportToCsv(context, items)
                    }
                }
            )
        },
        bottomBar = {
            DiplomatikiBottomAppBar(
                currentRoute = "statistics",
                onNavigate = { route ->
                    when (route) {
                        "home" -> navigateToHome()
                        "history" -> navigateToHistory()
                        "graph" -> navigateToGraph()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GFAPInfoDialog(
                showDialog = showDialog.value,
                onDismiss = { showDialog.value = false }
            )

            // Date Range Picker Button
            OutlinedButton(
                onClick = { showDateRangePicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Calendar",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        text = when {
                            statisticsUiState.startDate != null && statisticsUiState.endDate != null -> {
                                val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.US)
                                "From ${dateFormat.format(statisticsUiState.startDate)} to ${dateFormat.format(statisticsUiState.endDate)}"
                            }
                            else -> stringResource(R.string.date_range_picker_title)
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Statistics Cards
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GFAP Statistics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (statisticsUiState.itemList.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_data_on_period),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    } else {
                        // Min Value
                        StatisticRow(
                            label = stringResource(R.string.minimum_value),
                            value = String.format(Locale.US, "%.2f ng/ml", statisticsUiState.minValue)
                        )
                        
                        // Max Value
                        StatisticRow(
                            label = stringResource(R.string.maximum_value),
                            value = String.format(Locale.US, "%.2f ng/ml", statisticsUiState.maxValue)
                        )
                        
                        // Average Value
                        StatisticRow(
                            label = stringResource(R.string.average_value),
                            value = String.format(Locale.US, "%.2f ng/ml", statisticsUiState.avgValue)
                        )
                        
                        // Total Measurements
                        StatisticRow(
                            label = stringResource(R.string.total_measurements),
                            value = statisticsUiState.itemList.size.toString()
                        )
                    }
                }
            }
            
            // Bar Chart Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(400.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.range_distribution),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (statisticsUiState.itemList.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_data_on_period),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        ) {
                            GFAPBarChart(
                                normalCount = statisticsUiState.normalCount,
                                elevatedCount = statisticsUiState.elevatedCount,
                                highCount = statisticsUiState.highCount
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun GFAPBarChart(
    normalCount: Int,
    elevatedCount: Int,
    highCount: Int,
    modifier: Modifier = Modifier
) {
    val normalColor = Color(0xFF2E7D32) // Dark green
    val elevatedColor = Color(0xFFF57C00) // Dark yellow/orange
    val highColor = Color(0xFFD32F2F) // Dark red
    
    // Capture these colors from the MaterialTheme before entering the Canvas scope
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    // Use a Column to separate chart and labels vertically
    Column(modifier = modifier.fillMaxSize()) {
        // Chart area - takes 80% of the height
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f)
                .padding(top = 8.dp, end = 8.dp)
        ) {
            // Y-axis labels column - on the left
            Column(
                modifier = Modifier
                    .width(30.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val maxCount = max(max(normalCount, elevatedCount), highCount)
                if (maxCount > 0) {
                    Text(
                        text = maxCount.toString(),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(0.8f)

                    )
                    Text(
                        text = (maxCount / 2).toString(),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                    Text(
                        text = "0",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                }
            }
            
            // Chart canvas - in the center/right
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxCount = max(max(normalCount, elevatedCount), highCount).toFloat()
                    if (maxCount == 0f) return@Canvas
                    
                    val barWidth = size.width / 3 * 0.5f // 50% of available width per bar
                    val spacing = size.width / 3 * 0.45f / 2 // Equal spacing on both sides
                    
                    // Draw Y-axis
                    drawLine(
                        color = onSurfaceColor,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 2f
                    )
                    
                    // Draw X-axis
                    drawLine(
                        color = onSurfaceColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 2f
                    )
                    
                    // Draw bars
                    drawBar(0, normalCount.toFloat(), maxCount, barWidth, spacing, normalColor, size)
                    drawBar(1, elevatedCount.toFloat(), maxCount, barWidth, spacing, elevatedColor, size)
                    drawBar(2, highCount.toFloat(), maxCount, barWidth, spacing, highColor, size)
                }
            }
        }
        
        // X-axis labels area - takes 20% of the height
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.2f)
                .padding(top = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BarLabel("Normal\n(0-0.2)", Color(0xFF2E7D32))
            BarLabel("Elevated\n(0.2-0.5)", Color(0xFFF57C00))
            BarLabel("High\n(>0.5)", Color(0xFFD32F2F))
        }
    }
}

private fun DrawScope.drawBar(
    index: Int,
    value: Float,
    maxValue: Float,
    barWidth: Float,
    spacing: Float,
    color: Color,
    size: Size
) {
    val barHeight = (value / maxValue) * size.height
    val startX = index * (barWidth + 2 * spacing) + spacing
    
    drawRect(
        color = color,
        topLeft = Offset(startX, size.height - barHeight),
        size = Size(barWidth, barHeight)
    )
    
    // Draw value on top of the bar
    if (value > 0) {
        drawContext.canvas.nativeCanvas.drawText(
            value.toInt().toString(),
            startX + barWidth / 2,
            size.height - barHeight - 10,
            android.graphics.Paint().apply {
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 30f
                this.color = color.copy(alpha = 0.8f).toArgb()
            }
        )
    }
}

@Composable
fun BarLabel(text: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(8.dp)
                .background(color)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}