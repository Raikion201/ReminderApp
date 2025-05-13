package com.example.reminderapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reminderapp.data.model.Reminder
import com.example.reminderapp.data.model.ReminderList
import com.example.reminderapp.data.repository.ReminderRepository
import com.example.reminderapp.util.AlarmScheduler
import android.content.Context
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.example.reminderapp.data.model.Priority

class ReminderViewModel(private val alarmScheduler: AlarmScheduler) : ViewModel() {

    val reminderLists: StateFlow<List<ReminderList>> = ReminderRepository.reminderLists
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getRemindersForList(listId: String): StateFlow<List<Reminder>> {
        return ReminderRepository.reminders
            .map { reminders -> reminders.filter { it.listId == listId } }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }
    
    fun getReminderList(listId: String): ReminderList? {
        return ReminderRepository.getReminderList(listId)
    }

    fun addReminderList(name: String) {
        if (name.isNotBlank()) {
            ReminderRepository.addReminderList(ReminderList(name = name))
        }
    }
    
    fun updateReminderList(list: ReminderList) {
        ReminderRepository.updateReminderList(list)
    }

    fun addReminder(
        title: String,
        listId: String,
        notes: String? = null,
        dueDate: Long? = null,
        priority: Priority = Priority.NONE,
        isSoundEnabled: Boolean = true,
        notificationSoundUri: String? = null,
        isVibrateEnabled: Boolean = true,
        advanceNotificationMinutes: Int = 0,
        repeatCount: Int = 0,
        repeatIntervalMinutes: Int = 5
    ) {
        if (title.isNotBlank()) {
            val reminder = Reminder(
                title = title,
                listId = listId,
                notes = notes,
                dueDate = dueDate,
                priority = priority,
                isSoundEnabled = isSoundEnabled,
                notificationSoundUri = notificationSoundUri,
                isVibrateEnabled = isVibrateEnabled,
                advanceNotificationMinutes = advanceNotificationMinutes,
                repeatCount = repeatCount,
                repeatIntervalMinutes = repeatIntervalMinutes
            )
            ReminderRepository.addReminder(reminder)
            // Schedule notification if dueDate is not null
            dueDate?.let {
                val actualTriggerTime = it - (advanceNotificationMinutes * 60 * 1000L)
                if (actualTriggerTime > System.currentTimeMillis()) {
                    alarmScheduler.schedule(reminder.copy(dueDate = actualTriggerTime)) // Schedule with adjusted time
                }
            }
        }
    }

    fun updateReminder(reminder: Reminder) {
        ReminderRepository.updateReminder(reminder)
        // Re-schedule or cancel notification
        val actualTriggerTime = reminder.dueDate?.let {
            it - (reminder.advanceNotificationMinutes * 60 * 1000L)
        }

        if (actualTriggerTime != null && actualTriggerTime > System.currentTimeMillis() && !reminder.isCompleted) {
            alarmScheduler.schedule(reminder.copy(dueDate = actualTriggerTime)) // Schedule with adjusted time
        } else {
            alarmScheduler.cancel(reminder)
        }
    }

    fun toggleReminderCompletion(reminder: Reminder) {
        val updatedReminder = reminder.copy(isCompleted = !reminder.isCompleted)
        ReminderRepository.updateReminder(updatedReminder)
        if (updatedReminder.isCompleted) {
            alarmScheduler.cancel(updatedReminder)
        } else {
            updatedReminder.dueDate?.let {
                if (it > System.currentTimeMillis()) {
                    alarmScheduler.schedule(updatedReminder)
                }
            }
        }
    }

    fun getReminder(reminderId: String, listId: String): Reminder? {
        return ReminderRepository.getRemindersForList(listId).find { it.id == reminderId }
    }

    // Companion object to create ViewModel with dependencies (basic manual DI)
    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
                        return ReminderViewModel(AlarmScheduler(context)) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}
