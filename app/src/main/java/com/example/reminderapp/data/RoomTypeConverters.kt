package com.example.reminderapp.data

import androidx.room.TypeConverter
import com.example.reminderapp.data.model.Priority
import com.example.reminderapp.data.model.SoundFetchState

class RoomTypeConverters {
    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(name: String): Priority = Priority.valueOf(name)

    @TypeConverter
    fun fromSoundFetchState(state: SoundFetchState): String = state.name

    @TypeConverter
    fun toSoundFetchState(name: String): SoundFetchState = SoundFetchState.valueOf(name)

    @TypeConverter
    fun fromNullableLong(value: Long?): String? = value?.toString()

    @TypeConverter
    fun toNullableLong(value: String?): Long? = value?.toLongOrNull()
}
