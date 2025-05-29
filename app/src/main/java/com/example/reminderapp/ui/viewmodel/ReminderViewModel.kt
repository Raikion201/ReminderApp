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
    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ReminderViewModel(context) as T
            }
        }
    }
}
