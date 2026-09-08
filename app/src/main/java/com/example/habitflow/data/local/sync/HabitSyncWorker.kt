package com.example.habitflow.data.local.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.habitflow.data.local.room.datasource.CompletionSyncOperationDataSource
import com.example.habitflow.data.local.room.datasource.LocalCompletionDataSource
import com.example.habitflow.data.local.room.mapper.toDomain
import com.example.habitflow.data.remote.FirestoreCompletionDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class HabitSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,

    private val localCompletionDataSource: LocalCompletionDataSource,
    private val syncOperationDataSource: CompletionSyncOperationDataSource,
    private val remoteDataSource: FirestoreCompletionDataSource

) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {

        val userId = inputData.getString(KEY_USER_ID)
            ?: return Result.failure()

        return try {

            syncPendingCompletions(userId)

            syncPendingDeletions(userId)

            Result.success()

        } catch (e: Exception) {

            Result.retry()
        }
    }

    private suspend fun syncPendingCompletions(
        userId: String
    ) {

        val pendingCompletions =
            localCompletionDataSource
                .getPendingCompletions(userId)

        for (entity in pendingCompletions) {

            val completion = entity.toDomain()

            val result =
                remoteDataSource.syncCompletion(completion)

            if (result.isFailure) {
                throw result.exceptionOrNull()
                    ?: Exception("Completion sync failed")
            }

            localCompletionDataSource.markAsSynced(
                habitId = completion.habitId,
                date = completion.date
            )
        }
    }

    private suspend fun syncPendingDeletions(
        userId: String
    ) {

        val operations =
            syncOperationDataSource
                .getPendingOperations(userId)

        for (operation in operations) {

            val completionId =
                "${operation.habitId}_${operation.date}"

            val result =
                remoteDataSource.deleteCompletion(
                    userId = operation.userId,
                    completionId = completionId
                )

            if (result.isFailure) {
                throw result.exceptionOrNull()
                    ?: Exception("Completion deletion sync failed")
            }

            syncOperationDataSource.delete(
                operation.id
            )
        }
    }

    companion object {
        const val KEY_USER_ID = "user_id"
    }
}