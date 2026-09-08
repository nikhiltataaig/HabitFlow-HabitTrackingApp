package com.example.habitflow.data.local.room.mapper

import com.example.habitflow.data.local.room.entity.HabitEntity
import com.example.habitflow.domain.model.Habit
import com.example.habitflow.domain.model.HabitFrequency

fun HabitEntity.toDomain(): Habit {
    return Habit(
        id = id,
        userId = userId,
        name = name,
        description = description,
        frequency = HabitFrequency.valueOf(frequency),
        targetDays = targetDays,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Habit.toEntity(): HabitEntity {
    return HabitEntity(
        id = id,
        userId = userId,
        name = name,
        description = description,
        frequency = frequency.name,
        targetDays = targetDays,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
