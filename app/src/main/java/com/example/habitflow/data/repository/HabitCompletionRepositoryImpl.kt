package com.example.habitflow.data.repository



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

            syncScheduler.schedule(
                userId = completion.userId
            )

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getCompletion(
        userId: String,
        completionId: String
    ): Result<HabitCompletion> {

        TODO("Not yet implemented")




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
        habitId: String,
        date: String,
        userId: String
    ): Result<Unit> {

        return try {



            localDataSource.deleteCompletion(
                habitId = habitId,
                date = date
            )

            syncOperationDataSource.insert(
                CompletionSyncOperationEntity(
                    userId = userId,
                    habitId = habitId,
                    date = date,
                    operation = SyncOperation.DELETE,
                    createdAt = System.currentTimeMillis()
                )
            )

            syncScheduler.schedule(userId)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}