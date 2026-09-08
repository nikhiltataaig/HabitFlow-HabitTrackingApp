package com.example.habitflow.ui.createHabit

import com.example.habitflow.domain.model.HabitFrequency

data class CreateHabitUiState(
    val name: String = "",
    val description: String = "",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val targetDays: List<Int> = emptyList(),
    val errorMessage: String? = null
)