package com.example.habitflow.domain.repository

import com.example.habitflow.data.model.HabitDto
import com.example.habitflow.domain.model.Habit

interface HabitRepository {

    suspend fun createHabit(
        habit: Habit
    ): Result<Unit>

    suspend fun getHabit(
        userId: String,
        habitId: String
    ): Result<Habit>

    suspend fun getHabits(
        userId: String
    ): Result<List<Habit>>

    suspend fun updateHabit(
        habit: Habit
    ): Result<Unit>

    suspend fun deleteHabit(
        userId: String,
        habitId: String
    ): Result<Unit>
}