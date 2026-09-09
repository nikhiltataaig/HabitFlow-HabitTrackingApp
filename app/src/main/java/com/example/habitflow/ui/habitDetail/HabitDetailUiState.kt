package com.example.habitflow.ui.habitDetail

import com.example.habitflow.domain.model.Habit
import java.time.LocalDate

data class HabitDetailUiState(
    val habit: Habit? = null,
    val completedDates: Set<LocalDate> = emptySet(),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val completedCount: Int = 0,
    val completionRate: Int = 0,

    val errorMessage: String? = null
)