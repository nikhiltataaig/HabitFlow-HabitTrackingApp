package com.example.habitflow.ui.analysis

import com.example.habitflow.domain.model.Habit
import com.example.habitflow.domain.model.HabitCompletion
import com.example.habitflow.domain.model.HabitFrequency
import com.example.habitflow.domain.model.User
import com.example.habitflow.domain.repository.AuthRepository
import com.example.habitflow.domain.repository.CompletionRepository
import com.example.habitflow.domain.repository.HabitRepository
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
class AnalysisViewModelTest {

    private lateinit var habitRepository: HabitRepository
    private lateinit var completionRepository: CompletionRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AnalysisViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleUser = User("u1", "Test", "test@test.com", 0L)
    private val today = LocalDate.now()
    
    private val sampleHabit = Habit(
        id = "habit1",
        userId = "u1",
        name = "Exercise",
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
        
        // Default success mocks for init block
        every { authRepository.getCurrentUser() } returns sampleUser
        coEvery { habitRepository.getHabits("u1") } returns Result.success(listOf(sampleHabit))
        coEvery { completionRepository.getAllCompletions("u1") } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    private fun createViewModel() {
        viewModel = AnalysisViewModel(habitRepository, completionRepository, authRepository)
    }

    @Test
    fun `init loads analytics successfully`() = runTest {
        // Given
        val completion = HabitCompletion("c1", "u1", "habit1", today.toString(), 2000L)
        coEvery { completionRepository.getAllCompletions("u1") } returns Result.success(listOf(completion))

        // When
        createViewModel()

        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.totalHabits)
        assertEquals(1, state.completedToday)
        assertEquals(100, state.completionRate)
        assertEquals(30, state.dailyActivity.size)
        assertEquals(1, state.habitAnalysis.size)
        assertEquals(1, state.habitAnalysis[0].currentStreak)
    }

    @Test
    fun `init user not found shows error`() = runTest {
        // Given
        every { authRepository.getCurrentUser() } returns null

        // When
        createViewModel()

        // Then
        assertEquals("User not found", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `init repository failure shows error`() = runTest {
        // Given
        coEvery { habitRepository.getHabits(any()) } returns Result.failure(Exception("Fail"))

        // When
        createViewModel()

        // Then
        assertEquals("Failed to load analytics", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `calculateDailyActivity correctly counts habits and completions`() = runTest {
        // Given
        // One completion today
        val completion = HabitCompletion("c1", "u1", "habit1", today.toString(), 2000L)
        coEvery { completionRepository.getAllCompletions("u1") } returns Result.success(listOf(completion))

        // When
        createViewModel()

        // Then
        val activity = viewModel.uiState.value.dailyActivity
        val todayActivity = activity.last()
        assertEquals(today, todayActivity.date)
        assertEquals(1, todayActivity.totalHabits)
        assertEquals(1, todayActivity.completedHabits)
        
        val yesterdayActivity = activity[activity.size - 2]
        assertEquals(today.minusDays(1), yesterdayActivity.date)
        assertEquals(1, yesterdayActivity.totalHabits)
        assertEquals(0, yesterdayActivity.completedHabits)
    }

    @Test
    fun `calculateOverallCompletionRate handles mixed data`() = runTest {
        // Given
        // Start date is 2 days ago. Habit is daily. 
        // 3 possible days (today, yesterday, 2 days ago).
        // 2 completions (today, 2 days ago).
        val completions = listOf(
            HabitCompletion("c1", "u1", "habit1", today.toString(), 3000L),
            HabitCompletion("c2", "u1", "habit1", today.minusDays(2).toString(), 1000L)
        )
        coEvery { completionRepository.getAllCompletions("u1") } returns Result.success(completions)

        // When
        createViewModel()

        // Then
        // 2 / 3 = 66%
        assertEquals(66, viewModel.uiState.value.completionRate)
    }

    @Test
    fun `habitAnalysis calculates streaks correctly`() = runTest {
        // Given
        val completions = listOf(
            HabitCompletion("c1", "u1", "habit1", today.toString(), 3000L),
            HabitCompletion("c2", "u1", "habit1", today.minusDays(1).toString(), 2000L),
            HabitCompletion("c3", "u1", "habit1", today.minusDays(3).toString(), 1000L)
        )
        coEvery { completionRepository.getAllCompletions("u1") } returns Result.success(completions)

        // When
        createViewModel()

        // Then
        val analysis = viewModel.uiState.value.habitAnalysis[0]
        assertEquals(2, analysis.currentStreak)
        assertEquals(2, analysis.bestStreak)
    }

    @Test
    fun `onRefresh triggers data reload`() = runTest {
        // Given
        createViewModel()
        coVerify(exactly = 1) { habitRepository.getHabits("u1") }

        // When
        viewModel.onEvent(AnalysisScreenEvents.OnRefresh)

        // Then
        coVerify(exactly = 2) { habitRepository.getHabits("u1") }
    }
}
