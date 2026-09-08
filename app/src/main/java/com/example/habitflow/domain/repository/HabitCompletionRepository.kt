package com.example.habitflow.domain.repository

import com.example.habitflow.domain.model.HabitCompletion

interface CompletionRepository {

    suspend fun createCompletion(
        completion: HabitCompletion
    ): Result<Unit>

    suspend fun getCompletion(
        userId: String,
        completionId: String
    ): Result<HabitCompletion>

    suspend fun getCompletionsForHabit(
        userId: String,
        habitId: String
    ): Result<List<HabitCompletion>>

    suspend fun getAllCompletions(
        userId: String
    ): Result<List<HabitCompletion>>

    suspend fun isHabitCompletedToday(
        habitId: String,
        date: String
    ): Result<Boolean>

    suspend fun deleteCompletion(
        userId: String,
        habitId: String,
        date: String
    ): Result<Unit>
}