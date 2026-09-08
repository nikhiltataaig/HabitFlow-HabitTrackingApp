package com.example.habitflow.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "completion_sync_operations"
)
data class CompletionSyncOperationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userId: String,
    val habitId: String,
    val date: String,

    val operation: SyncOperation,

    val createdAt: Long
)