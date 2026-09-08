package com.example.habitflow.data.local.room.datasource

import com.example.habitflow.data.local.room.dao.CompletionSyncOperationDao
import com.example.habitflow.data.local.room.entity.CompletionSyncOperationEntity
import javax.inject.Inject

class CompletionSyncOperationDataSource @Inject constructor(
    private val dao: CompletionSyncOperationDao
) {

    suspend fun insert(
        operation: CompletionSyncOperationEntity
    ) {
        dao.insert(operation)
    }

    suspend fun getPendingOperations(
        userId: String
    ): List<CompletionSyncOperationEntity> {
        return dao.getPendingOperations(userId)
    }

    suspend fun delete(
        operationId: Long
    ) {
        dao.delete(operationId)
    }


    suspend fun getPendingDeletions(
        userId: String
    ): List<CompletionSyncOperationEntity> {
        return dao.getPendingDeletions(userId)
    }

}