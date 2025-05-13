package com.example.reminderapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.reminderapp.data.model.Reminder
import com.example.reminderapp.ui.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminderScreen(
    navController: NavController,
    viewModel: ReminderViewModel,
    listId: String,
    reminderId: String?
) {
    val isEditing = reminderId != null
    val existingReminder = if (isEditing) viewModel.getReminder(reminderId!!, listId) else null

    var title by rememberSaveable { mutableStateOf(existingReminder?.title ?: "") }
    var notes by rememberSaveable { mutableStateOf(existingReminder?.notes ?: "") }

    val initialCalendar = Calendar.getInstance().apply {
        existingReminder?.dueDate?.let { timeInMillis = it }
    }
    var reminderDateTimeCalendar by remember { mutableStateOf(initialCalendar) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var dueDateEnabled by rememberSaveable { mutableStateOf(existingReminder?.dueDate != null) }

    // Notification settings states
    var soundEnabled by rememberSaveable { mutableStateOf(existingReminder?.isSoundEnabled ?: true) }
    var customSoundUri by rememberSaveable { mutableStateOf(existingReminder?.notificationSoundUri ?: "") } // Placeholder for URI
    var vibrateEnabled by rememberSaveable { mutableStateOf(existingReminder?.isVibrateEnabled ?: true) }
    var selectedAdvanceMinutes by rememberSaveable { mutableStateOf(existingReminder?.advanceNotificationMinutes ?: 0) }
    var repeatCount by rememberSaveable { mutableStateOf(existingReminder?.repeatCount?.toString() ?: "0") }
    var repeatInterval by rememberSaveable { mutableStateOf(existingReminder?.repeatIntervalMinutes?.toString() ?: "5") }
    var selectedPriority by remember { mutableStateOf(existingReminder?.priority ?: com.example.reminderapp.data.model.Priority.NONE) }


    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = { showDatePickerDialog = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(
                state = rememberDatePickerState(initialSelectedDateMillis = reminderDateTimeCalendar.timeInMillis)
                .also { state ->
                    LaunchedEffect(state.selectedDateMillis) {
                        state.selectedDateMillis?.let { millis ->
                            val newCal = Calendar.getInstance().apply { timeInMillis = millis }
                            reminderDateTimeCalendar.set(
                                newCal.get(Calendar.YEAR),
                                newCal.get(Calendar.MONTH),
                                newCal.get(Calendar.DAY_OF_MONTH)
                            )
                        }
                    }
                }
            )
        }
    }

    if (showTimePickerDialog) {
        // Basic Time Picker Dialog (Consider a more robust library or custom dialog for better UX)
        AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            title = { Text("Select Time") },
            text = {
                val timePickerState = rememberTimePickerState(
                    initialHour = reminderDateTimeCalendar.get(Calendar.HOUR_OF_DAY),
                    initialMinute = reminderDateTimeCalendar.get(Calendar.MINUTE),
                    is24Hour = false // Or use LocalConfiguration.current.is24HourFormat
                )
                LaunchedEffect(timePickerState.hour, timePickerState.minute) {
                    reminderDateTimeCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    reminderDateTimeCalendar.set(Calendar.MINUTE, timePickerState.minute)
                }
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = { showTimePickerDialog = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerDialog = false }) { Text("Cancel") }
            }
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Reminder" else "Add New Reminder") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (title.isNotBlank()) {
                            val finalDueDate = if (dueDateEnabled) reminderDateTimeCalendar.timeInMillis else null
                            val currentRepeatCount = repeatCount.toIntOrNull() ?: 0
                            val currentRepeatInterval = repeatInterval.toIntOrNull() ?: 5

                            if (isEditing && existingReminder != null) {
                                viewModel.updateReminder(
                                    existingReminder.copy(
                                        title = title,
                                        notes = notes.ifBlank { null },
                                        dueDate = finalDueDate,
                                        priority = selectedPriority,
                                        isSoundEnabled = soundEnabled,
                                        notificationSoundUri = customSoundUri.ifBlank { null },
                                        isVibrateEnabled = vibrateEnabled,
                                        advanceNotificationMinutes = selectedAdvanceMinutes,
                                        repeatCount = currentRepeatCount,
                                        repeatIntervalMinutes = currentRepeatInterval
                                    )
                                )
                            } else {
                                viewModel.addReminder(
                                    title = title,
                                    listId = listId,
                                    notes = notes.ifBlank { null },
                                    dueDate = finalDueDate,
                                    priority = selectedPriority,
                                    isSoundEnabled = soundEnabled,
                                    notificationSoundUri = customSoundUri.ifBlank { null },
                                    isVibrateEnabled = vibrateEnabled,
                                    advanceNotificationMinutes = selectedAdvanceMinutes,
                                    repeatCount = currentRepeatCount,
                                    repeatIntervalMinutes = currentRepeatInterval
                                )
                            }
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Filled.Done, "Save Reminder")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // Added for scrollability
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp) // Increased spacing
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Due Date/Time Switch
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Set Due Date/Time", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = dueDateEnabled,
                    onCheckedChange = { dueDateEnabled = it }
                )
            }
            // Date and Time Pickers
            if (dueDateEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { showDatePickerDialog = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pick Date")
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(reminderDateTimeCalendar.time))
                    }
                    Button(onClick = { showTimePickerDialog = true }) {
                        Text(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(reminderDateTimeCalendar.time))
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Notification Settings", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())

            // Priority Dropdown
            PrioritySelector(
                selectedPriority = selectedPriority,
                onPrioritySelected = { selectedPriority = it }
            )

            // Sound Enabled Switch
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Enable Sound")
                Spacer(Modifier.weight(1f))
                Switch(checked = soundEnabled, onCheckedChange = { soundEnabled = it })
            }

            // Custom Sound (Placeholder UI)
            OutlinedTextField(
                value = customSoundUri,
                onValueChange = { customSoundUri = it },
                label = { Text("Custom Sound URI (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g., content://media/internal/audio/media/123") }
            )
            Button(onClick = { /* TODO: Implement Sound Picker */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Select Custom Sound")
            }


            // Vibrate Enabled Switch
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Enable Vibration")
                Spacer(Modifier.weight(1f))
                Switch(checked = vibrateEnabled, onCheckedChange = { vibrateEnabled = it })
            }

            // Advance Notification Dropdown
            AdvanceNotificationSelector(
                selectedMinutes = selectedAdvanceMinutes,
                onMinutesSelected = { selectedAdvanceMinutes = it }
            )

            // Repeat Count
            OutlinedTextField(
                value = repeatCount,
                onValueChange = { repeatCount = it.filter { char -> char.isDigit() } },
                label = { Text("Repeat Count (if not viewed)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Repeat Interval
            OutlinedTextField(
                value = repeatInterval,
                onValueChange = { repeatInterval = it.filter { char -> char.isDigit() } },
                label = { Text("Repeat Interval (minutes)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = (repeatCount.toIntOrNull() ?: 0) > 0
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrioritySelector(
    selectedPriority: com.example.reminderapp.data.model.Priority,
    onPrioritySelected: (com.example.reminderapp.data.model.Priority) -> Unit
) {
    val priorities = com.example.reminderapp.data.model.Priority.entries // Changed from .values() to .entries
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedPriority.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Priority") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            priorities.forEach { itemPriority -> // Renamed loop variable for clarity
                DropdownMenuItem(
                    text = { Text(itemPriority.name) }, // Use the renamed loop variable
                    onClick = {
                        onPrioritySelected(itemPriority) // Use the renamed loop variable
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvanceNotificationSelector(
    selectedMinutes: Int,
    onMinutesSelected: (Int) -> Unit
) {
    val advanceOptions = listOf(0, 5, 10, 15, 30, 60) // In minutes
    val optionsText = advanceOptions.map {
        when (it) {
            0 -> "At time of event"
            else -> "$it minutes before"
        }
    }
    var expanded by remember { mutableStateOf(false) }
    val currentText = when (selectedMinutes) {
        0 -> "At time of event"
        else -> "$selectedMinutes minutes before"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = currentText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Notify Me") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            advanceOptions.forEachIndexed { index, minutes ->
                DropdownMenuItem(
                    text = { Text(optionsText[index]) },
                    onClick = {
                        onMinutesSelected(minutes)
                        expanded = false
                    }
                )
            }
        }
    }
}
