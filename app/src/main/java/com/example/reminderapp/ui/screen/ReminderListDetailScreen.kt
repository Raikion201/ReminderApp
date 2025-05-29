package com.example.reminderapp.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.reminderapp.data.model.Reminder
import com.example.reminderapp.ui.components.DueDateChip
import com.example.reminderapp.ui.components.PriorityChip
import com.example.reminderapp.ui.components.StatusIcon
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
                title = { 
                    Column {
                        Text(list?.name ?: "Reminders")
                        if (reminders.isNotEmpty()) {
                            Text(
                                text = "${reminders.count { !it.isCompleted }} active, ${reminders.count { it.isCompleted }} completed",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No reminders in this list yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap the + button to add one",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Group reminders by completion status
                val (completed, active) = reminders.partition { it.isCompleted }
                
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    // Active reminders section
                    if (active.isNotEmpty()) {
                        item {
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        
                        items(active.sortedWith(
                            compareBy<Reminder> { 
                                it.dueDate ?: Long.MAX_VALUE 
                            }.thenByDescending { 
                                it.priority.ordinal 
                            }
                        )) { reminder ->
                            ReminderRow(
                                reminder = reminder,
                                onToggleComplete = { viewModel.toggleReminderCompletion(reminder) },
                                onClick = { navController.navigate(Routes.editReminder(listId, reminder.id)) }
                            )
                        }
                    }
                    
                    // Completed reminders section
                    if (completed.isNotEmpty()) {
                        item {
                            Text(
                                text = "Completed",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        
                        items(completed) { reminder ->
                            ReminderRow(
                                reminder = reminder,
                                onToggleComplete = { viewModel.toggleReminderCompletion(reminder) },
                                onClick = { navController.navigate(Routes.editReminder(listId, reminder.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderRow(reminder: Reminder, onToggleComplete: () -> Unit, onClick: () -> Unit) {
    val alpha by animateFloatAsState(targetValue = if (reminder.isCompleted) 0.6f else 1f, label = "opacity")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isCompleted) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f) 
                else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon with checkbox
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                StatusIcon(
                    isCompleted = reminder.isCompleted,
                    dueDate = reminder.dueDate
                )
                
                Checkbox(
                    checked = reminder.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    modifier = Modifier.alpha(0.01f) // Make it nearly invisible but clickable
                )
            }
            
            // Content column with title, notes, and metadata
            Column(modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
                .alpha(alpha)
            ) {
                // Title row with priority indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (reminder.priority != com.example.reminderapp.data.model.Priority.NONE) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(com.example.reminderapp.ui.components.getPriorityColor(reminder.priority))
                                .padding(end = 8.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Notes if available
                reminder.notes?.let {
                    if (it.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Chips for metadata (due date and priority)
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Due date chip
                    reminder.dueDate?.let { dueDate ->
                        DueDateChip(dueDate)
                    }
                    
                    // Priority chip if not NONE
                    if (reminder.priority != com.example.reminderapp.data.model.Priority.NONE) {
                        PriorityChip(reminder.priority)
                    }
                }
            }
        }
    }
}
