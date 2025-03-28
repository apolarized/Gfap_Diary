package com.example.diplomatiki.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableFab(
    onAddManually: () -> Unit,
    onImportCsv: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val transition = updateTransition(expanded, label = "FAB transition")
    val rotate by transition.animateFloat(label = "FAB rotation") { state ->
        if (state) 45f else 0f
    }

    Box(
        contentAlignment = Alignment.BottomEnd
    ) {
        if (expanded) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                // Import CSV Option
                ExtendedFloatingActionButton(
                    onClick = {
                        expanded = false
                        onImportCsv()
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(bottom = 8.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = "Import CSV"
                    )
                    Text(
                        text = "Import CSV   ",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Add Manually Option
                ExtendedFloatingActionButton(
                    onClick = {
                        expanded = false
                        onAddManually()
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Add Manually"
                    )
                    Text(
                        text = "Add Manually",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        // Main FAB
        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.rotate(rotate)
            )
        }
    }
} 