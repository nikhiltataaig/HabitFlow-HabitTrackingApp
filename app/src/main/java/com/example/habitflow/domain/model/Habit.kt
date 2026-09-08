package com.example.habitflow.domain.model

data class Habit(
    val id: String,
    val userId: String,
    val name: String,
    val description: String,
    val frequency: HabitFrequency,
    val targetDays: List<Int>,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)


enum class HabitFrequency {
    DAILY,
    WEEKDAYS,
    WEEKENDS,
    WEEKLY
}