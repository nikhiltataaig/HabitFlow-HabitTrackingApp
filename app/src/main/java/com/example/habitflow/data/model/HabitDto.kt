package com.example.habitflow.data.model

import com.example.habitflow.domain.model.HabitFrequency
import com.google.firebase.Timestamp

data class HabitDto(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val frequency: String = HabitFrequency.DAILY.name,
    val targetDays: List<Int> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)