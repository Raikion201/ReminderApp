package com.example.reminderapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log // Import Log
import androidx.core.app.NotificationCompat
import com.example.reminderapp.MainActivity
import com.example.reminderapp.R
import com.example.reminderapp.data.model.Priority
import com.example.reminderapp.data.model.Reminder
import com.example.reminderapp.data.model.SoundFetchState

object NotificationHelper {

    const val CHANNEL_ID = "reminder_channel_id"
    private const val CHANNEL_NAME = "Reminder Notifications"
    private const val CHANNEL_DESCRIPTION = "Shows notifications for reminders"
    private const val TAG = "NotificationHelper" // For logging

    // Channel for silent/low priority notifications
    const val SILENT_CHANNEL_ID = "reminder_silent_channel_id"
    private const val SILENT_CHANNEL_NAME = "Silent Reminders"
    private const val SILENT_CHANNEL_DESCRIPTION = "Shows silent notifications for reminders"


    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Default/High Importance Channel
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE) // Ensure this is desired for all sounds on this channel
                .build()

            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                // Set default sound and attributes for the channel.
                // If a notification on this channel calls builder.setSound(uri) without attributes,
                // these channel attributes will be used.
                setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes)
                enableVibration(true) // Ensure vibration is enabled on the channel
            }

            // Silent/Low Importance Channel
            val silentImportance = NotificationManager.IMPORTANCE_LOW
            val silentChannel = NotificationChannel(SILENT_CHANNEL_ID, SILENT_CHANNEL_NAME, silentImportance).apply {
                description = SILENT_CHANNEL_DESCRIPTION
                setSound(null, null) // No sound for this channel
                enableVibration(false) // No vibration for this channel
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(silentChannel)
        }
    }

    fun showNotification(context: Context, reminder: Reminder) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("REMINDER_ID_FROM_NOTIFICATION", reminder.id) // For navigation
            putExtra("LIST_ID_FROM_NOTIFICATION", reminder.listId) // For navigation
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            reminder.id.hashCode(), // Unique request code per reminder
            intent,
            pendingIntentFlags
        )

        val channelIdToUse: String
        val notificationPriority: Int

        // Determine if a custom sound is successfully fetched and enabled
        val useCustomSound = reminder.isSoundEnabled &&
                reminder.soundFetchState == SoundFetchState.FETCHED &&
                reminder.localSoundUri != null

        if (useCustomSound) {
            // If using a custom sound, always use the main channel that allows sound
            channelIdToUse = CHANNEL_ID
            // Set notification priority based on reminder's priority, but ensure it's on a sound-enabled channel
            notificationPriority = when (reminder.priority) {
                Priority.HIGH -> NotificationCompat.PRIORITY_MAX
                Priority.MEDIUM -> NotificationCompat.PRIORITY_HIGH
                Priority.LOW -> NotificationCompat.PRIORITY_LOW
                Priority.NONE -> NotificationCompat.PRIORITY_DEFAULT // Or PRIORITY_LOW if preferred for NONE with custom sound
            }
        } else {
            // Original logic if not using a custom sound
            when (reminder.priority) {
                Priority.HIGH -> {
                    channelIdToUse = CHANNEL_ID
                    notificationPriority = NotificationCompat.PRIORITY_MAX
                }
                Priority.MEDIUM -> {
                    channelIdToUse = CHANNEL_ID
                    notificationPriority = NotificationCompat.PRIORITY_HIGH
                }
                Priority.LOW -> {
                    channelIdToUse = CHANNEL_ID // Still use default channel, but lower priority
                    notificationPriority = NotificationCompat.PRIORITY_LOW
                }
                Priority.NONE -> { // Treat NONE as default/medium or map to silent
                    channelIdToUse = SILENT_CHANNEL_ID
                    notificationPriority = NotificationCompat.PRIORITY_DEFAULT
                }
            }
        }


        val builder = NotificationCompat.Builder(context, channelIdToUse)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with a proper notification icon
            .setContentTitle(reminder.title)
            .setContentText(reminder.notes ?: "Your reminder is due!")
            .setPriority(notificationPriority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss notification when tapped


        if (reminder.isSoundEnabled) {
            if (useCustomSound) { // useCustomSound is true if fetched, valid, and enabled
                try {
                    val soundUri = Uri.parse(reminder.localSoundUri)
                    Log.d(TAG, "Attempting to use custom sound URI: $soundUri for channel $channelIdToUse")
                    // Use the setSound(Uri) overload. The AudioAttributes from the channel will be used.
                    builder.setSound(soundUri)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing custom sound URI: ${reminder.localSoundUri}", e)
                    // Fallback to default sound if local URI is invalid or parsing fails
                    builder.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                }
            } else {
                Log.d(TAG, "Using default notification sound for channel $channelIdToUse.")
                // Use default system sound if no custom sound is specified or ready, but sound is enabled
                builder.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            }
        } else {
            Log.d(TAG, "Sound is disabled for this notification.")
            builder.setSound(null) // Explicitly no sound
        }

        if (reminder.isVibrateEnabled) {
            Log.d(TAG, "Vibration is enabled for this notification.")
            // Rely on channel's vibration setting.
            // If CHANNEL_ID has vibration enabled, it should vibrate.
            // If we want to be absolutely sure, we can provide a pattern.
            // For now, let's assume the channel handles default vibration if builder.setVibrate(null) is not called.
        } else {
            Log.d(TAG, "Vibration is disabled for this notification.")
            builder.setVibrate(longArrayOf(0L)) // Explicitly no vibration
        }

        // For HIGH priority, consider setFullScreenIntent for pop-up like behavior
        // if (reminder.priority == Priority.HIGH) {
        // val fullScreenIntent = Intent(context, MainActivity::class.java) // Or a dedicated activity
        // val fullScreenPendingIntent = PendingIntent.getActivity(context, reminder.id.hashCode() + 1, fullScreenIntent, pendingIntentFlags)
        // builder.setFullScreenIntent(fullScreenPendingIntent, true)
        // }


        // Use reminder.id.hashCode() or a unique integer for notification ID
        Log.i(TAG, "Showing notification for reminder: ${reminder.title} on channel $channelIdToUse")
        notificationManager.notify(reminder.id.hashCode(), builder.build())

        // Handle repeat logic (Simplified: re-schedule next alarm if repeatCount > 0)
        // This is a basic implementation. A robust one would need to track dismissal/view.
        val currentRepeatCount = intent.getIntExtra("REPEAT_COUNT_CURRENT", reminder.repeatCount)
        if (currentRepeatCount > 0 && !reminder.isCompleted) {
            val nextRepeatTime = System.currentTimeMillis() + (reminder.repeatIntervalMinutes * 60 * 1000L)
            val repeatReminder = reminder.copy(
                dueDate = nextRepeatTime,
                // Decrement repeat count for the *next* alarm's intent
                // This state needs to be passed carefully.
            )

            val alarmScheduler = AlarmScheduler(context) // Consider DI
            alarmScheduler.scheduleRepeat(
                repeatReminder,
                currentRepeatCount - 1
            )
        }
    }
}
