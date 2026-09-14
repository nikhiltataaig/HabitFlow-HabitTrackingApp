package com.example.habitflow.data.repository

import android.util.Log
import com.example.habitflow.data.local.room.datasource.CompletionSyncOperationDataSource
import com.example.habitflow.data.local.room.datasource.LocalCompletionDataSource
import com.example.habitflow.data.local.room.entity.CompletionSyncOperationEntity
import com.example.habitflow.data.local.room.entity.SyncOperation
import com.example.habitflow.data.local.room.entity.SyncStatus
import com.example.habitflow.data.local.room.mapper.toDomain
import com.example.habitflow.data.local.room.mapper.toEntity
import com.example.habitflow.data.local.sync.HabitSyncScheduler
import com.example.habitflow.data.model.toDomain
import com.example.habitflow.data.remote.FirestoreCompletionDataSource
import com.example.habitflow.domain.model.HabitCompletion
import com.example.habitflow.domain.repository.CompletionRepository
import javax.inject.Inject

class CompletionRepositoryImpl @Inject constructor(
    private val localDataSource: LocalCompletionDataSource,
    private val remoteDataSource: FirestoreCompletionDataSource,
    private val syncOperationDataSource: CompletionSyncOperationDataSource,
    private val syncScheduler: HabitSyncScheduler
) : CompletionRepository {

    override suspend fun createCompletion(
        completion: HabitCompletion
    ): Result<Unit> {
        return try {
            localDataSource.insertCompletion(
                completion.toEntity(
                    syncStatus = SyncStatus.PENDING
                )
            )

            // Try remote sync immediately
            val remoteResult = remoteDataSource.createCompletion(completion)
            if (remoteResult.isSuccess) {
                localDataSource.markAsSynced(
                    habitId = completion.habitId,
                    date = completion.date
                )
            } else {
                syncScheduler.schedule(
                    userId = completion.userId
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCompletion(
        userId: String,
        completionId: String
    ): Result<HabitCompletion> {
        return try {
            // completionId is habitId_date
            val parts = completionId.split("_")
            if (parts.size < 2) return Result.failure(Exception("Invalid completion ID"))
            
            val habitId = parts[0]
            val date = parts[1]

            val localCompletion = localDataSource.getCompletion(habitId, date)
            if (localCompletion != null) {
                return Result.success(localCompletion.toDomain())
            }

            val remoteResult = remoteDataSource.getCompletion(userId, completionId)
            val remoteDto = remoteResult.getOrThrow()
            
            Result.success(remoteDto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCompletionsForHabit(
        userId: String,
        habitId: String
    ): Result<List<HabitCompletion>> {
        return try {
            Result.success(
                localDataSource
                    .getCompletionsForHabit(
                        userId,
                        habitId
                    )
                    .map { it.toDomain() }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllCompletions(
        userId: String
    ): Result<List<HabitCompletion>> {
        return try {
            Result.success(
                localDataSource
                    .getAllCompletions(userId)
                    .map { it.toDomain() }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isHabitCompletedToday(
        habitId: String,
        date: String
    ): Result<Boolean> {
        return try {
            Result.success(
                localDataSource.isCompleted(
                    habitId,
                    date
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCompletion(
        userId: String,
        habitId: String,
        date: String
    ): Result<Unit> {
        return try {
            Log.d("Toggle Habit", " Inside deleteCompletion")

            localDataSource.deleteCompletion(
                habitId = habitId,
                date = date
            )

            val operationId = syncOperationDataSource.insert(
                CompletionSyncOperationEntity(
                    userId = userId,
                    habitId = habitId,
                    date = date,
                    operation = SyncOperation.DELETE,
                    createdAt = System.currentTimeMillis()
                )
            )

            // Try remote delete immediately
            val completionId = "${habitId}_$date"
            val remoteResult = remoteDataSource.deleteCompletion(userId, completionId)
            
            if (remoteResult.isSuccess) {
                syncOperationDataSource.delete(operationId)
            } else {
                syncScheduler.schedule(userId)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("Toggle Habit", " Inside deleteCompletion failed")
            Result.failure(e)
        }
    }
}