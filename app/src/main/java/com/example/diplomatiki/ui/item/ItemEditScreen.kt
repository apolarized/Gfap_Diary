package com.example.diplomatiki.ui.item

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diplomatiki.DiplomatikiBottomAppBar
import com.example.diplomatiki.DiplomatikiTopAppBar
import com.example.diplomatiki.R
import com.example.diplomatiki.ui.AppViewModelProvider
import com.example.diplomatiki.ui.navigation.NavigationDestination
import com.example.diplomatiki.ui.theme.DiplomatikiTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ItemEditDestination : NavigationDestination {
    override val route = "item_edit"
    override val titleRes = R.string.edit_item_title
    const val itemIdArg = "itemId"
    val routeWithArgs = "$route/{$itemIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditScreen(
    navigateToHistory: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToGraph: () -> Unit,
    navigateToStatistics: () -> Unit,
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            DiplomatikiTopAppBar(
                title = stringResource(ItemEditDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        },
        bottomBar = {
            DiplomatikiBottomAppBar(
                currentRoute = ItemEditDestination.route,
                onNavigate = { route ->
                    if (route == "history") {
                        navigateToHistory()
                    }else if (route == "home"){
                        navigateToHome()
                    }else if (route == "graph"){
                        navigateToGraph()
                    }else if (route == "statistics"){
                        navigateToStatistics()
                    }
                }
            )
        }
    ) { innerPadding ->
        ItemEditBody(
            itemUiState = viewModel.itemUiState,
            onItemValueChange = viewModel::updateUiState,
            onDateTimeChange = viewModel::updateDateTime,
            onSaveClick = {
                coroutineScope.launch {
                    viewModel.updateItem()
                    navigateBack()
                }
            },
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding()
                )
                .verticalScroll(rememberScrollState())
        )
    }
}

@Composable
fun ItemEditBody(
    itemUiState: ItemUiState,
    onItemValueChange: (ItemDetails) -> Unit,
    onDateTimeChange: (Long) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply {
        timeInMillis = itemUiState.itemDetails.timestamp
    }
    
    // Format date and time
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    val dateString = dateFormat.format(calendar.time)
    val timeString = timeFormat.format(calendar.time)
    
    // Determine the status icon based on the GFAP value
    val gfapValue = itemUiState.itemDetails.gfapval.toDoubleOrNull() ?: 0.0
    val (statusIcon, statusColor, statusDescription) = when {
        gfapValue <= 0.2 -> Triple(
            Icons.Default.Check,
            Color(0xFF2E7D32), // Dark green
            stringResource(R.string.normal_levels)
        )
        gfapValue <= 0.5 -> Triple(
            Icons.Default.Warning,
            Color(0xFFF57C00), // Dark yellow/orange
            stringResource(R.string.elevated_levels)
        )
        else -> Triple(
            Icons.Default.Warning,
            Color(0xFFD32F2F), // Dark red
            stringResource(R.string.high_levels)
        )
    }

    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        Spacer(modifier = Modifier.fillMaxHeight(0.15f))
        // Date Selector with Status Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .clickable {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(Calendar.YEAR, year)
                                calendar.set(Calendar.MONTH, month)
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                onDateTimeChange(calendar.timeInMillis)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Select Date",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            // Status Icon
            if (itemUiState.itemDetails.gfapval.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = statusDescription,
                        tint = statusColor
                    )
                }
            }
        }
        
        // Time Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            calendar.set(Calendar.MINUTE, minute)
                            calendar.set(Calendar.SECOND, 0)
                            onDateTimeChange(calendar.timeInMillis)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Select Time",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = timeString,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        ItemInputForm(
            itemDetails = itemUiState.itemDetails,
            onValueChange = onItemValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.CenterHorizontally)
        )
        
        Button(
            onClick = onSaveClick,
            enabled = itemUiState.isEntryValid,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
            .fillMaxWidth(0.5f)
            .aspectRatio(3.125f)
            .align(alignment = Alignment.CenterHorizontally),
        ) {
            Text(text = stringResource(R.string.save_action))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ItemEditScreenPreview() {
    DiplomatikiTheme {
        ItemEditScreen(navigateBack = { /*Do nothing*/ }, onNavigateUp = { /*Do nothing*/ },
            navigateToHistory = { /*Do nothing*/ }, navigateToHome = { /*Do nothing*/ },
            navigateToGraph = { /*Do nothing*/ },
        navigateToStatistics = { /*Do nothing*/ }
        )
    }
}
