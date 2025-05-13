package com.example.reminderapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.reminderapp.data.model.Reminder
import com.example.reminderapp.ui.navigation.Routes
import com.example.reminderapp.ui.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListDetailScreen(
    navController: NavController,
    viewModel: ReminderViewModel,
    listId: String
) {
    val list = viewModel.getReminderList(listId)
    val reminders by viewModel.getRemindersForList(listId).collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(list?.name ?: "Reminders") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.addReminder(listId)) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Reminder")
            }
        }
    ) { paddingValues ->
        if (list == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("List not found.")
            }
            return@Scaffold
        }

        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (reminders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No reminders in this list. Tap '+' to add one.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(reminders.sortedBy { it.isCompleted }) { reminder ->
                        ReminderRow(
                            reminder = reminder,
                            onToggleComplete = { viewModel.toggleReminderCompletion(reminder) },
                            onClick = { navController.navigate(Routes.editReminder(listId, reminder.id)) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderRow(reminder: Reminder, onToggleComplete: () -> Unit, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = reminder.isCompleted,
            onCheckedChange = { onToggleComplete() }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = reminder.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null,
                color = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
            )
            reminder.notes?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            reminder.dueDate?.let {
                val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                Text(
                    text = "Due: ${dateTimeFormat.format(Date(it))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
