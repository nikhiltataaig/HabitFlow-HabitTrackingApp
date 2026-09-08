package com.example.habitflow.data.model

import com.example.habitflow.domain.model.HabitCompletion

fun HabitCompletionDto.toDomain(): HabitCompletion {
    return HabitCompletion(
        id = id,
        userId = userId,
        habitId = habitId,
        date = date,
        completedAt = completedAt?.toDate()?.time ?: 0L
    )
}