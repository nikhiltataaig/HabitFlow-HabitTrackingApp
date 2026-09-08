package com.example.habitflow.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.habitflow.data.local.room.entity.HabitEntity


@Dao
interface HabitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(
        habit: HabitEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(
        habits: List<HabitEntity>
    )

    @Query("""
        SELECT *
        FROM habits
        WHERE userId = :userId
        AND isActive = 1
        ORDER BY createdAt DESC
    """)
    suspend fun getActiveHabits(
        userId: String
    ): List<HabitEntity>

    @Query("""
        SELECT *
        FROM habits
        WHERE id = :habitId
        LIMIT 1
    """)
    suspend fun getHabit(
        habitId: String
    ): HabitEntity?

    @Update
    suspend fun updateHabit(
        habit: HabitEntity
    )

    @Query("""
        DELETE FROM habits
        WHERE id = :habitId
    """)
    suspend fun deleteHabit(
        habitId: String
    )
}