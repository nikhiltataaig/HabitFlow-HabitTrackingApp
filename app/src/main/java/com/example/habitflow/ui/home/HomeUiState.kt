package com.example.habitflow.ui.home

import com.example.habitflow.domain.model.Habit



data class HomeUiState(
    val isLoading: Boolean = false,
    val habits: List<Habit> = emptyList(),
    val completedHabitIds: Set<String> = emptySet(),
    val errorMessage: String? = null
) {
    val completedToday: Int
        get() = completedHabitIds.size
}