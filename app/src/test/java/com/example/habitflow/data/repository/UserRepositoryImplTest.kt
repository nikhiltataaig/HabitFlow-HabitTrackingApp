package com.example.habitflow.data.repository

import com.example.habitflow.data.model.UserDto
import com.example.habitflow.data.remote.FirestoreUserDataSource
import com.example.habitflow.domain.model.User
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

class UserRepositoryImplTest {

    private lateinit var dataSource: FirestoreUserDataSource
    private lateinit var userRepository: UserRepositoryImpl

    @Before
    fun setUp() {
        dataSource = mockk()
        userRepository = UserRepositoryImpl(dataSource)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `createUser calls datasource with correct dto`() = runTest {
        // Given
        val user = User(id = "user123", name = "Test", email = "test@example.com", createdAt = 0L)
        coEvery { dataSource.createUser(any()) } returns Result.success(Unit)

        // When
        val result = userRepository.createUser(user)

        // Then
        assertTrue(result.isSuccess)
        coVerify { dataSource.createUser(match { it.id == user.id && it.name == user.name }) }
    }

    @Test
    fun `createUser returns failure when datasource fails`() = runTest {
        // Given
        val user = User(id = "user123", name = "Test", email = "test@example.com", createdAt = 0L)
        val exception = Exception("Firestore error")
        coEvery { dataSource.createUser(any()) } returns Result.failure(exception)

        // When
        val result = userRepository.createUser(user)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getUser returns domain user when datasource succeeds`() = runTest {
        // Given
        val userId = "user123"
        val userDto = UserDto(id = userId, name = "Test", email = "test@example.com")
        coEvery { dataSource.getUser(userId) } returns Result.success(userDto)

        // When
        val result = userRepository.getUser(userId)

        // Then
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertEquals(userId, user?.id)
        assertEquals("Test", user?.name)
        coVerify { dataSource.getUser(userId) }
    }

    @Test
    fun `getUser returns failure when datasource fails`() = runTest {
        // Given
        val userId = "user123"
        val exception = Exception("Not found")
        coEvery { dataSource.getUser(userId) } returns Result.failure(exception)

        // When
        val result = userRepository.getUser(userId)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `updateUser calls datasource with correct dto`() = runTest {
        // Given
        val user = User(id = "user123", name = "Updated Name", email = "test@example.com", createdAt = 0L)
        coEvery { dataSource.updateUser(any()) } returns Result.success(Unit)

        // When
        val result = userRepository.updateUser(user)

        // Then
        assertTrue(result.isSuccess)
        coVerify { dataSource.updateUser(match { it.id == user.id && it.name == user.name }) }
    }

    @Test
    fun `deleteUser calls datasource with correct id`() = runTest {
        // Given
        val user = User(id = "user123", name = "Test", email = "test@example.com", createdAt = 0L)
        coEvery { dataSource.deleteUser(user.id) } returns Result.success(Unit)

        // When
        val result = userRepository.deleteUser(user)

        // Then
        assertTrue(result.isSuccess)
        coVerify { dataSource.deleteUser(user.id) }
    }
}
