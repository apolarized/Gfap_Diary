package com.example.diplomatiki.ui.graph

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diplomatiki.DiplomatikiBottomAppBar
import com.example.diplomatiki.DiplomatikiTopAppBar
import com.example.diplomatiki.GFAPInfoDialog
import com.example.diplomatiki.R
import com.example.diplomatiki.data.Item
import com.example.diplomatiki.ui.AppViewModelProvider
import com.example.diplomatiki.ui.components.DateRangePickerDialog
import com.example.diplomatiki.ui.navigation.NavigationDestination
import com.example.diplomatiki.utils.PdfUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object GraphDestination : NavigationDestination {
    override val route = "graph"
    override val titleRes = R.string.app_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(
    navigateToHome: () -> Unit,
    navigateToHistory: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToStatistics: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GraphViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val graphUiState by viewModel.graphUiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val showDialog = remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }

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
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            DiplomatikiTopAppBar(
                title = stringResource(GraphDestination.titleRes),
                canNavigateBack = false,
                showInfoButton = true,
                onInfoClick = { showDialog.value = true },
                onShareClick = { viewModel.shareData(context) },
                onSettingsClick = { navigateToSettings() },
                onExportCsvClick = { 
                    viewModel.graphUiState.value.itemList.let { items ->
                        PdfUtils.exportToCsv(context, items)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            DiplomatikiBottomAppBar(
                currentRoute = "graph",
                onNavigate = { route ->
                    if(route == "home") {
                        navigateToHome()
                    }else if(route == "history") {
                        navigateToHistory()
                    }else if (route == "statistics"){
                        navigateToStatistics()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            GFAPInfoDialog(
                showDialog = showDialog.value,
                onDismiss = { showDialog.value = false }
            )
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
                            graphUiState.startDate != null && graphUiState.endDate != null -> {
                                val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.US)
                                "From ${dateFormat.format(graphUiState.startDate)} to ${dateFormat.format(graphUiState.endDate)}"
                            }
                            else -> "Select Date Range"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                if (graphUiState.itemList.isEmpty()) {
                    Text(
                        text = "No data available",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    GFAPLineChart(
                        items = graphUiState.itemList,
                        modifier = Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                    )
                }
            }
        }
    }
}

@Composable
fun GFAPLineChart(
    items: List<Item>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.onSecondaryContainer.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                setDrawGridBackground(false)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.valueFormatter = DateAxisValueFormatter()
                xAxis.textColor = textColor
                axisLeft.textColor = textColor
                axisRight.isEnabled = false
                legend.textColor = lineColor
            }
        },
        update = { chart ->
            val entries = items.map { item ->
                Entry(item.timestamp.toFloat(), item.gfapval.toFloat())
            }.sortedBy { it.x }

            val dataSet = LineDataSet(entries, "GFAP").apply {
                color = lineColor
                setCircleColor(lineColor)
                lineWidth = 1f
                circleRadius = 2f
                setDrawCircleHole(false)
                valueTextSize = 10f
                valueTextColor = textColor
                setDrawValues(false)
                valueTextColor = lineColor
            }

            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}

class DateAxisValueFormatter : ValueFormatter() {
    private val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

    override fun getFormattedValue(value: Float): String {
        return dateFormat.format(Date(value.toLong()))
    }
}

