package com.example.reminderapp.data.repository

import androidx.room.*
import com.example.reminderapp.data.model.ReminderList
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderListDao {
    @Query("SELECT * FROM reminder_lists")
    fun getAllLists(): Flow<List<ReminderList>>

    @Query("SELECT * FROM reminder_lists WHERE id = :id")
    suspend fun getListById(id: String): ReminderList?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ReminderList)

    @Update
    suspend fun update(list: ReminderList)

    @Delete
    suspend fun delete(list: ReminderList)

    @Query("DELETE FROM reminder_lists WHERE id = :id")
    suspend fun deleteById(id: String)
}
