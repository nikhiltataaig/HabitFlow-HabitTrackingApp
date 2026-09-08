package com.example.habitflow.data.local.room.mapper

import com.example.habitflow.data.local.room.entity.HabitCompletionEntity
import com.example.habitflow.data.local.room.entity.SyncStatus
import com.example.habitflow.domain.model.HabitCompletion

fun HabitCompletionEntity.toDomain(): HabitCompletion {
    return HabitCompletion(
        id = "${habitId}_${date}",
        userId = userId,
        habitId = habitId,
        date = date,
        completedAt = completedAt
    )
}

fun HabitCompletion.toEntity(
    syncStatus: SyncStatus
): HabitCompletionEntity {
    return HabitCompletionEntity(
        habitId = habitId,
        userId = userId,
        date = date,
        completedAt = completedAt,
        syncStatus = syncStatus
    )
}