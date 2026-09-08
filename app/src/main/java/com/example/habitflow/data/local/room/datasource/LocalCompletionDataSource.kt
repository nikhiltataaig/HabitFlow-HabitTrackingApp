package com.example.habitflow.data.local.room.datasource


import com.example.habitflow.data.local.room.dao.CompletionDao
import com.example.habitflow.data.local.room.entity.HabitCompletionEntity
import com.example.habitflow.data.local.room.entity.SyncStatus
import javax.inject.Inject

class LocalCompletionDataSource @Inject constructor(
    private val completionDao: CompletionDao
) {

    suspend fun insertCompletion(
        completion: HabitCompletionEntity
    ) {
        completionDao.insertCompletion(completion)
    }

    suspend fun getCompletionsForHabit(
        userId: String,
        habitId: String
    ): List<HabitCompletionEntity> {
        return completionDao.getCompletionsForHabit(
            userId = userId,
            habitId = habitId
        )
    }

    suspend fun getAllCompletions(
        userId: String
    ): List<HabitCompletionEntity> {
        return completionDao.getAllCompletions(userId)
    }

    suspend fun getPendingCompletions(
        userId: String
    ): List<HabitCompletionEntity> {
        return completionDao.getPendingCompletions(
            userId = userId
        )
    }

    suspend fun isCompleted(
        habitId: String,
        date: String
    ): Boolean {
        return completionDao.isCompleted(
            habitId = habitId,
            date = date
        )
    }

    suspend fun markAsSynced(
        habitId: String,
        date: String
    ) {
        completionDao.updateSyncStatus(
            habitId = habitId,
            date = date,
            status = SyncStatus.SYNCED
        )
    }

    suspend fun deleteCompletion(
        habitId: String,
        date: String
    ) {
        completionDao.deleteCompletion(
            habitId = habitId,
            date = date
        )
    }
}