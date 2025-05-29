package com.example.reminderapp.ui.viewmodel

import android.app.Application // Import Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reminderapp.data.model.Reminder
import com.example.reminderapp.data.model.ReminderList
import com.example.reminderapp.data.repository.ReminderRepository
import android.content.Context
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map

class ReminderViewModel(
    private val context: Context
) : ViewModel() {
    private val repository = ReminderRepository(context)

    val reminderLists = repository.getAllLists().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getRemindersForList(listId: String) = repository.getRemindersForList(listId).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch { repository.insertReminder(reminder) }
    }
    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch { repository.updateReminder(reminder) }
    }
    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch { repository.deleteReminder(reminder) }
    }
    fun addReminderList(list: ReminderList) {
        viewModelScope.launch { repository.insertList(list) }
    }
    fun updateReminderList(list: ReminderList) {
        viewModelScope.launch { repository.updateList(list) }
    }
    fun deleteReminderList(list: ReminderList) {
        viewModelScope.launch { repository.deleteList(list) }
    }

    // Get a ReminderList by ID from the current state (for Compose screens)
    fun getReminderList(listId: String): ReminderList? {
        return reminderLists.value.find { it.id == listId }
    }

    // Get a Reminder by ID from the current reminders for a list (for Compose screens)
    fun getReminder(reminderId: String, listId: String): Reminder? {
        return getRemindersForList(listId).value.find { it.id == reminderId }
    }

    // Toggle completion status of a reminder
    fun toggleReminderCompletion(reminder: Reminder) {
        val updated = reminder.copy(isCompleted = !reminder.isCompleted)
        updateReminder(updated)
    }

    // Get counts of active and completed reminders for a list as a Flow
    fun getReminderCountsForList(listId: String): kotlinx.coroutines.flow.Flow<Pair<Int, Int>> {
        return repository.getRemindersForList(listId).map { reminders ->
            val active = reminders.count { !it.isCompleted }
            val completed = reminders.count { it.isCompleted }
            Pair(active, completed)
        }
    }

    // Helper for addReminderList(name: String)
    fun addReminderList(name: String) {
        val list = ReminderList(name = name)
        addReminderList(list)
    }

    // Stub for fetchCustomSound to avoid unresolved reference error
    fun fetchCustomSound(reminderId: String, url: String) {
        // TODO: Implement sound download and update logic
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ReminderViewModel(context) as T
            }
        }
    }
}
