package com.example.habitflow.ui.createHabit

import com.example.habitflow.CommonUiEvent
import com.example.habitflow.domain.model.HabitFrequency
import com.example.habitflow.domain.model.User
import com.example.habitflow.domain.repository.AuthRepository
import com.example.habitflow.domain.repository.HabitRepository
import com.example.habitflow.ui.AppRoutes
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
class CreateHabitViewModelTest {

    private lateinit var habitRepository: HabitRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: CreateHabitViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        habitRepository = mockk()
        authRepository = mockk()
        viewModel = CreateHabitViewModel(habitRepository, authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `field changes update UI state`() = runTest {
        viewModel.onEvent(CreateHabitScreenEvent.OnNameChanged("Exercise"))
        viewModel.onEvent(CreateHabitScreenEvent.OnDescriptionChanged("Go to gym"))
        
        val state = viewModel.uiState.value
        assertEquals("Exercise", state.name)
        assertEquals("Go to gym", state.description)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `changing frequency to DAILY clears targetDays`() = runTest {
        // Given
        viewModel.onEvent(CreateHabitScreenEvent.OnFrequencyChanged(HabitFrequency.WEEKLY))
        viewModel.onEvent(CreateHabitScreenEvent.OnDaySelected(1))
        assertTrue(viewModel.uiState.value.targetDays.contains(1))

        // When
        viewModel.onEvent(CreateHabitScreenEvent.OnFrequencyChanged(HabitFrequency.DAILY))

        // Then
        assertTrue(viewModel.uiState.value.targetDays.isEmpty())
    }

    @Test
    fun `toggleDay adds and removes days correctly`() = runTest {
        // Add
        viewModel.onEvent(CreateHabitScreenEvent.OnDaySelected(1))
        viewModel.onEvent(CreateHabitScreenEvent.OnDaySelected(3))
        assertEquals(listOf(1, 3), viewModel.uiState.value.targetDays)

        // Remove
        viewModel.onEvent(CreateHabitScreenEvent.OnDaySelected(1))
        assertEquals(listOf(3), viewModel.uiState.value.targetDays)
    }

    @Test
    fun `createHabit with empty name shows error`() = runTest {
        viewModel.onEvent(CreateHabitScreenEvent.OnCreateClicked)
        assertEquals("Habit name cannot be empty", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `createHabit with WEEKLY frequency and no days shows error`() = runTest {
        viewModel.onEvent(CreateHabitScreenEvent.OnNameChanged("Exercise"))
        viewModel.onEvent(CreateHabitScreenEvent.OnFrequencyChanged(HabitFrequency.WEEKLY))
        
        viewModel.onEvent(CreateHabitScreenEvent.OnCreateClicked)
        
        assertEquals("Select at least one day", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `createHabit success flow`() = runTest {
        // Given
        val user = User("u1", "Test", "test@test.com", 0L)
        every { authRepository.getCurrentUser() } returns user
        coEvery { habitRepository.createHabit(any()) } returns Result.success(Unit)

        viewModel.onEvent(CreateHabitScreenEvent.OnNameChanged("Exercise"))
        viewModel.onEvent(CreateHabitScreenEvent.OnDescriptionChanged("Gym"))

        // When
        viewModel.onEvent(CreateHabitScreenEvent.OnCreateClicked)

        // Then
        coVerify { habitRepository.createHabit(match { 
            it.name == "Exercise" && it.userId == user.id 
        }) }
        
        // In UnconfinedTestDispatcher, events are sent immediately
        val event = viewModel.uiEvent.first()
        assertEquals(CommonUiEvent.ShowLoader, event)
        
        // We can check if toast/navigation was sent by collecting more events if needed, 
        // but checking repository call and first event is a strong start.
    }

    @Test
    fun `createHabit failure shows error message`() = runTest {
        // Given
        val user = User("u1", "Test", "test@test.com", 0L)
        every { authRepository.getCurrentUser() } returns user
        coEvery { habitRepository.createHabit(any()) } returns Result.failure(Exception("Error"))

        viewModel.onEvent(CreateHabitScreenEvent.OnNameChanged("Exercise"))

        // When
        viewModel.onEvent(CreateHabitScreenEvent.OnCreateClicked)

        // Then
        assertEquals("Failed to create habit", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onBackClicked navigates to home`() = runTest {
        viewModel.onEvent(CreateHabitScreenEvent.OnBackClicked)
        val event = viewModel.uiEvent.first()
        assertTrue(event is CommonUiEvent.Navigate && event.route == AppRoutes.HomeRoute)
    }
}
