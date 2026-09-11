package com.example.habitflow

import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
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
import com.example.habitflow.ui.habitDetail.HabitDetailScreen
import com.example.habitflow.ui.habitDetail.HabitDetailViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HabitDetailScreenAndroidTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var habitRepository: HabitRepository
    private lateinit var completionRepository: CompletionRepository
    private lateinit var viewModel: HabitDetailViewModel
    private lateinit var navController: TestNavHostController

    private val user = User("u1", "John", "john@test.com", 0L)
    private val habit = Habit(
        id = "h1", userId = "u1", name = "Test Habit", description = "Test Description",
        frequency = HabitFrequency.DAILY, targetDays = emptyList(), isActive = true,
        createdAt = 0L, updatedAt = 0L
    )

    // State for fakes
    private var getHabitResult: Result<Habit> = Result.success(habit)

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
            override suspend fun getHabit(userId: String, habitId: String) = getHabitResult
            override suspend fun getHabits(userId: String) = Result.success(listOf(habit))
            override suspend fun updateHabit(habit: Habit) = Result.success(Unit)
            override suspend fun deleteHabit(userId: String, habitId: String) = Result.success(Unit)
        }

        completionRepository = object : CompletionRepository {
            override suspend fun createCompletion(completion: HabitCompletion) = Result.success(Unit)
            override suspend fun getCompletion(userId: String, completionId: String) = Result.failure<HabitCompletion>(Exception())
            override suspend fun getCompletionsForHabit(userId: String, habitId: String) = Result.success(emptyList<HabitCompletion>())
            override suspend fun getAllCompletions(userId: String) = Result.success(emptyList<HabitCompletion>())
            override suspend fun isHabitCompletedToday(habitId: String, date: String) = Result.success(false)
            override suspend fun deleteCompletion(userId: String, habitId: String, date: String) = Result.success(Unit)
        }

        viewModel = HabitDetailViewModel(
            habitRepository = habitRepository,
            completionRepository = completionRepository,
            authRepository = authRepository
        )
    }

    private fun setContent() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(
                navController = navController,
                startDestination = "detail/h1"
            ) {
                composable("detail/{habitId}") {
                    HabitDetailScreen(
                        navController = navController,
                        habitId = "h1",
                        habitDetailViewModel = viewModel
                    )
                }
                composable<AppRoutes.HomeRoute> {
                    Text("Home Screen")
                }
            }
        }
    }

    @Test
    fun detail_displaysHabitInfoAndStats() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.HABIT_DETAIL_NAME).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.HABIT_DETAIL_DESC).assertIsDisplayed()
        composeTestRule.onNodeWithTag("${TestTags.STAT_CARD_PREFIX}Current Streak").assertIsDisplayed()
    }

    @Test
    fun detail_deleteHabit_showsDialogAndNavigatesHome() {
        setContent()
        // Open dialog
        composeTestRule.onNodeWithTag(TestTags.DELETE_HABIT_ICON).performClick()
        composeTestRule.onNodeWithText("Delete habit?").assertIsDisplayed()
        
        // Confirm delete
        composeTestRule.onNodeWithTag(TestTags.CONFIRM_DELETE_BUTTON).performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Home Screen").assertIsDisplayed()
    }

    @Test
    fun detail_deleteHabit_cancelDialog() {
        setContent()
        // Open dialog
        composeTestRule.onNodeWithTag(TestTags.DELETE_HABIT_ICON).performClick()
        
        // Cancel delete
        composeTestRule.onNodeWithTag(TestTags.CANCEL_DELETE_BUTTON).performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Delete habit?").assertDoesNotExist()
        composeTestRule.onNodeWithTag(TestTags.HABIT_DETAIL_NAME).assertIsDisplayed()
    }

    @Test
    fun detail_errorState_showsRetry() {
        // Set error state BEFORE setting content so the initial load fails
        getHabitResult = Result.failure(Exception("Load error"))
        
        setContent()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Unable to load habit").assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.RETRY_BUTTON).assertIsDisplayed()
        
        // Fix error and retry
        getHabitResult = Result.success(habit)
        composeTestRule.onNodeWithTag(TestTags.RETRY_BUTTON).performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithTag(TestTags.HABIT_DETAIL_NAME).assertIsDisplayed()
    }
}
