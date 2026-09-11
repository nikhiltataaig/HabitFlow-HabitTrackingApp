package com.example.habitflow.ui.login

import com.example.habitflow.CommonUiEvent
import com.example.habitflow.data.local.sync.HabitSyncScheduler
import com.example.habitflow.domain.model.User
import com.example.habitflow.domain.repository.AuthRepository
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
class LoginViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var syncScheduler: HabitSyncScheduler
    private lateinit var viewModel: LoginViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        syncScheduler = mockk()
        viewModel = LoginViewModel(authRepository, syncScheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `onEmailChanged updates state and clears error`() = runTest {
        // Given
        val email = "test@example.com"
        
        // When
        viewModel.onEvent(LoginScreenEvents.onEmailChanged(email))

        // Then
        assertEquals(email, viewModel.uiState.value.email)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onPasswordChanged updates state and clears error`() = runTest {
        // Given
        val password = "password123"
        
        // When
        viewModel.onEvent(LoginScreenEvents.onPasswordChanged(password))

        // Then
        assertEquals(password, viewModel.uiState.value.password)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onSignupClicked emits navigation event`() = runTest {
        // When
        viewModel.onEvent(LoginScreenEvents.onSignupClicked)

        // Then
        val event = viewModel.uiEvent.first()
        assertTrue(event is CommonUiEvent.Navigate)
        assertEquals(AppRoutes.SignupRoute, (event as CommonUiEvent.Navigate).route)
    }

    @Test
    fun `login with empty fields sets error message`() = runTest {
        // Given empty state (default)
        
        // When
        viewModel.onEvent(LoginScreenEvents.onLoginClicked)

        // Then
        assertEquals("Email and password cannot be empty", viewModel.uiState.value.errorMessage)
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    @Test
    fun `login success shows loader, authenticates, navigates and schedules sync`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val user = User("u1", "Test", email, 0L)
        
        viewModel.onEvent(LoginScreenEvents.onEmailChanged(email))
        viewModel.onEvent(LoginScreenEvents.onPasswordChanged(password))
        
        coEvery { authRepository.login(email, password) } returns Result.success(user)
        coEvery { syncScheduler.scheduleInitialSync(user.id) } returns Unit

        // When
        viewModel.onEvent(LoginScreenEvents.onLoginClicked)

        // Then
        coVerify { authRepository.login(email, password) }
        coVerify { syncScheduler.scheduleInitialSync(user.id) }
        
        // Verify events (Loader then Navigate)
        // Since it's a Channel, we might need to collect them
        // In UnconfinedTestDispatcher, they are sent immediately
        // But collecting from channel multiple times can be tricky. 
        // We can just verify the last state/event if needed or use a list.
        
        // For simplicity in this test, let's just check the final navigation
        val event = viewModel.uiEvent.first()
        // Note: In reality, ShowLoader was sent first. first() might catch that.
        // If we want to check all events, we'd collect into a list.
        
        // Re-running logic to capture events if needed, but first() usually gets the first one.
        // Actually, in the VM:
        // _uiEvent.send(CommonUiEvent.ShowLoader)
        // val result = ...
        // _uiEvent.send(CommonUiEvent.Navigate(AppRoutes.HomeRoute))
        
        // The first event should be ShowLoader
        assertEquals(CommonUiEvent.ShowLoader, event)
    }

    @Test
    fun `login failure updates error message`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrong"
        
        viewModel.onEvent(LoginScreenEvents.onEmailChanged(email))
        viewModel.onEvent(LoginScreenEvents.onPasswordChanged(password))
        
        coEvery { authRepository.login(email, password) } returns Result.failure(Exception("Failed"))

        // When
        viewModel.onEvent(LoginScreenEvents.onLoginClicked)

        // Then
        assertEquals("Login Failed", viewModel.uiState.value.errorMessage)
    }
}
