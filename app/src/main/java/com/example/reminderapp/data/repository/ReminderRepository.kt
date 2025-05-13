package com.example.reminderapp.data.repository

import com.example.reminderapp.data.model.Reminder
import com.example.reminderapp.data.model.ReminderList
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// For simplicity, using a singleton object for in-memory storage.
// In a real app, use Dagger/Hilt for dependency injection and Room for persistence.
object ReminderRepository {
    private val _reminderLists = MutableStateFlow<List<ReminderList>>(emptyList())
    val reminderLists: StateFlow<List<ReminderList>> = _reminderLists.asStateFlow()

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    init {
        // Sample Data
        val sampleList1 = ReminderList(name = "Personal")
        val sampleList2 = ReminderList(name = "Work")
        _reminderLists.value = listOf(sampleList1, sampleList2)

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1) // Tomorrow
        calendar.set(Calendar.HOUR_OF_DAY, 10) // 10 AM
        calendar.set(Calendar.MINUTE, 30)
        calendar.set(Calendar.SECOND, 0)

        _reminders.value = listOf(
            Reminder(title = "Buy groceries", listId = sampleList1.id),
            Reminder(title = "Call John", listId = sampleList1.id, isCompleted = true),
            Reminder(title = "Project deadline", listId = sampleList2.id, dueDate = calendar.timeInMillis)
        )
    }

    fun addReminderList(list: ReminderList) {
        _reminderLists.update { it + list }
    }

    fun getReminderList(listId: String): ReminderList? {
        return _reminderLists.value.find { it.id == listId }
    }
    
    fun updateReminderList(list: ReminderList) {
        _reminderLists.update { currentLists ->
            currentLists.map { if (it.id == list.id) list else it }
        }
    }

    fun deleteReminderList(listId: String) {
        _reminderLists.update { lists -> lists.filterNot { it.id == listId } }
        _reminders.update { rems -> rems.filterNot { it.listId == listId } }
    }

    fun getRemindersForList(listId: String): List<Reminder> {
        return _reminders.value.filter { it.listId == listId }
    }

    fun addReminder(reminder: Reminder) {
        _reminders.update { it + reminder }
    }

    fun updateReminder(reminder: Reminder) {
        _reminders.update { currentReminders ->
            currentReminders.map { if (it.id == reminder.id) reminder else it }
        }
    }

    fun deleteReminder(reminderId: String) {
        _reminders.update { it.filterNot { reminder -> reminder.id == reminderId } }
    }
}
