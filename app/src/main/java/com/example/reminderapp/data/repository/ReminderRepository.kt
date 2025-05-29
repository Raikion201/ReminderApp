package com.example.reminderapp.data.repository

import android.content.Context
import com.example.reminderapp.data.ReminderDatabase
import com.example.reminderapp.data.model.Reminder
import com.example.reminderapp.data.model.ReminderList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ReminderRepository(context: Context) {
    private val db = ReminderDatabase.getDatabase(context)
    private val reminderDao = db.reminderDao()
    private val reminderListDao = db.reminderListDao()

    fun getRemindersForList(listId: String): Flow<List<Reminder>> = reminderDao.getRemindersForList(listId)
    suspend fun getReminderById(id: String): Reminder? = reminderDao.getReminderById(id)
    suspend fun insertReminder(reminder: Reminder) = reminderDao.insert(reminder)
    suspend fun updateReminder(reminder: Reminder) = reminderDao.update(reminder)
    suspend fun deleteReminder(reminder: Reminder) = reminderDao.delete(reminder)
    suspend fun deleteReminderById(id: String) = reminderDao.deleteById(id)

    fun getAllLists(): Flow<List<ReminderList>> = reminderListDao.getAllLists()
    suspend fun getListById(id: String): ReminderList? = reminderListDao.getListById(id)
    suspend fun insertList(list: ReminderList) = reminderListDao.insert(list)
    suspend fun updateList(list: ReminderList) = reminderListDao.update(list)
    suspend fun deleteList(list: ReminderList) = reminderListDao.delete(list)
    suspend fun deleteListById(id: String) = reminderListDao.deleteById(id)
}
