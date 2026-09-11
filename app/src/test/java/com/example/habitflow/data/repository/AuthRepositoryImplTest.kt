package com.example.habitflow.data.repository

import com.example.habitflow.data.model.UserDto
import com.example.habitflow.data.remote.FirebaseAuthDataSource
import com.example.habitflow.data.remote.FirestoreUserDataSource
import com.google.firebase.auth.FirebaseUser
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private lateinit var authDataSource: FirebaseAuthDataSource
    private lateinit var userDataSource: FirestoreUserDataSource
    private lateinit var authRepository: AuthRepositoryImpl

    @Before
    fun setUp() {
        authDataSource = mockk()
        userDataSource = mockk()
        authRepository = AuthRepositoryImpl(authDataSource, userDataSource)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `signUp returns success when both auth and firestore succeed`() = runTest {
        // Given
        val name = "Test User"
        val email = "test@example.com"
        val password = "password123"
        val uid = "user123"
        
        val firebaseUser = mockk<FirebaseUser> {
            every { this@mockk.uid } returns uid
        }
        
        coEvery { authDataSource.signUp(email, password) } returns Result.success(firebaseUser)
        coEvery { userDataSource.createUser(any()) } returns Result.success(Unit)

        // When
        val result = authRepository.signUp(name, email, password)

        // Then
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertEquals(uid, user?.id)
        assertEquals(name, user?.name)
        assertEquals(email, user?.email)
        
        coVerify { authDataSource.signUp(email, password) }
        coVerify { userDataSource.createUser(match { it.id == uid && it.name == name }) }
    }

    @Test
    fun `signUp returns failure when auth fails`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val exception = Exception("Auth failed")
        
        coEvery { authDataSource.signUp(email, password) } returns Result.failure(exception)

        // When
        val result = authRepository.signUp("Name", email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        
        coVerify { authDataSource.signUp(email, password) }
        coVerify(exactly = 0) { userDataSource.createUser(any()) }
    }

    @Test
    fun `signUp returns failure when firestore fails`() = runTest {
        // Given
        val name = "Test User"
        val email = "test@example.com"
        val password = "password123"
        val uid = "user123"
        val exception = Exception("Firestore failed")
        
        val firebaseUser = mockk<FirebaseUser> {
            every { this@mockk.uid } returns uid
        }
        
        coEvery { authDataSource.signUp(email, password) } returns Result.success(firebaseUser)
        coEvery { userDataSource.createUser(any()) } returns Result.failure(exception)

        // When
        val result = authRepository.signUp(name, email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        
        coVerify { authDataSource.signUp(email, password) }
        coVerify { userDataSource.createUser(any()) }
    }

    @Test
    fun `login returns success when both auth and firestore succeed`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val uid = "user123"
        
        val firebaseUser = mockk<FirebaseUser> {
            every { this@mockk.uid } returns uid
        }
        val userDto = UserDto(id = uid, name = "Test User", email = email)
        
        coEvery { authDataSource.login(email, password) } returns Result.success(firebaseUser)
        coEvery { userDataSource.getUser(uid) } returns Result.success(userDto)

        // When
        val result = authRepository.login(email, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(uid, result.getOrNull()?.id)
        
        coVerify { authDataSource.login(email, password) }
        coVerify { userDataSource.getUser(uid) }
    }

    @Test
    fun `login returns failure when auth fails`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val exception = Exception("Login failed")
        
        coEvery { authDataSource.login(email, password) } returns Result.failure(exception)

        // When
        val result = authRepository.login(email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        
        coVerify { authDataSource.login(email, password) }
        coVerify(exactly = 0) { userDataSource.getUser(any()) }
    }

    @Test
    fun `getCurrentUser returns user when firebase user exists`() {
        // Given
        val uid = "user123"
        val name = "Display Name"
        val email = "test@example.com"
        val firebaseUser = mockk<FirebaseUser> {
            every { this@mockk.uid } returns uid
            every { this@mockk.displayName } returns name
            every { this@mockk.email } returns email
        }
        
        every { authDataSource.getCurrentUser() } returns firebaseUser

        // When
        val user = authRepository.getCurrentUser()

        // Then
        assertEquals(uid, user?.id)
        assertEquals(name, user?.name)
        assertEquals(email, user?.email)
    }

    @Test
    fun `getCurrentUser returns null when no firebase user`() {
        // Given
        every { authDataSource.getCurrentUser() } returns null

        // When
        val user = authRepository.getCurrentUser()

        // Then
        assertTrue(user == null)
    }

    @Test
    fun `logout calls datasource logout`() {
        // Given
        every { authDataSource.logout() } returns Unit

        // When
        authRepository.logout()

        // Then
        verify { authDataSource.logout() }
    }
}
