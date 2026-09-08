package com.example.habitflow.data.local.sync

import com.example.habitflow.data.local.room.datasource.CompletionSyncOperationDataSource
import com.example.habitflow.data.local.room.datasource.LocalCompletionDataSource
import com.example.habitflow.data.local.room.datasource.LocalHabitDataSource
import com.example.habitflow.data.local.room.entity.SyncStatus
import com.example.habitflow.data.local.room.mapper.toEntity
import com.example.habitflow.data.model.toDomain
import com.example.habitflow.data.remote.FirestoreCompletionDataSource
import com.example.habitflow.data.remote.FirestoreHabitDataSource
import javax.inject.Inject


class RemoteToLocalSync @Inject constructor(
    private val remoteHabitDataSource: FirestoreHabitDataSource,
    private val remoteCompletionDataSource: FirestoreCompletionDataSource,
    private val localHabitDataSource: LocalHabitDataSource,
    private val localCompletionDataSource: LocalCompletionDataSource,
    private val completionSyncOperationDataSource : CompletionSyncOperationDataSource
) {

    suspend fun sync(
        userId: String
    ): Result<Unit> {

        return try {

            syncHabits(userId)

            syncCompletions(userId)

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    private suspend fun syncHabits(
        userId: String
    ) {
        val habits = remoteHabitDataSource
            .getHabits(userId)
            .getOrThrow()
            .map { it.toDomain() }

        localHabitDataSource.insertHabits(
            habits.map { it.toEntity() }
        )
    }

    private suspend fun syncCompletions(userId: String) {

        val remoteCompletions =
            remoteCompletionDataSource
                .getAllCompletions(userId)
                .getOrThrow()
                .map { it.toDomain() }

        val pendingCreations =
            localCompletionDataSource
                .getPendingCompletions(userId)

        val pendingDeletions =
            completionSyncOperationDataSource
                .getPendingDeletions(userId)

        val pendingCreationKeys =
            pendingCreations
                .map { "${it.habitId}_${it.date}" }
                .toSet()

        val pendingDeletionKeys =
            pendingDeletions
                .map { "${it.habitId}_${it.date}" }
                .toSet()

        val entities = remoteCompletions
            .filterNot { completion ->

                val key = "${completion.habitId}_${completion.date}"

                key in pendingCreationKeys ||
                        key in pendingDeletionKeys
            }
            .map { completion ->
                completion.toEntity(
                    syncStatus = SyncStatus.SYNCED
                )
            }

        entities.forEach { entity ->
            localCompletionDataSource.insertCompletion(entity)
        }
    }
}