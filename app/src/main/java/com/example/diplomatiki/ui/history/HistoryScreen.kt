package com.example.diplomatiki.ui.history

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diplomatiki.DiplomatikiBottomAppBar
import com.example.diplomatiki.DiplomatikiTopAppBar
import com.example.diplomatiki.GFAPInfoDialog
import com.example.diplomatiki.R
import com.example.diplomatiki.data.Item
import com.example.diplomatiki.ui.AppViewModelProvider
import com.example.diplomatiki.ui.components.DateRangePickerDialog
import com.example.diplomatiki.ui.components.ExpandableFab
import com.example.diplomatiki.ui.navigation.NavigationDestination
import com.example.diplomatiki.ui.theme.DiplomatikiTheme
import com.example.diplomatiki.utils.PdfUtils
import java.text.SimpleDateFormat
import java.util.Locale

object HistoryDestination : NavigationDestination {
    override val route = "history"
    override val titleRes = R.string.app_title
}

/**
 * Entry route for History screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navigateToItemEntry: () -> Unit,
    navigateToItemUpdate: (Int) -> Unit,
    navigateToHome: () -> Unit,
    navigateToGraph: () -> Unit,
    navigateToStatistics: () -> Unit,
    navigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val historyUiState by viewModel.historyUiState.collectAsState()
    val showDialog = remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }

    // File picker launcher
    val csvFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importCsvFile(context, it) }
    }

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
                title = stringResource(HistoryDestination.titleRes),
                canNavigateBack = false,
                showInfoButton = true,
                onInfoClick = { showDialog.value = true },
                onShareClick = { viewModel.shareData(context) },
                onSettingsClick = navigateToSettings,
                onExportCsvClick = { 
                    viewModel.historyUiState.value.itemList.let { items ->
                        PdfUtils.exportToCsv(context, items)
                    }
                }
            )
        },
        bottomBar = {
            DiplomatikiBottomAppBar(
                currentRoute = "history",
                onNavigate = { route ->
                    if(route == "home") {
                        navigateToHome()
                    }else if(route == "graph") {
                        navigateToGraph()
                    }else if (route == "statistics"){
                        navigateToStatistics()
                    }
                }
            )
        },
        floatingActionButton = {
            ExpandableFab(
                onAddManually = navigateToItemEntry,
                onImportCsv = { csvFileLauncher.launch("text/csv") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                            historyUiState.startDate != null && historyUiState.endDate != null -> {
                                val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.US)
                                "From ${dateFormat.format(historyUiState.startDate)} to ${dateFormat.format(historyUiState.endDate)}"
                            }
                            else -> "Select Date Range"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HistoryBody(
                itemList = historyUiState.itemList,
                onItemClick = navigateToItemUpdate,
                modifier = Modifier.weight(1f)  // Add weight to allow LazyColumn to fill remaining space
            )

            GFAPInfoDialog(
                showDialog = showDialog.value,
                onDismiss = { showDialog.value = false }
            )
        }
    }
}


@Composable
private fun HistoryBody(
    itemList: List<Item>,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (itemList.isEmpty()) {
            Text(
                text = stringResource(R.string.no_item_description),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
        } else {
            DiplomatikiList(
                itemList = itemList,
                onItemClick = { onItemClick(it.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
            )
        }
    }
}

@Composable
private fun DiplomatikiList(
    itemList: List<Item>,
    onItemClick: (Item) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
    ) {
        items(items = itemList, key = { it.id }) { item ->
            DiplomatikiItem(
                item = item,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_small))
                    .clickable { onItemClick(item) }
            )
        }
    }
}

@Composable
private fun DiplomatikiItem(
    item: Item,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Value section with status icon, GFAP value, and comment icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Status icon based on GFAP value - on the left
                    val (statusIcon, statusColor) = when {
                        item.gfapval <= 0.2 -> Pair(
                            Icons.Default.Check,
                            Color(0xFF2E7D32) // Dark green
                        )
                        item.gfapval <= 0.5 -> Pair(
                            Icons.Default.Warning,
                            Color(0xFFF57C00) // Dark yellow/orange
                        )
                        else -> Pair(
                            Icons.Default.Warning,
                            Color(0xFFD32F2F) // Dark red
                        )
                    }
                    
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = when {
                            item.gfapval <= 0.2 -> "Normal level"
                            item.gfapval <= 0.5 -> "Elevated level"
                            else -> "High level"
                        },
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Text(
                        text = item.formatedPrice(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    // Comment icon right next to the GFAP value
                    if (item.hasComment()) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = "Has comment",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // Date and time column
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = item.formattedTimestamp().substringBefore(" "), // Date part
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = item.formattedTimestamp().substringAfter(" "), // Time part
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryBodyPreview() {
    DiplomatikiTheme {
        HistoryBody(
            listOf(
                Item(1, 11.0), Item(2, 15.0), Item(3, 9.0)
            ),
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryBodyEmptyListPreview() {
    DiplomatikiTheme {
        HistoryBody(listOf(), onItemClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun DiplomatikiItemPreview() {
    DiplomatikiTheme {
        DiplomatikiItem(
            Item(1, 10.0),
        )
    }
}

