@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.diplomatiki

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.diplomatiki.R.string
import com.example.diplomatiki.ui.AppViewModelProvider
import com.example.diplomatiki.ui.navigation.DiplomatikiNavHost
import com.example.diplomatiki.ui.theme.DiplomatikiTheme
import com.example.diplomatiki.ui.theme.ThemeViewModel

/**
 * Top level composable that represents screens for the application.
 */
@Composable
fun DiplomatikiApp(
    navController: NavHostController = rememberNavController(),
    themeViewModel: ThemeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // Collect the current theme preference
    val isDarkTheme = themeViewModel.isDarkTheme.collectAsState().value
    
    // Apply the theme preference
    DiplomatikiTheme(
        forceDarkTheme = isDarkTheme
    ) {
        DiplomatikiNavHost(navController = navController)
    }
}

/**
 * App bar to display title and conditionally display the back navigation.
 */
@Composable
fun DiplomatikiTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateUp: () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    showInfoButton: Boolean = false,
    onInfoClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onExportCsvClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Text(
                title,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleLarge
            )
        },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(string.back_button),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else if (showInfoButton) {
                IconButton(onClick = onInfoClick) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Information",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        actions = {
            if (!canNavigateBack) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        contentDescription = "More options"
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share PDF"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share PDF")
                            }
                        },
                        onClick = {
                            onShareClick()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Export CSV"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Export CSV")
                            }
                        },
                        onClick = {
                            onExportCsvClick()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Settings")
                            }
                        },
                        onClick = {
                            onSettingsClick()
                            showMenu = false
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun DiplomatikiBottomAppBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        MaterialTheme.colorScheme.primary
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            },
            label = {
                Text(
                    color = MaterialTheme.colorScheme.onPrimary,
                    text = "Home"
                )
            },
            selected = currentRoute == "home",
            onClick = { onNavigate("home") }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "History",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            },
            label = {
                Text(
                    color = MaterialTheme.colorScheme.onPrimary,
                    text = "History"
                )
            },
            selected = currentRoute == "history",
            onClick = { onNavigate("history") }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ShowChart,
                    contentDescription = "Graph",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            },
            label = {
                Text(
                    color = MaterialTheme.colorScheme.onPrimary,
                    text = "Graph"
                )
            },
            selected = currentRoute == "graph",
            onClick = { onNavigate("graph") }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = "Statistics",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            },
            label = {
                Text(
                    color = MaterialTheme.colorScheme.onPrimary,
                    text = "Statistics"
                )
            },
            selected = currentRoute == "statistics",
            onClick = { onNavigate("statistics") }
        )
    }
}

    @Composable
    fun GFAPInfoDialog(
        showDialog: Boolean,
        onDismiss: () -> Unit
    ) {
        if (showDialog) {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        text = stringResource(string.app_title),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(string.info_body),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Justify,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(string.normal_range),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF2E7D32)// Dark green
                            )
                            Text(
                                text = stringResource(string.elevated_range),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFF57C00) // Dark yellow/orange
                            )
                            Text(
                                text = stringResource(string.high_range),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFD32F2F) // Dark red
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text(
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            text = "OK"
                        )
                    }
                },
            )
        }
    }
