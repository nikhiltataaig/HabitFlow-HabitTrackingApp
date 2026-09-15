package com.example.habitflow.data.model

import com.example.habitflow.domain.model.HabitCompletion
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate

fun HabitCompletionDto.toDomain(): HabitCompletion {
    val domainDate = if (date.isBlank() && completedAt != null) {
        try {
            val instant = Instant.ofEpochMilli(completedAt.toDate().time)
            instant.atZone(ZoneId.systemDefault()).toLocalDate().toString()
        } catch (e: Exception) {
            LocalDate.now().toString()
        }
    } else if (date.isBlank()) {
        LocalDate.now().toString()
    } else {
        date
    }

    return HabitCompletion(
        id = id,
        userId = userId,
        habitId = habitId,
        date = domainDate,
        completedAt = completedAt?.toDate()?.time ?: 0L
    )
}