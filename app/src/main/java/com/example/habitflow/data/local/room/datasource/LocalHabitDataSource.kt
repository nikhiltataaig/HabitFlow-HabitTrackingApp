package com.example.habitflow.data.local.room.datasource

import com.example.habitflow.data.local.room.dao.HabitDao
import com.example.habitflow.data.local.room.entity.HabitEntity
import javax.inject.Inject


class LocalHabitDataSource @Inject constructor(
private val habitDao: HabitDao
) {

    suspend fun insertHabit(
        habit: HabitEntity
    ) {
        habitDao.insertHabit(habit)
    }

    suspend fun insertHabits(
        habits: List<HabitEntity>
    ) {
        habitDao.insertHabits(habits)
    }

    suspend fun getHabit(
        habitId: String
    ): HabitEntity? {
        return habitDao.getHabit(habitId)
    }

    suspend fun getActiveHabits(
        userId: String
    ): List<HabitEntity> {
        return habitDao.getActiveHabits(userId)
    }

    suspend fun updateHabit(
        habit: HabitEntity
    ) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(
        habitId: String
    ) {
        habitDao.deleteHabit(habitId)
    }
}