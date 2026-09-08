package com.example.habitflow.data.model

import com.google.firebase.Timestamp

data class HabitCompletionDto(
    val id: String = "",
    val userId: String = "",
    val habitId: String = "",
    val date: String = "",
    val completedAt: Timestamp? = null
)