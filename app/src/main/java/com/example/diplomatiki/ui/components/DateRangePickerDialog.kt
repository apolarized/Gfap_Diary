package com.example.diplomatiki.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.diplomatiki.R
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    onDismissRequest: () -> Unit,
    onDateRangeSelected: (startDate: Long?, endDate: Long?) -> Unit
) {
    var showCustomDatePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()
    
    if (showCustomDatePicker) {
        DatePickerDialog(
            onDismissRequest = { 
                showCustomDatePicker = false
                onDismissRequest()
            },
            confirmButton = {
                TextButton(onClick = {
                    showCustomDatePicker = false
                    onDateRangeSelected(
                        dateRangePickerState.selectedStartDateMillis,
                        dateRangePickerState.selectedEndDateMillis
                    )
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCustomDatePicker = false
                    onDismissRequest()
                }) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.height(height = 500.dp)
            )
        }
    } else {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(text = stringResource(R.string.date_range_picker_title)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            val endDate = calendar.timeInMillis
                            calendar.add(Calendar.DAY_OF_YEAR, -7) // Moves back 7 days
                            val startDate = calendar.timeInMillis
                            onDateRangeSelected(startDate, endDate)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Last Week")
                    }
                    
                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            val endDate = calendar.timeInMillis
                            calendar.add(Calendar.MONTH, -1)
                            val startDate = calendar.timeInMillis
                            onDateRangeSelected(startDate, endDate)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Last Month")
                    }
                    
                    Button(
                        onClick = { showCustomDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Custom Dates")
                    }

                    Button(
                        onClick = { 
                            onDateRangeSelected(null, null)
                            onDismissRequest()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Show All")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel")
                }
            }
        )
    }
}