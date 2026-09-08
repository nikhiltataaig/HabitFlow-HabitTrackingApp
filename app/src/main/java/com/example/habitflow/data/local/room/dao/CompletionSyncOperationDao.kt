package com.example.habitflow.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.habitflow.data.local.room.entity.CompletionSyncOperationEntity
import com.example.habitflow.data.local.room.entity.SyncOperation

@Dao
interface CompletionSyncOperationDao {

    @Insert
    suspend fun insert(
        operation: CompletionSyncOperationEntity
    )

    @Query("""
        SELECT * FROM completion_sync_operations
        WHERE userId = :userId
        ORDER BY createdAt ASC
    """)
    suspend fun getPendingOperations(
        userId: String
    ): List<CompletionSyncOperationEntity>

    @Query("""
        DELETE FROM completion_sync_operations
        WHERE id = :operationId
    """)
    suspend fun delete(
        operationId: Long
    )
    @Query("""
    SELECT * FROM completion_sync_operations
    WHERE userId = :userId
    AND operation = :operation
""")
    suspend fun getPendingDeletions(
        userId: String,
        operation: SyncOperation = SyncOperation.DELETE
    ): List<CompletionSyncOperationEntity>
}