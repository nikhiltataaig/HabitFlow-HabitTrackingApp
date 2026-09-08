package com.example.habitflow.data.local.room.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.habitflow.data.local.room.entity.CompletionSyncOperationEntity
import com.example.habitflow.data.local.room.entity.HabitCompletionEntity
import com.example.habitflow.data.local.room.entity.SyncOperation
import com.example.habitflow.data.local.room.entity.SyncStatus


@Dao
interface CompletionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(
        completion: HabitCompletionEntity
    )

    @Query("""
        SELECT *
        FROM habit_completions
        WHERE userId = :userId
        AND habitId = :habitId
        ORDER BY date DESC
    """)
    suspend fun getCompletionsForHabit(
        userId: String,
        habitId: String
    ): List<HabitCompletionEntity>

    @Query("""
        SELECT *
        FROM habit_completions
        WHERE userId = :userId
        ORDER BY date DESC
    """)
    suspend fun getAllCompletions(
        userId: String
    ): List<HabitCompletionEntity>

    @Query("""
        SELECT *
        FROM habit_completions
        WHERE userId = :userId
        AND syncStatus = :status
    """)
    suspend fun getPendingCompletions(
        userId: String,
        status: SyncStatus = SyncStatus.PENDING
    ): List<HabitCompletionEntity>

    @Query("""
        SELECT EXISTS(
            SELECT 1
            FROM habit_completions
            WHERE habitId = :habitId
            AND date = :date
        )
    """)
    suspend fun isCompleted(
        habitId: String,
        date: String
    ): Boolean

    @Query("""
        UPDATE habit_completions
        SET syncStatus = :status
        WHERE habitId = :habitId
        AND date = :date
    """)
    suspend fun updateSyncStatus(
        habitId: String,
        date: String,
        status: SyncStatus
    )

    @Query("""
        DELETE FROM habit_completions
        WHERE habitId = :habitId
        AND date = :date
    """)
    suspend fun deleteCompletion(
        habitId: String,
        date: String
    )

    @Query("""
    SELECT * FROM habit_completions
    WHERE userId = :userId
    AND syncStatus = :status
""")
    suspend fun getCompletionsBySyncStatus(
        userId: String,
        status: SyncStatus
    ): List<HabitCompletionEntity>


    @Query("""
    DELETE FROM habit_completions
    WHERE userId = :userId
    AND syncStatus = :status
    AND habitId = :habitId
    AND date NOT IN (:remoteDates)
""")
    suspend fun deleteMissingSyncedCompletions(
        userId: String,
        status: SyncStatus,
        habitId: String,
        remoteDates: List<String>
    )

}