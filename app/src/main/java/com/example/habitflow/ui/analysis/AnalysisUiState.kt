package com.example.habitflow.ui.analysis



data class AnalysisUiState(
    val totalHabits: Int = 0,
    val completedToday: Int = 0,
    val completionRate: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)