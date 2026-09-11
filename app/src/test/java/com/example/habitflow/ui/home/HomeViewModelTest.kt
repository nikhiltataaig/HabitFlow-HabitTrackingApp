package com.example.habitflow.ui.home

import com.example.habitflow.CommonUiEvent
import com.example.habitflow.domain.model.Habit
import com.example.habitflow.domain.model.HabitCompletion
import com.example.habitflow.domain.model.HabitFrequency
import com.example.habitflow.domain.model.User
import com.example.habitflow.domain.repository.AuthRepository
import com.example.habitflow.domain.repository.CompletionRepository
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var habitRepository: HabitRepository
    private lateinit var completionRepository: CompletionRepository
    private lateinit var viewModel: HomeViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleUser = User(id = "user1", name = "Test", email = "test@example.com", createdAt = 0L)
    private val today = LocalDate.now().toString()
    
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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        habitRepository = mockk()
        completionRepository = mockk()
        
        // Default mocks for init block
        every { authRepository.getCurrentUser() } returns sampleUser
        coEvery { habitRepository.getHabits(any()) } returns Result.success(listOf(sampleHabit))
        coEvery { completionRepository.getAllCompletions(any()) } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    private fun createViewModel() {
        viewModel = HomeViewModel(authRepository, habitRepository, completionRepository)
    }

    @Test
    fun `init loads user habits and completions successfully`() = runTest {
        // Given
        val completion = HabitCompletion(
            id = "c1", userId = "user1", habitId = "habit1", date = today, completedAt = 2000L
        )
        coEvery { completionRepository.getAllCompletions("user1") } returns Result.success(listOf(completion))

        // When
        createViewModel()

        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.habits.size)
        assertEquals(sampleHabit.id, state.habits[0].id)
        assertTrue(state.completedHabitIds.contains("habit1"))
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `init shows error when user not logged in`() = runTest {
        // Given
        every { authRepository.getCurrentUser() } returns null

        // When
        createViewModel()

        // Then
        assertEquals("User not logged in", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `init shows error when habits loading fails`() = runTest {
        // Given
        coEvery { habitRepository.getHabits(any()) } returns Result.failure(Exception("DB error"))

        // When
        createViewModel()

        // Then
        assertEquals("Failed to load habits", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onHabitToggled creates completion when not already completed`() = runTest {
        // Given
        createViewModel()
        coEvery { completionRepository.createCompletion(any()) } returns Result.success(Unit)

        // When
        viewModel.onEvent(HomeScreenEvents.onHabitToggled(sampleHabit))

        // Then
        coVerify { completionRepository.createCompletion(match { it.habitId == sampleHabit.id }) }
        assertTrue(viewModel.uiState.value.completedHabitIds.contains(sampleHabit.id))
    }

    @Test
    fun `onHabitToggled deletes completion when already completed`() = runTest {
        // Given
        val completion = HabitCompletion("c1", "user1", "habit1", today, 2000L)
        coEvery { completionRepository.getAllCompletions("user1") } returns Result.success(listOf(completion))
        createViewModel()
        
        coEvery { completionRepository.deleteCompletion(any(), any(), any()) } returns Result.success(Unit)

        // When
        viewModel.onEvent(HomeScreenEvents.onHabitToggled(sampleHabit))

        // Then
        coVerify { completionRepository.deleteCompletion("user1", sampleHabit.id, today) }
        assertTrue(!viewModel.uiState.value.completedHabitIds.contains(sampleHabit.id))
    }

    @Test
    fun `onLogoutClicked emits navigation event`() = runTest {
        // Given
        createViewModel()

        // When
        viewModel.onEvent(HomeScreenEvents.onLogoutClicked)

        // Then
        val event = viewModel.uiEvent.first()
        assertTrue(event is CommonUiEvent.Navigate)
        assertEquals(AppRoutes.LogoutRoute, (event as CommonUiEvent.Navigate).route)
    }

    @Test
    fun `onHabitClicked emits navigation event with correct habit id`() = runTest {
        // Given
        createViewModel()

        // When
        viewModel.onEvent(HomeScreenEvents.onHabitClicked("habit_xyz"))

        // Then
        val event = viewModel.uiEvent.first()
        assertTrue(event is CommonUiEvent.Navigate)
        val route = (event as CommonUiEvent.Navigate).route
        assertTrue(route is AppRoutes.HabitDetailRoute)
        assertEquals("habit_xyz", (route as AppRoutes.HabitDetailRoute).habitId)
    }
}
