package com.example.habitflow

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.habitflow.domain.model.Habit
import com.example.habitflow.domain.model.HabitCompletion
import com.example.habitflow.domain.model.HabitFrequency
import com.example.habitflow.domain.model.User
import com.example.habitflow.domain.repository.AuthRepository
import com.example.habitflow.domain.repository.CompletionRepository
import com.example.habitflow.domain.repository.HabitRepository
import com.example.habitflow.ui.TestTags
import com.example.habitflow.ui.analysis.AnalysisScreen
import com.example.habitflow.ui.analysis.AnalysisViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class AnalysisScreenAndroidTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var habitRepository: HabitRepository
    private lateinit var completionRepository: CompletionRepository
    private lateinit var viewModel: AnalysisViewModel
    private lateinit var navController: TestNavHostController

    private val user = User("u1", "John", "john@test.com", 0L)
    
    private val habit1 = Habit(
        id = "h1", userId = "u1", name = "Reading", description = "Books",
        frequency = HabitFrequency.DAILY, targetDays = emptyList(), isActive = true,
        createdAt = 0L, updatedAt = 0L
    )

    // Results for fakes
    private var habitsResult: Result<List<Habit>> = Result.success(listOf(habit1))
    private var completionsResult: Result<List<HabitCompletion>> = Result.success(emptyList())

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
            override suspend fun getHabits(userId: String) = habitsResult
            override suspend fun updateHabit(habit: Habit) = Result.success(Unit)
            override suspend fun deleteHabit(userId: String, habitId: String) = Result.success(Unit)
        }

        completionRepository = object : CompletionRepository {
            override suspend fun createCompletion(completion: HabitCompletion) = Result.success(Unit)
            override suspend fun getCompletion(userId: String, completionId: String) = Result.failure<HabitCompletion>(Exception())
            override suspend fun getCompletionsForHabit(userId: String, habitId: String) = Result.success(emptyList<HabitCompletion>())
            override suspend fun getAllCompletions(userId: String) = completionsResult
            override suspend fun isHabitCompletedToday(habitId: String, date: String) = Result.success(false)
            override suspend fun deleteCompletion(userId: String, habitId: String, date: String) = Result.success(Unit)
        }

        viewModel = AnalysisViewModel(
            habitRepository = habitRepository,
            completionRepository = completionRepository,
            authRepository = authRepository
        )

        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            AnalysisScreen(analysisViewModel = viewModel)
        }
    }

    @Test
    fun analysis_displaysOverviewStats() {
        val activeHabitsTag = "${TestTags.ANALYSIS_SUMMARY_CARD_PREFIX}Active Habits"
        composeTestRule.onNodeWithTag(activeHabitsTag).assertIsDisplayed()
        
        // Check for the value '1' specifically inside the Active Habits card
        composeTestRule.onNode(hasParent(hasTestTag(activeHabitsTag)) and hasText("1")).assertIsDisplayed()
        
        composeTestRule.onNodeWithTag("${TestTags.ANALYSIS_SUMMARY_CARD_PREFIX}Completed Today").assertIsDisplayed()
        composeTestRule.onNodeWithTag("${TestTags.ANALYSIS_SUMMARY_CARD_PREFIX}Completion Rate").assertIsDisplayed()
    }

    @Test
    fun analysis_displaysHabitPerformanceCards() {
        composeTestRule.onNodeWithTag("${TestTags.HABIT_ANALYSIS_CARD_PREFIX}h1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reading").assertIsDisplayed()
        composeTestRule.onNodeWithText("Current streak").assertIsDisplayed()
    }

    @Test
    fun analysis_emptyState_showsMessage() {
        // Set habits to empty and reload
        habitsResult = Result.success(emptyList())
        composeTestRule.runOnUiThread {
            viewModel.onEvent(com.example.habitflow.ui.analysis.AnalysisScreenEvents.OnRefresh)
        }
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithTag("EMPTY_ANALYSIS_TEXT").assertIsDisplayed()
    }

    @Test
    fun analysis_pullToRefresh_triggersReload() {
        // Just verify it doesn't crash and the loader appears briefly
        composeTestRule.onNodeWithTag("${TestTags.ANALYSIS_SUMMARY_CARD_PREFIX}Active Habits").performTouchInput {
            swipeDown()
        }
        composeTestRule.waitForIdle()
        
        // After idle it should be back to displaying data
        composeTestRule.onNodeWithText("Reading").assertIsDisplayed()
    }
}
