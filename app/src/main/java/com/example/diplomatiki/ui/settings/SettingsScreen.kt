package com.example.diplomatiki.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diplomatiki.DiplomatikiBottomAppBar
import com.example.diplomatiki.DiplomatikiTopAppBar
import com.example.diplomatiki.R
import com.example.diplomatiki.ui.AppViewModelProvider
import com.example.diplomatiki.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object SettingsDestination : NavigationDestination {
    override val route = "settings"
    override val titleRes = R.string.settings_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navigateToHome: () -> Unit,
    navigateToHistory: () -> Unit,
    navigateToGraph: () -> Unit,
    navigateToStatistics: () -> Unit,
    navigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val settingsUiState = viewModel.settingsUiState
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Show snackbar when data is deleted
    LaunchedEffect(settingsUiState.dataDeleted) {
        if (settingsUiState.dataDeleted) {
            scope.launch {
                snackbarHostState.showSnackbar("All data has been deleted")
            }
            viewModel.resetDataDeletedFlag()
        }
    }

    Scaffold(
        topBar = {
            DiplomatikiTopAppBar(
                title = stringResource(R.string.settings_title),
                canNavigateBack = true,
                navigateUp = navigateBack
            )
        },
        bottomBar = {
            DiplomatikiBottomAppBar(
                currentRoute = SettingsDestination.route,
                onNavigate = { route ->
                    when (route) {
                        "home" -> navigateToHome()
                        "history" -> navigateToHistory()
                        "graph" -> navigateToGraph()
                        "statistics" -> navigateToStatistics()
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Appearance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dark Theme",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Switch(
                            checked = settingsUiState.isDarkTheme,
                            onCheckedChange = { viewModel.toggleDarkTheme(it) }
                        )
                    }
                }
            }
            
            // Data Management Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Data Management",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.showDeleteConfirmation() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F) // Red color for danger
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Delete All Data")
                    }
                }
            }
            
            // Delete Confirmation Dialog
            if (settingsUiState.showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissDeleteConfirmation() },
                    title = { Text( text = stringResource(R.string.delete_all_data)) },
                    text = { Text( text = stringResource(R.string.delete_all_data_confirmation)) },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.deleteAllData() }
                        ) {
                            Text(
                                text = stringResource(R.string.delete),
                                color = Color(0xFFD32F2F))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.dismissDeleteConfirmation() }
                        ) {
                            Text(text = stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}