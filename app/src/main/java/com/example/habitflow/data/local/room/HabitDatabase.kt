package com.example.habitflow.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.habitflow.data.local.room.converter.HabitTypeConverters
import com.example.habitflow.data.local.room.dao.CompletionDao
import com.example.habitflow.data.local.room.dao.CompletionSyncOperationDao
import com.example.habitflow.data.local.room.dao.HabitDao
import com.example.habitflow.data.local.room.entity.HabitEntity
import com.example.habitflow.data.local.room.entity.HabitCompletionEntity
import com.example.habitflow.data.local.room.entity.CompletionSyncOperationEntity



@Database(
    entities = [
        HabitEntity::class,
        HabitCompletionEntity::class,
        CompletionSyncOperationEntity::class
    ],
    version = 2,
    exportSchema = true
)@TypeConverters(HabitTypeConverters::class)
abstract class HabitDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    abstract fun completionDao(): CompletionDao

    abstract fun completionSyncOperationDao():
            CompletionSyncOperationDao
}