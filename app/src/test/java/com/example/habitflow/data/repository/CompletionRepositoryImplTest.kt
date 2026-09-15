package com.example.habitflow.data.repository

import com.example.habitflow.data.local.room.datasource.CompletionSyncOperationDataSource
import com.example.habitflow.data.local.room.datasource.LocalCompletionDataSource
import com.example.habitflow.data.local.room.entity.HabitCompletionEntity
import com.example.habitflow.data.local.room.entity.SyncOperation
import com.example.habitflow.data.local.room.entity.SyncStatus
import com.example.habitflow.data.local.sync.HabitSyncScheduler
import com.example.habitflow.data.remote.FirestoreCompletionDataSource
import com.example.habitflow.domain.model.HabitCompletion
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CompletionRepositoryImplTest {

    private lateinit var localDataSource: LocalCompletionDataSource
    private lateinit var remoteDataSource: FirestoreCompletionDataSource
    private lateinit var syncOperationDataSource: CompletionSyncOperationDataSource
    private lateinit var syncScheduler: HabitSyncScheduler
    private lateinit var repository: CompletionRepositoryImpl

    private val sampleCompletion = HabitCompletion(
        id = "habit1_2026-09-10",
        userId = "user1",
        habitId = "habit1",
        date = "2026-09-10",
        completedAt = 1000L
    )

    private val sampleEntity = HabitCompletionEntity(
        habitId = "habit1",
        userId = "user1",
        date = "2026-09-10",
        completedAt = 1000L,
        syncStatus = SyncStatus.SYNCED
    )

    @Before
    fun setUp() {
        localDataSource = mockk()
        remoteDataSource = mockk()
        syncOperationDataSource = mockk()
        syncScheduler = mockk()
        repository = CompletionRepositoryImpl(
            localDataSource,
            remoteDataSource,
            syncOperationDataSource,
            syncScheduler
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `createCompletion saves locally as PENDING and schedules sync`() = runTest {
        // Given
        coEvery { localDataSource.insertCompletion(any()) } returns Unit
        coEvery { syncScheduler.schedule(any()) } returns Unit

        // When
        val result = repository.createCompletion(sampleCompletion)

        // Then
        assertTrue(result.isSuccess)
        coVerify { 
            localDataSource.insertCompletion(match { 
                it.habitId == sampleCompletion.habitId && 
                it.syncStatus == SyncStatus.PENDING 
            }) 
        }
        coVerify { syncScheduler.schedule(sampleCompletion.userId) }
    }

    @Test
    fun `deleteCompletion deletes locally, records sync operation and schedules sync`() = runTest {
        // Given
        coEvery { localDataSource.deleteCompletion(any(), any()) } returns Unit
        coEvery { syncOperationDataSource.insert(any()) } returns 1L
        coEvery { syncScheduler.schedule(any()) } returns Unit

        // When
        val result = repository.deleteCompletion(
            habitId = "habit1",
            date = "2026-09-10",
            userId = "user1"
        )

        // Then
        assertTrue(result.isSuccess)
        coVerify { localDataSource.deleteCompletion("habit1", "2026-09-10") }
        coVerify { 
            syncOperationDataSource.insert(match { 
                it.habitId == "habit1" && 
                it.operation == SyncOperation.DELETE 
            }) 
        }
        coVerify { syncScheduler.schedule("user1") }
    }

    @Test
    fun `getAllCompletions returns domain objects from local source`() = runTest {
        // Given
        coEvery { localDataSource.getAllCompletions("user1") } returns listOf(sampleEntity)

        // When
        val result = repository.getAllCompletions("user1")

        // Then
        assertTrue(result.isSuccess)
        val completions = result.getOrNull()
        assertEquals(1, completions?.size)
        assertEquals(sampleCompletion.habitId, completions?.first()?.habitId)
    }

    @Test
    fun `isHabitCompletedToday returns result from local source`() = runTest {
        // Given
        coEvery { localDataSource.isCompleted("habit1", "2026-09-10") } returns true

        // When
        val result = repository.isHabitCompletedToday("habit1", "2026-09-10")

        // Then
        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
    }

    @Test
    fun `getCompletionsForHabit returns domain list`() = runTest {
        // Given
        coEvery { localDataSource.getCompletionsForHabit("user1", "habit1") } returns listOf(sampleEntity)

        // When
        val result = repository.getCompletionsForHabit("user1", "habit1")

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        coVerify { localDataSource.getCompletionsForHabit("user1", "habit1") }
    }
}
