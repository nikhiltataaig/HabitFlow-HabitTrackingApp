package com.example.habitflow

import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.habitflow.domain.model.Habit
import com.example.habitflow.domain.model.User
import com.example.habitflow.domain.repository.AuthRepository
import com.example.habitflow.domain.repository.HabitRepository
import com.example.habitflow.ui.AppRoutes
import com.example.habitflow.ui.TestTags
import com.example.habitflow.ui.createHabit.CreateHabitScreen
import com.example.habitflow.ui.createHabit.CreateHabitViewModel
import io.mockk.mockk
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class CreateHabitScreenAndroidTest {

    private lateinit var habitRepository: HabitRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var createHabitViewModel: CreateHabitViewModel
    private lateinit var navController: TestNavHostController

    private val user = User("u1", "John", "john@test.com", 0L)
    
    // State for anonymous fakes
    private var createHabitResult: Result<Unit> = Result.success(Unit)
    private var createDelay: Long = 0

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
            override suspend fun createHabit(habit: Habit): Result<Unit> {
                if (createDelay > 0) delay(createDelay.milliseconds)
                return createHabitResult
            }
            override suspend fun getHabit(userId: String, habitId: String) = Result.failure<Habit>(Exception())
            override suspend fun getHabits(userId: String) = Result.success(emptyList<Habit>())
            override suspend fun updateHabit(habit: Habit) = Result.success(Unit)
            override suspend fun deleteHabit(userId: String, habitId: String) = Result.success(Unit)
        }

        createHabitViewModel = CreateHabitViewModel(
            habitRepository = habitRepository,
            authRepository = authRepository
        )

        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(
                navController = navController,
                startDestination = AppRoutes.CreateHabitRoute()
            ) {
                composable<AppRoutes.CreateHabitRoute> {
                    CreateHabitScreen(
                        navController = navController,
                        createHabitViewModel = createHabitViewModel
                    )
                }
                composable<AppRoutes.HomeRoute> {
                    Text("Home Screen")
                }
            }
        }
    }

    @Test
    fun create_inputFields_updateState() {
        composeTestRule.onNodeWithTag(TestTags.HABIT_NAME_FIELD).performTextInput("Read")
        composeTestRule.onNodeWithTag(TestTags.HABIT_DESC_FIELD).performTextInput("30 mins")
        
        composeTestRule.waitForIdle()
        assertEquals("Read", createHabitViewModel.uiState.value.name)
        assertEquals("30 mins", createHabitViewModel.uiState.value.description)
    }

    @Test
    fun create_frequencySwitch_updatesState() {
        // Daily by default
        composeTestRule.onNodeWithTag(TestTags.FREQ_DAILY_CHIP).assertIsSelected()
        
        // Switch to Weekly
        composeTestRule.onNodeWithTag(TestTags.FREQ_WEEKLY_CHIP).performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithTag(TestTags.FREQ_WEEKLY_CHIP).assertIsSelected()
        composeTestRule.onNodeWithTag(TestTags.FREQ_DAILY_CHIP).assertIsNotSelected()
        
        // Days should be visible now
        composeTestRule.onNodeWithTag("${TestTags.DAY_CHIP_PREFIX}1").assertIsDisplayed()
    }

    @Test
    fun create_daySelection_updatesState() {
        // Set to Weekly first
        composeTestRule.onNodeWithTag(TestTags.FREQ_WEEKLY_CHIP).performClick()
        
        // Select Mon (1) and Wed (3)
        composeTestRule.onNodeWithTag("${TestTags.DAY_CHIP_PREFIX}1").performClick()
        composeTestRule.onNodeWithTag("${TestTags.DAY_CHIP_PREFIX}3").performClick()
        
        composeTestRule.waitForIdle()
        assertTrue(createHabitViewModel.uiState.value.targetDays.contains(1))
        assertTrue(createHabitViewModel.uiState.value.targetDays.contains(3))
        
        // Deselect Mon
        composeTestRule.onNodeWithTag("${TestTags.DAY_CHIP_PREFIX}1").performClick()
        composeTestRule.waitForIdle()
        assertTrue(!createHabitViewModel.uiState.value.targetDays.contains(1))
    }

    @Test
    fun create_validationError_showsErrorMessage() {
        // Empty name
        composeTestRule.onNodeWithTag(TestTags.CREATE_HABIT_BUTTON).performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Habit name cannot be empty").assertIsDisplayed()
    }

    @Test
    fun create_loadingState_disablesInputs() {
        createDelay = 5000
        
        composeTestRule.onNodeWithTag(TestTags.HABIT_NAME_FIELD).performTextInput("Exercise")
        composeTestRule.onNodeWithTag(TestTags.CREATE_HABIT_BUTTON).performClick()
        
        // Check disabled states and loader
        composeTestRule.onNodeWithTag(TestTags.HABIT_NAME_FIELD).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(TestTags.CREATE_HABIT_BUTTON).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(TestTags.LOADER).assertIsDisplayed()
    }

    @Test
    fun create_success_navigatesToHome() {
        composeTestRule.onNodeWithTag(TestTags.HABIT_NAME_FIELD).performTextInput("Exercise")
        composeTestRule.onNodeWithTag(TestTags.CREATE_HABIT_BUTTON).performClick()
        
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Home Screen").assertIsDisplayed()
    }

    @Test
    fun backClicked_navigatesToHome() {
        composeTestRule.onNodeWithTag(TestTags.BACK_BUTTON).performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Home Screen").assertIsDisplayed()
    }
}
