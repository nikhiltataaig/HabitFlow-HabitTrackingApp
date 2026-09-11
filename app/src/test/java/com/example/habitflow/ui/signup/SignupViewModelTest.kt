package com.example.habitflow.ui.signup

import com.example.habitflow.CommonUiEvent
import com.example.habitflow.data.local.sync.HabitSyncScheduler
import com.example.habitflow.domain.model.User
import com.example.habitflow.domain.repository.AuthRepository
import com.example.habitflow.domain.repository.UserRepository
import com.example.habitflow.ui.AppRoutes
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignupViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var userRepository: UserRepository
    private lateinit var syncScheduler: HabitSyncScheduler
    private lateinit var viewModel: SignupViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        userRepository = mockk()
        syncScheduler = mockk()
        viewModel = SignupViewModel(authRepository, userRepository, syncScheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `field changes update UI state`() = runTest {
        viewModel.onEvent(SignupScreenEvent.onNameChanged("John"))
        viewModel.onEvent(SignupScreenEvent.onEmailChanged("john@example.com"))
        viewModel.onEvent(SignupScreenEvent.onPasswordChanged("password123"))
        viewModel.onEvent(SignupScreenEvent.onConfirmPasswordChanged("password123"))

        val state = viewModel.uiState.value
        assertEquals("John", state.name)
        assertEquals("john@example.com", state.email)
        assertEquals("password123", state.password)
        assertEquals("password123", state.confirmPassword)
    }

    @Test
    fun `onLoginClicked emits navigation event`() = runTest {
        viewModel.onEvent(SignupScreenEvent.onLoginClicked)
        val event = viewModel.uiEvent.first()
        assertTrue(event is CommonUiEvent.Navigate && event.route == AppRoutes.LoginRoute)
    }

    @Test
    fun `signup with empty email shows error`() = runTest {
        viewModel.onEvent(SignupScreenEvent.onSignupClicked)
        assertEquals("Email is Required", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `signup with empty name shows error`() = runTest {
        viewModel.onEvent(SignupScreenEvent.onEmailChanged("test@test.com"))
        viewModel.onEvent(SignupScreenEvent.onSignupClicked)
        assertEquals("Name is Required", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `signup with short password shows error`() = runTest {
        viewModel.onEvent(SignupScreenEvent.onEmailChanged("test@test.com"))
        viewModel.onEvent(SignupScreenEvent.onNameChanged("Test"))
        viewModel.onEvent(SignupScreenEvent.onPasswordChanged("123"))
        viewModel.onEvent(SignupScreenEvent.onSignupClicked)
        assertEquals("Password should be longer than 6", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `signup success flow`() = runTest {
        // Given
        val email = "test@test.com"
        val name = "Test User"
        val password = "password123"
        val user = User("u1", name, email, 0L)

        viewModel.onEvent(SignupScreenEvent.onEmailChanged(email))
        viewModel.onEvent(SignupScreenEvent.onNameChanged(name))
        viewModel.onEvent(SignupScreenEvent.onPasswordChanged(password))

        coEvery { authRepository.signUp(name, email, password) } returns Result.success(user)
        coEvery { userRepository.createUser(user) } returns Result.success(Unit)
        coEvery { syncScheduler.scheduleInitialSync(user.id) } returns Unit

        // When
        viewModel.onEvent(SignupScreenEvent.onSignupClicked)

        // Then
        coVerify { authRepository.signUp(name, email, password) }
        coVerify { userRepository.createUser(user) }
        coVerify { syncScheduler.scheduleInitialSync(user.id) }
        
        // Check for loader then navigation
        // In UnconfinedTestDispatcher, events are sent and can be collected
        val event = viewModel.uiEvent.first()
        assertEquals(CommonUiEvent.ShowLoader, event)
    }

    @Test
    fun `signup failure in auth shows error`() = runTest {
        // Given
        val email = "test@test.com"
        val name = "Test User"
        val password = "password123"

        viewModel.onEvent(SignupScreenEvent.onEmailChanged(email))
        viewModel.onEvent(SignupScreenEvent.onNameChanged(name))
        viewModel.onEvent(SignupScreenEvent.onPasswordChanged(password))

        coEvery { authRepository.signUp(any(), any(), any()) } returns Result.failure(Exception("Auth failed"))

        // When
        viewModel.onEvent(SignupScreenEvent.onSignupClicked)

        // Then
        assertEquals("Error Creating a User", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `signup failure in user creation shows error`() = runTest {
        // Given
        val email = "test@test.com"
        val name = "Test User"
        val password = "password123"
        val user = User("u1", name, email, 0L)

        viewModel.onEvent(SignupScreenEvent.onEmailChanged(email))
        viewModel.onEvent(SignupScreenEvent.onNameChanged(name))
        viewModel.onEvent(SignupScreenEvent.onPasswordChanged(password))

        coEvery { authRepository.signUp(any(), any(), any()) } returns Result.success(user)
        coEvery { userRepository.createUser(user) } returns Result.failure(Exception("User exists"))

        // When
        viewModel.onEvent(SignupScreenEvent.onSignupClicked)

        // Then
        assertEquals("User already Exist", viewModel.uiState.value.errorMessage)
    }
}
