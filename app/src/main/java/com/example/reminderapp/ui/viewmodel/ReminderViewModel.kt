package com.example.reminderapp.ui.viewmodel

import android.app.Application // Import Application
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
import com.example.reminderapp.data.model.SoundFetchState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import androidx.core.content.FileProvider // Import FileProvider
import kotlinx.coroutines.ensureActive

class ReminderViewModel(
    private val application: Application, // Store application context
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

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

    // Get counts of completed and active reminders for a list
    fun getReminderCountsForList(listId: String): StateFlow<Pair<Int, Int>> {
        return ReminderRepository.reminders
            .map { reminders ->
                val listReminders = reminders.filter { it.listId == listId }
                val completedCount = listReminders.count { it.isCompleted }
                val activeCount = listReminders.size - completedCount
                Pair(activeCount, completedCount)
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, Pair(0, 0))
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
        notificationsEnabled: Boolean = true, // New param
        remoteSoundUrl: String? = null, // new field
        isVibrateEnabled: Boolean = true,
        advanceNotificationMinutes: Int = 0,
        repeatCount: Int = 0,
        repeatIntervalMinutes: Int = 5
    ) {
        if (title.isNotBlank()) {
            val newReminder = Reminder(
                title = title,
                listId = listId,
                notes = notes,
                dueDate = dueDate,
                priority = priority,
                isSoundEnabled = isSoundEnabled,
                notificationsEnabled = notificationsEnabled, // Pass new field
                remoteSoundUrl = remoteSoundUrl,
                localSoundUri = null, // Initially null
                soundFetchState = if (remoteSoundUrl != null && isSoundEnabled) SoundFetchState.IDLE else SoundFetchState.IDLE, // Or FETCHING if auto-fetch
                isVibrateEnabled = isVibrateEnabled,
                advanceNotificationMinutes = advanceNotificationMinutes,
                repeatCount = repeatCount,
                repeatIntervalMinutes = repeatIntervalMinutes
            )
            ReminderRepository.addReminder(newReminder)
            // Schedule notification only if enabled
            if (notificationsEnabled) {
                newReminder.dueDate?.let {
                    val actualTriggerTime = it - (newReminder.advanceNotificationMinutes * 60 * 1000L)
                    alarmScheduler.schedule(newReminder.copy(dueDate = actualTriggerTime))
                }
            }
            // Optionally, auto-fetch sound if URL is provided
            if (newReminder.isSoundEnabled && newReminder.remoteSoundUrl != null && newReminder.soundFetchState == SoundFetchState.IDLE) {
                fetchCustomSound(newReminder.id, newReminder.remoteSoundUrl!!)
            }
        }
    }

    fun updateReminder(reminder: Reminder) {
        ReminderRepository.updateReminder(reminder)
        // Cancel or schedule notification based on notificationsEnabled
        if (reminder.notificationsEnabled) {
            reminder.dueDate?.let {
                val actualTriggerTime = it - (reminder.advanceNotificationMinutes * 60 * 1000L)
                alarmScheduler.schedule(reminder.copy(dueDate = actualTriggerTime))
            }
        } else {
            alarmScheduler.cancel(reminder)
        }
    }

    fun fetchCustomSound(reminderId: String, remoteUrl: String) {
        viewModelScope.launch {
            var reminder = ReminderRepository.reminders.value.find { it.id == reminderId } ?: return@launch

            val soundsDir = File(application.cacheDir, "sounds")
            val expectedFileName = "${reminderId}_${remoteUrl.hashCode()}.mp3"
            val expectedOutputFile = File(soundsDir, expectedFileName)

            if (reminder.remoteSoundUrl != remoteUrl ||
                reminder.soundFetchState == SoundFetchState.ERROR ||
                (reminder.remoteSoundUrl == remoteUrl && reminder.soundFetchState == SoundFetchState.FETCHED && !expectedOutputFile.exists())) {
                reminder = reminder.copy(soundFetchState = SoundFetchState.IDLE, localSoundUri = null, soundFetchProgress = null, remoteSoundUrl = remoteUrl)
                ReminderRepository.updateReminder(reminder)
            } else if (reminder.soundFetchState == SoundFetchState.FETCHING ||
                       (reminder.soundFetchState == SoundFetchState.FETCHED && reminder.remoteSoundUrl == remoteUrl && expectedOutputFile.exists())) {
                return@launch
            }

            ReminderRepository.updateReminder(reminder.copy(soundFetchState = SoundFetchState.FETCHING, soundFetchProgress = 0, remoteSoundUrl = remoteUrl))
            
            launch(Dispatchers.IO) {
                try {
                    val url = URL(remoteUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connect()

                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        throw Exception("Server returned HTTP ${connection.responseCode} ${connection.responseMessage}")
                    }

                    val contentLength = connection.contentLengthLong
                    val inputStream: InputStream = connection.inputStream
                    val soundsDirInner = File(application.cacheDir, "sounds") // Re-access for IO thread safety if needed
                    if (!soundsDirInner.exists()) {
                        soundsDirInner.mkdirs()
                    }
                    val fileNameInner = "${reminderId}_${remoteUrl.hashCode()}.mp3"
                    val outputFileInner = File(soundsDirInner, fileNameInner)

                    val outputStream = FileOutputStream(outputFileInner)
                    var bytesCopied: Long = 0
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytes = inputStream.read(buffer)
                    var lastProgressReportTime = System.currentTimeMillis()

                    while (bytes >= 0) {
                        ensureActive() // Check for coroutine cancellation
                        outputStream.write(buffer, 0, bytes)
                        bytesCopied += bytes
                        bytes = inputStream.read(buffer)

                        if (contentLength > 0) {
                            val progress = ((bytesCopied * 100) / contentLength).toInt()
                            // Report progress not too frequently to avoid overwhelming UI updates
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastProgressReportTime > 200 || progress == 100) { // Update every 200ms or at 100%
                                launch(Dispatchers.Main) {
                                    val currentReminderProg = ReminderRepository.reminders.value.find { it.id == reminderId }
                                    currentReminderProg?.let {
                                        if (it.soundFetchState == SoundFetchState.FETCHING) { // Only update progress if still fetching
                                            ReminderRepository.updateReminder(it.copy(soundFetchProgress = progress))
                                        }
                                    }
                                }
                                lastProgressReportTime = currentTime
                            }
                        }
                    }

                    outputStream.close()
                    inputStream.close()

                    val localFileUri = FileProvider.getUriForFile(
                        application,
                        "${application.packageName}.provider",
                        outputFileInner
                    )

                    launch(Dispatchers.Main) {
                        val currentReminderSuccess = ReminderRepository.reminders.value.find { it.id == reminderId }
                        currentReminderSuccess?.let {
                            ReminderRepository.updateReminder(
                                it.copy(
                                    localSoundUri = localFileUri.toString(),
                                    soundFetchState = SoundFetchState.FETCHED,
                                    soundFetchProgress = null, // Clear progress on success
                                    remoteSoundUrl = remoteUrl
                                )
                            )
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    ensureActive() // Check for cancellation before updating state on error
                    launch(Dispatchers.Main) {
                         val currentReminderError = ReminderRepository.reminders.value.find { it.id == reminderId }
                         currentReminderError?.let {
                            ReminderRepository.updateReminder(
                                it.copy(
                                    soundFetchState = SoundFetchState.ERROR,
                                    localSoundUri = null,
                                    soundFetchProgress = null, // Clear progress on error
                                    remoteSoundUrl = remoteUrl
                                )
                            )
                        }
                    }
                }
            }
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
            }        }
    }

    fun deleteReminder(reminder: Reminder) {
        // Cancel any alarms for this reminder
        alarmScheduler.cancel(reminder)
        
        // Delete from repository
        ReminderRepository.deleteReminder(reminder.id)
    }

    fun getReminder(reminderId: String, listId: String): Reminder? {
        return ReminderRepository.getRemindersForList(listId).find { it.id == reminderId }
    }

    // Companion object to create ViewModel with dependencies (basic manual DI)
    companion object {
        fun provideFactory(applicationContext: Context): ViewModelProvider.Factory { // Pass full context
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
                        // Ensure Application context is passed if needed, or cast context to Application
                        val app = applicationContext as? Application ?: applicationContext.applicationContext as Application
                        return ReminderViewModel(app, AlarmScheduler(applicationContext)) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}
