package com.example.habitflow.ui.analysis

import com.example.habitflow.domain.model.Habit
import java.time.LocalDate


data class AnalysisUiState(
    val totalHabits: Int = 0,
    val completedToday: Int = 0,
    val completionRate: Int = 0,
    val habitAnalysis: List<HabitAnalysis> = emptyList(),
    val dailyActivity: List<DailyHabitActivity> = emptyList(),

    val errorMessage: String? = null
)


data class HabitAnalysis(
    val habit: Habit,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val completedCount: Int = 0,
    val completedDates: Set<LocalDate> = emptySet()
)

data class DailyHabitActivity(
    val date: LocalDate,
    val totalHabits: Int,
    val completedHabits: Int
)