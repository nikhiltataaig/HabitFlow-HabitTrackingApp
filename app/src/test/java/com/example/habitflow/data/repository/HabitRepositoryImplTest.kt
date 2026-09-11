package com.example.habitflow.data.repository

import com.example.habitflow.data.local.room.datasource.LocalHabitDataSource
import com.example.habitflow.data.local.room.entity.HabitEntity
import com.example.habitflow.data.remote.FirestoreHabitDataSource
import com.example.habitflow.domain.model.Habit
import com.example.habitflow.domain.model.HabitFrequency
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

class HabitRepositoryImplTest {

    private lateinit var localDataSource: LocalHabitDataSource
    private lateinit var remoteDataSource: FirestoreHabitDataSource
    private lateinit var habitRepository: HabitRepositoryImpl

    private val sampleHabit = Habit(
        id = "habit1",
        userId = "user1",
        name = "Exercise",
        description = "Go to gym",
        frequency = HabitFrequency.DAILY,
        targetDays = listOf(1, 2, 3, 4, 5, 6, 7),
        isActive = true,
        createdAt = 1000L,
        updatedAt = 1000L
    )

    private val sampleHabitEntity = HabitEntity(
        id = "habit1",
        userId = "user1",
        name = "Exercise",
        description = "Go to gym",
        frequency = "DAILY",
        targetDays = listOf(1, 2, 3, 4, 5, 6, 7),
        isActive = true,
        createdAt = 1000L,
        updatedAt = 1000L
    )

    @Before
    fun setUp() {
        localDataSource = mockk()
        remoteDataSource = mockk()
        habitRepository = HabitRepositoryImpl(localDataSource, remoteDataSource)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `createHabit saves locally and then remotely`() = runTest {
        // Given
        coEvery { localDataSource.insertHabit(any()) } returns Unit
        coEvery { remoteDataSource.createHabit(any()) } returns Result.success(Unit)

        // When
        val result = habitRepository.createHabit(sampleHabit)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { localDataSource.insertHabit(match { it.id == sampleHabit.id }) }
        coVerify(exactly = 1) { remoteDataSource.createHabit(sampleHabit) }
    }

    @Test
    fun `createHabit returns failure if remote fails`() = runTest {
        // Given
        val exception = Exception("Remote failure")
        coEvery { localDataSource.insertHabit(any()) } returns Unit
        coEvery { remoteDataSource.createHabit(any()) } returns Result.failure(exception)

        // When
        val result = habitRepository.createHabit(sampleHabit)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        
        // Local should still have been called
        coVerify { localDataSource.insertHabit(any()) }
    }

    @Test
    fun `getHabit returns domain habit from local data`() = runTest {
        // Given
        coEvery { localDataSource.getHabit("habit1") } returns sampleHabitEntity

        // When
        val result = habitRepository.getHabit("user1", "habit1")

        // Then
        assertTrue(result.isSuccess)
        assertEquals(sampleHabit.id, result.getOrNull()?.id)
        assertEquals(sampleHabit.name, result.getOrNull()?.name)
    }

    @Test
    fun `getHabit returns failure if not found locally`() = runTest {
        // Given
        coEvery { localDataSource.getHabit("habit1") } returns null

        // When
        val result = habitRepository.getHabit("user1", "habit1")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoSuchElementException)
    }

    @Test
    fun `getHabits returns domain habits from local data`() = runTest {
        // Given
        coEvery { localDataSource.getActiveHabits("user1") } returns listOf(sampleHabitEntity)

        // When
        val result = habitRepository.getHabits("user1")

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals(sampleHabit.id, result.getOrNull()?.get(0)?.id)
    }

    @Test
    fun `updateHabit updates locally and remotely`() = runTest {
        // Given
        coEvery { localDataSource.updateHabit(any()) } returns Unit
        coEvery { remoteDataSource.updateHabit(any()) } returns Result.success(Unit)

        // When
        val result = habitRepository.updateHabit(sampleHabit)

        // Then
        assertTrue(result.isSuccess)
        coVerify { localDataSource.updateHabit(match { it.id == sampleHabit.id }) }
        coVerify { remoteDataSource.updateHabit(sampleHabit) }
    }

    @Test
    fun `deleteHabit deletes locally and remotely`() = runTest {
        // Given
        coEvery { localDataSource.deleteHabit("habit1") } returns Unit
        coEvery { remoteDataSource.deleteHabit("user1", "habit1") } returns Result.success(Unit)

        // When
        val result = habitRepository.deleteHabit("user1", "habit1")

        // Then
        assertTrue(result.isSuccess)
        coVerify { localDataSource.deleteHabit("habit1") }
        coVerify { remoteDataSource.deleteHabit("user1", "habit1") }
    }
}
