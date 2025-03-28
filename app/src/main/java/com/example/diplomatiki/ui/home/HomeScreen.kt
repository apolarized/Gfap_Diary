package com.example.diplomatiki.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.InsertEmoticon
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material.icons.rounded.SentimentVeryDissatisfied
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diplomatiki.DiplomatikiBottomAppBar
import com.example.diplomatiki.DiplomatikiTopAppBar
import com.example.diplomatiki.GFAPInfoDialog
import com.example.diplomatiki.R
import com.example.diplomatiki.ui.AppViewModelProvider
import com.example.diplomatiki.ui.components.DateRangePickerDialog
import com.example.diplomatiki.ui.components.ExpandableFab
import com.example.diplomatiki.ui.navigation.NavigationDestination
import com.example.diplomatiki.utils.PdfUtils


object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToItemEntry: () -> Unit,
    navigateToHistory: () -> Unit,
    navigateToGraph: () -> Unit,
    navigateToStatistics: () -> Unit,
    navigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val homeUiState by viewModel.homeUiState.collectAsState()
    val scrollState = rememberScrollState()
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
                title = stringResource(HomeDestination.titleRes),
                canNavigateBack = false,
                showInfoButton = true,
                onInfoClick = { showDialog.value = true },
                onShareClick = { viewModel.shareData(context) },
                onSettingsClick = navigateToSettings,
                onExportCsvClick = { 
                    viewModel.homeUiState.value.itemList.let { items ->
                        PdfUtils.exportToCsv(context, items)
                    }
                }
            )
        },
        bottomBar = {
            DiplomatikiBottomAppBar(
                currentRoute = "home",
                onNavigate = { route ->
                    when (route) {
                        "history" -> navigateToHistory()
                        "graph" -> navigateToGraph()
                        "statistics" -> navigateToStatistics()
                    }
                }
            )
        },
        floatingActionButton = {
            ExpandableFab(
                onAddManually = navigateToItemEntry,
                onImportCsv = { csvFileLauncher.launch("text/csv") }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.fillMaxHeight(0.08f))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dialog
                GFAPInfoDialog(
                    showDialog = showDialog.value,
                    onDismiss = { showDialog.value = false }
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Last Measurement Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.last_measurement),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = homeUiState.latestItem?.formatedPrice() ?: stringResource(R.string.no_measurements_yet),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                homeUiState.latestItem?.let {
                                    Text(
                                        text = stringResource(R.string.measure_unit),
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(bottom = 3.dp),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            homeUiState.latestItem?.let { item ->
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = item.formattedTimestamp().substringBefore(" "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = item.formattedTimestamp().substringAfter(" "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Custom Progress Bar
                homeUiState.latestItem?.let { item ->
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "0",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "0.2",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "0.3",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "0.4",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "0.5",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "2.0",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                )
                        ) {
                            // Define the positions of your scale markers with adjusted distribution
                            val positions = mapOf(
                                0.0 to 0.0f,
                                0.1 to 0.1f,
                                0.15 to 0.14f,
                                0.2 to 0.2f,  // Give more visual space to the normal range (0-0.2)
                                0.25 to 0.35f,
                                0.3 to 0.45f, // Adjusted position
                                0.4 to 0.6f,  // Adjusted position
                                0.5 to 0.75f, // Adjusted position
                                0.55 to 0.82f, // Adjusted position
                                0.6 to 0.85f,
                                1.0 to 0.9f,  // Add an intermediate point for better scaling
                                2.0 to 1.0f   // Maximum still at the end
                            )

                            // Find the two closest markers
                            val lowerMarker = positions.keys.filter { it <= item.gfapval }.maxOrNull() ?: 0.0
                            val upperMarker = positions.keys.filter { it > item.gfapval }.minOrNull() ?: 2.0

                            // Calculate the progress using linear interpolation
                            val progress = if (lowerMarker == upperMarker) {
                                positions[lowerMarker] ?: 0.0f
                            } else {
                                val lowerPos = positions[lowerMarker] ?: 0.0f
                                val upperPos = positions[upperMarker] ?: 1.0f
                                val percentage = (item.gfapval - lowerMarker) / (upperMarker - lowerMarker)
                                lowerPos + percentage * (upperPos - lowerPos)
                            }.toFloat().coerceIn(0f, 1f)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .fillMaxHeight()
                                    .background(
                                        color = when {
                                            item.gfapval <= 0.2 -> Color(0xFF2E7D32) // Dark green for normal
                                            item.gfapval <= 0.5 -> Color(0xFFF57C00) // Dark yellow/orange for elevated
                                            else -> Color(0xFFD32F2F) // Dark red for high
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Status Message Box
                homeUiState.latestItem?.let { item ->
                    val (backgroundColor, textColor, icon, message) = when {
                        item.gfapval <= 0.2 -> QuadState(
                            backgroundColor = Color(0xFFE8F5E9),  // Light green
                            textColor = Color(0xFF2E7D32),        // Dark green
                            icon = Icons.Default.Check,
                            message = "Protein levels are normal"
                        )
                        item.gfapval <= 0.5 -> QuadState(
                            backgroundColor = Color(0xFFFFF3E0),  // Light yellow
                            textColor = Color(0xFFF57C00),        // Dark yellow/orange
                            icon = Icons.Default.Warning,
                            message = "Protein levels are elevated"
                        )
                        else -> QuadState(
                            backgroundColor = Color(0xFFFFEBEE),  // Light red
                            textColor = Color(0xFFD32F2F),        // Dark red
                            icon = Icons.Default.Warning,
                            message = "Protein levels are high!"
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = backgroundColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = textColor
                        )
                        Text(
                            text = message,
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Average Section Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.thirty_day_average),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = when {
                                        homeUiState.averageLast30Days != null -> 
                                            String.format("%.2f", homeUiState.averageLast30Days)
                                        else -> "No data available"
                                    },
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                homeUiState.latestItem?.let {
                                    Text(
                                        text = stringResource(R.string.measure_unit),
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(bottom = 3.dp),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            
                            // Mood Icon based on average value
                            homeUiState.averageLast30Days?.let { avg ->
                                Icon(
                                    imageVector = when {
                                        avg <= 0.2 -> Icons.Rounded.InsertEmoticon
                                        avg <= 0.5 -> Icons.Rounded.SentimentDissatisfied
                                        else -> Icons.Rounded.SentimentVeryDissatisfied
                                    },
                                    contentDescription = "Mood indicator",
                                    modifier = Modifier.size(32.dp),
                                    tint = when {
                                        avg <= 0.2 -> Color(0xFF2E7D32) // Dark green
                                        avg <= 0.5 -> Color(0xFFF57C00) // Dark yellow/orange
                                        else -> Color(0xFFD32F2F) // Dark red
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        navigateToItemEntry = {},
        navigateToHistory = {},
        navigateToGraph = {},
        navigateToSettings = {},
        navigateToStatistics = {}
    )
}

private data class QuadState(
    val backgroundColor: Color,
    val textColor: Color,
    val icon: ImageVector,
    val message: String
)
