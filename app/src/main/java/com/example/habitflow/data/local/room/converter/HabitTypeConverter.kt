package com.example.habitflow.data.local.room.converter

import androidx.room.TypeConverter
import com.example.habitflow.data.local.room.entity.SyncOperation
import com.example.habitflow.data.local.room.entity.SyncStatus

class HabitTypeConverters {

    @TypeConverter
    fun fromIntList(value: List<Int>): String =
        value.joinToString(",")

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        if (value.isBlank()) return emptyList()

        return value.split(",").map { it.toInt() }
    }

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String =
        value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus =
        SyncStatus.valueOf(value)

    @TypeConverter
    fun fromSyncOperation(value: SyncOperation): String =
        value.name

    @TypeConverter
    fun toSyncOperation(value: String): SyncOperation =
        SyncOperation.valueOf(value)
}