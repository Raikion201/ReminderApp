package com.example.reminderapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.reminderapp.data.model.ReminderList
import com.example.reminderapp.ui.navigation.Routes
import com.example.reminderapp.ui.viewmodel.ReminderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, viewModel: ReminderViewModel) {
    val lists by viewModel.reminderLists.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Reminder App") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.ADD_LIST_SCREEN) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Reminder List")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // Categories section (Simplified: just "My Lists")
            Text(
                text = "My Lists",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            if (lists.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No reminder lists yet. Tap '+' to add one.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(lists) { list ->
                        ReminderListRow(list = list, onClick = {
                            navController.navigate(Routes.listDetail(list.id))
                        })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderListRow(list: ReminderList, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = list.name, style = MaterialTheme.typography.bodyLarge)
        // In a real app, add an icon or count of reminders
    }
}
