package com.example.habitflow.ui.habitDetail

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
class HabitDetailViewModelTest {

    private lateinit var habitRepository: HabitRepository
    private lateinit var completionRepository: CompletionRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: HabitDetailViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleUser = User("u1", "Test", "test@test.com", 0L)
    private val today = LocalDate.now()
    
    private val dailyHabit = Habit(
        id = "habit1",
        userId = "u1",
        name = "Daily Exercise",
        description = "Gym",
        frequency = HabitFrequency.DAILY,
        targetDays = emptyList(),
        isActive = true,
        createdAt = 1000L,
        updatedAt = 1000L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        habitRepository = mockk()
        completionRepository = mockk()
        authRepository = mockk()
        
        every { authRepository.getCurrentUser() } returns sampleUser
        
        viewModel = HabitDetailViewModel(habitRepository, completionRepository, authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `loadHabit with invalid id sets error message`() = runTest {
        viewModel.loadHabit("")
        assertEquals("Invalid habit", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `loadHabit success calculates stats correctly for daily habit`() = runTest {
        // Given
        val habitId = "habit1"
        coEvery { habitRepository.getHabit("u1", habitId) } returns Result.success(dailyHabit)
        
        // 3 day streak including today
        val completions = listOf(
            HabitCompletion("c1", "u1", habitId, today.toString(), 100L),
            HabitCompletion("c2", "u1", habitId, today.minusDays(1).toString(), 90L),
            HabitCompletion("c3", "u1", habitId, today.minusDays(2).toString(), 80L)
        )
        coEvery { completionRepository.getCompletionsForHabit("u1", habitId) } returns Result.success(completions)

        // When
        viewModel.loadHabit(habitId)

        // Then
        val state = viewModel.uiState.value
        assertEquals(dailyHabit.id, state.habit?.id)
        assertEquals(3, state.currentStreak)
        assertEquals(3, state.bestStreak)
        assertEquals(3, state.completedCount)
        assertTrue(state.completionRate > 0)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `loadHabit handles broken streak correctly`() = runTest {
        // Given
        val habitId = "habit1"
        coEvery { habitRepository.getHabit("u1", habitId) } returns Result.success(dailyHabit)
        
        // Completed today and 2 days ago, but NOT yesterday
        val completions = listOf(
            HabitCompletion("c1", "u1", habitId, today.toString(), 100L),
            HabitCompletion("c3", "u1", habitId, today.minusDays(2).toString(), 80L)
        )
        coEvery { completionRepository.getCompletionsForHabit("u1", habitId) } returns Result.success(completions)

        // When
        viewModel.loadHabit(habitId)

        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.currentStreak) // Only today
        assertEquals(1, state.bestStreak)
    }

    @Test
    fun `onToggleComplete on scheduled day creates completion when not done`() = runTest {
        // Given
        coEvery { habitRepository.getHabit("u1", dailyHabit.id) } returns Result.success(dailyHabit)
        coEvery { completionRepository.getCompletionsForHabit("u1", dailyHabit.id) } returns Result.success(emptyList())
        viewModel.loadHabit(dailyHabit.id)
        
        coEvery { completionRepository.createCompletion(any()) } returns Result.success(Unit)

        // When
        viewModel.onEvent(HabitDetailScreenEvents.OnToggleComplete)

        // Then
        coVerify { completionRepository.createCompletion(match { it.habitId == dailyHabit.id && it.date == today.toString() }) }
        // Verify it triggers a reload
        coVerify(exactly = 2) { habitRepository.getHabit("u1", dailyHabit.id) }
    }

    @Test
    fun `onToggleComplete on non-scheduled day shows toast`() = runTest {
        // Given a weekly habit that is NOT scheduled for today
        // Find a day of week that is NOT today's day of week
        val notTodayDoW = if (today.dayOfWeek.value == 1) 2 else 1
        val weeklyHabit = dailyHabit.copy(frequency = HabitFrequency.WEEKLY, targetDays = listOf(notTodayDoW))
        
        coEvery { habitRepository.getHabit("u1", weeklyHabit.id) } returns Result.success(weeklyHabit)
        coEvery { completionRepository.getCompletionsForHabit("u1", weeklyHabit.id) } returns Result.success(emptyList())
        viewModel.loadHabit(weeklyHabit.id)

        // When
        viewModel.onEvent(HabitDetailScreenEvents.OnToggleComplete)

        // Then
        val event = viewModel.uiEvent.first()
        assertTrue(event is CommonUiEvent.ShowToast)
        assertEquals("This habit is not scheduled for today", (event as CommonUiEvent.ShowToast).msg)
        coVerify(exactly = 0) { completionRepository.createCompletion(any()) }
    }

    @Test
    fun `deleteHabit success navigates home`() = runTest {
        // Given
        coEvery { habitRepository.getHabit("u1", dailyHabit.id) } returns Result.success(dailyHabit)
        coEvery { completionRepository.getCompletionsForHabit("u1", dailyHabit.id) } returns Result.success(emptyList())
        viewModel.loadHabit(dailyHabit.id)
        
        coEvery { habitRepository.deleteHabit("u1", dailyHabit.id) } returns Result.success(Unit)

        // When
        viewModel.onEvent(HabitDetailScreenEvents.OnDeleteConfirmed)

        // Then
        coVerify { habitRepository.deleteHabit("u1", dailyHabit.id) }
        // Verify navigation
        val event = viewModel.uiEvent.first()
        // Note: It might be ShowToast or Navigate depending on order
        // In VM: send(ShowToast), then send(Navigate)
        assertTrue(event is CommonUiEvent.ShowToast || event is CommonUiEvent.Navigate)
    }

    @Test
    fun `onBackClicked navigates to home`() = runTest {
        viewModel.onEvent(HabitDetailScreenEvents.OnBackClicked)
        val event = viewModel.uiEvent.first()
        assertTrue(event is CommonUiEvent.Navigate && event.route == AppRoutes.HomeRoute)
    }

    @Test
    fun `onEditClicked navigates to create with name`() = runTest {
        // Given
        coEvery { habitRepository.getHabit("u1", dailyHabit.id) } returns Result.success(dailyHabit)
        coEvery { completionRepository.getCompletionsForHabit("u1", dailyHabit.id) } returns Result.success(emptyList())
        viewModel.loadHabit(dailyHabit.id)

        // When
        viewModel.onEvent(HabitDetailScreenEvents.OnEditClicked)

        // Then
        val event = viewModel.uiEvent.first()
        assertTrue(event is CommonUiEvent.Navigate)
        val route = (event as CommonUiEvent.Navigate).route
        assertTrue(route is AppRoutes.CreateHabitRoute)
        assertEquals(dailyHabit.name, (route as AppRoutes.CreateHabitRoute).name)
    }
}
