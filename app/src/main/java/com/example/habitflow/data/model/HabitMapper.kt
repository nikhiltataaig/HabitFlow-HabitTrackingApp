package com.example.habitflow.data.model

import com.example.habitflow.domain.model.Habit
import com.example.habitflow.domain.model.HabitFrequency

fun HabitDto.toDomain(): Habit {

    val habitFrequency = try {
        HabitFrequency.valueOf(frequency)
    } catch (e: IllegalArgumentException) {
        HabitFrequency.DAILY
    }

    return Habit(
        id = id,
        userId = userId,
        name = name,
        description = description,
        frequency = habitFrequency,
        targetDays = targetDays,
        isActive = isActive,
        createdAt = createdAt?.toDate()?.time ?: 0L,
        updatedAt = updatedAt?.toDate()?.time ?: 0L
    )
}