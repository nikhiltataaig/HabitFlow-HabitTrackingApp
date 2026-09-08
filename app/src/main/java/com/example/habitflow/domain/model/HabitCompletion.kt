package com.example.habitflow.domain.model

data class HabitCompletion(
    val id: String,
    val userId: String,
    val habitId: String,
    val date: String,
    val completedAt: Long
)