package com.example.reminderapp.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.reminderapp.data.model.Priority
import com.example.reminderapp.data.model.Reminder
import com.example.reminderapp.data.model.SoundFetchState
import com.example.reminderapp.data.repository.ReminderRepository // For fetching reminder details if needed

class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra("REMINDER_ID")
        val reminderTitle = intent.getStringExtra("REMINDER_TITLE") ?: "Reminder"
        val reminderNotes = intent.getStringExtra("REMINDER_NOTES")
        val listId = intent.getStringExtra("REMINDER_LIST_ID") ?: "" // Required for creating a dummy reminder if needed

        val priorityOrdinal = intent.getIntExtra("REMINDER_PRIORITY_ORDINAL", Priority.NONE.ordinal)
        val priority = Priority.values().getOrElse(priorityOrdinal) { Priority.NONE }
        val soundEnabled = intent.getBooleanExtra("REMINDER_SOUND_ENABLED", true)
        // val soundUri = intent.getStringExtra("REMINDER_SOUND_URI") // Old field
        val remoteSoundUrl = intent.getStringExtra("REMINDER_REMOTE_SOUND_URL")
        val localSoundUri = intent.getStringExtra("REMINDER_LOCAL_SOUND_URI")
        val soundFetchStateOrdinal = intent.getIntExtra("REMINDER_SOUND_FETCH_STATE_ORDINAL", SoundFetchState.IDLE.ordinal)
        val soundFetchState = SoundFetchState.values().getOrElse(soundFetchStateOrdinal) { SoundFetchState.IDLE }
        val soundFetchProgressExtra = intent.getIntExtra("REMINDER_SOUND_FETCH_PROGRESS", -1)
        val soundFetchProgress = if (soundFetchProgressExtra == -1) null else soundFetchProgressExtra
        val vibrateEnabled = intent.getBooleanExtra("REMINDER_VIBRATE_ENABLED", true)
        val repeatCount = intent.getIntExtra("REMINDER_REPEAT_COUNT", 0)
        val repeatInterval = intent.getIntExtra("REMINDER_REPEAT_INTERVAL", 5)
        val currentRepeatCountForNotification = intent.getIntExtra("REPEAT_COUNT_CURRENT", repeatCount)
        val isRepeat = intent.getBooleanExtra("IS_REPEAT_ALARM", false)


        if (reminderId != null) {
            // Create a temporary Reminder object from intent extras for NotificationHelper
            // This is a simplified approach. A more robust solution might involve querying
            // the database/repository for the reminder by its ID.
            val reminder = Reminder(
                id = reminderId,
                title = reminderTitle,
                notes = reminderNotes,
                listId = listId, // listId is needed for Reminder constructor
                priority = priority,
                isSoundEnabled = soundEnabled,
                // notificationSoundUri = soundUri, // Old field
                remoteSoundUrl = remoteSoundUrl,
                localSoundUri = localSoundUri,
                soundFetchState = soundFetchState,
                soundFetchProgress = soundFetchProgress,
                isVibrateEnabled = vibrateEnabled,
                repeatCount = repeatCount, // Original repeat count
                repeatIntervalMinutes = repeatInterval
                // dueDate is not strictly needed here for showing notification,
                // but NotificationHelper might re-schedule based on original reminder.dueDate
                // For repeat logic, the current time is the trigger time.
            )
            // Pass the currentRepeatCount to the notification helper if it's a repeat
            // The NotificationHelper will use this to decide if it needs to schedule the *next* repeat.
            NotificationHelper.showNotification(context, reminder)
        }
    }
}
