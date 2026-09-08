package com.example.habitflow.data.local.room.entity

import androidx.room.Entity

@Entity(
    tableName = "habit_completions",
    primaryKeys = ["habitId", "date"]
)
data class HabitCompletionEntity(

    val habitId: String,

    val userId: String,

    val date: String,

    val completedAt: Long,

    val syncStatus: SyncStatus
)


