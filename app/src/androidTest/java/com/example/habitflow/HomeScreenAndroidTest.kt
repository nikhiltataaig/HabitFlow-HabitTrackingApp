package com.example.habitflow

import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.habitflow.domain.model.Habit
import com.example.habitflow.domain.model.HabitCompletion
import com.example.habitflow.domain.model.HabitFrequency
import com.example.habitflow.domain.model.User
import com.example.habitflow.domain.repository.AuthRepository
import com.example.habitflow.domain.repository.CompletionRepository
import com.example.habitflow.domain.repository.HabitRepository
import com.example.habitflow.ui.AppRoutes
import com.example.habitflow.ui.TestTags
import com.example.habitflow.ui.home.HomeScreen
import com.example.habitflow.ui.home.HomeViewModel
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class HomeScreenAndroidTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var habitRepository: HabitRepository
    private lateinit var completionRepository: CompletionRepository
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var navController: TestNavHostController

    private val user = User("u1", "John", "john@test.com", 0L)
    
    private val habit1 = Habit(
        id = "h1", userId = "u1", name = "Habit 1", description = "Desc 1",
        frequency = HabitFrequency.DAILY, targetDays = emptyList(), isActive = true,
        createdAt = 0L, updatedAt = 0L
    )
    private val habit2 = Habit(
        id = "h2", userId = "u1", name = "Habit 2", description = "Desc 2",
        frequency = HabitFrequency.DAILY, targetDays = emptyList(), isActive = true,
        createdAt = 0L, updatedAt = 0L
    )

    // State for anonymous fakes
    private val completions = mutableListOf<HabitCompletion>()

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        authRepository = object : AuthRepository {
            override suspend fun signUp(name: String, email: String, password: String) = Result.failure<User>(Exception())
            override suspend fun login(email: String, password: String) = Result.failure<User>(Exception())
            override fun getCurrentUser(): User? = user
            override fun logout() {}
        }

        habitRepository = object : HabitRepository {
            override suspend fun createHabit(habit: Habit) = Result.success(Unit)
            override suspend fun getHabit(userId: String, habitId: String) = Result.success(habit1)
            override suspend fun getHabits(userId: String) = Result.success(listOf(habit1, habit2))
            override suspend fun updateHabit(habit: Habit) = Result.success(Unit)
            override suspend fun deleteHabit(userId: String, habitId: String) = Result.success(Unit)
        }

        completionRepository = object : CompletionRepository {
            override suspend fun createCompletion(completion: HabitCompletion): Result<Unit> {
                completions.add(completion)
                return Result.success(Unit)
            }
            override suspend fun getCompletion(userId: String, completionId: String) = Result.failure<HabitCompletion>(Exception())
            override suspend fun getCompletionsForHabit(userId: String, habitId: String) = Result.success(completions.filter { it.habitId == habitId })
            override suspend fun getAllCompletions(userId: String) = Result.success(completions.toList())
            override suspend fun isHabitCompletedToday(habitId: String, date: String) = Result.success(completions.any { it.habitId == habitId && it.date == date })
            override suspend fun deleteCompletion(userId: String, habitId: String, date: String): Result<Unit> {
                completions.removeAll { it.habitId == habitId && it.date == date }
                return Result.success(Unit)
            }
        }
        
        homeViewModel = HomeViewModel(
            authRepository = authRepository,
            habitRepository = habitRepository,
            completionRepository = completionRepository
        )

        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(
                navController = navController,
                startDestination = AppRoutes.HomeRoute
            ) {
                composable<AppRoutes.HomeRoute> {
                    HomeScreen(
                        navController = navController,
                        homeViewModel = homeViewModel
                    )
                }
                composable<AppRoutes.HabitDetailRoute> {
                    Text("Detail Screen")
                }
            }
        }
    }

    @Test
    fun home_displaysHabitList() {
        composeTestRule.onNodeWithText("Habit 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Habit 2").assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.COMPLETED_COUNT).assertIsDisplayed()
        composeTestRule.onNodeWithText("Completed today: 0").assertIsDisplayed()
    }

    @Test
    fun home_habitToggle_updatesStateAndCount() {
        // Initially off
        composeTestRule.onNodeWithTag("${TestTags.HABIT_TOGGLE_PREFIX}h1").assertIsOff()
        
        // Toggle on
        composeTestRule.onNodeWithTag("${TestTags.HABIT_TOGGLE_PREFIX}h1").performClick()
        composeTestRule.waitForIdle()
        
        // Verify state and count
        composeTestRule.onNodeWithTag("${TestTags.HABIT_TOGGLE_PREFIX}h1").assertIsOn()
        composeTestRule.onNodeWithText("Completed today: 1").assertIsDisplayed()
        
        // Toggle off
        composeTestRule.onNodeWithTag("${TestTags.HABIT_TOGGLE_PREFIX}h1").performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithTag("${TestTags.HABIT_TOGGLE_PREFIX}h1").assertIsOff()
        composeTestRule.onNodeWithText("Completed today: 0").assertIsDisplayed()
    }

    @Test
    fun home_habitClick_navigatesToDetails() {
        composeTestRule.onNodeWithTag("${TestTags.HABIT_ITEM_PREFIX}h1").performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Detail Screen").assertIsDisplayed()
    }
}
