package com.example.habitflow

import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
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
import com.example.habitflow.data.local.sync.HabitSyncScheduler
import com.example.habitflow.domain.model.User
import com.example.habitflow.domain.repository.AuthRepository
import com.example.habitflow.domain.repository.UserRepository
import com.example.habitflow.ui.AppRoutes
import com.example.habitflow.ui.TestTags
import com.example.habitflow.ui.signup.SignupScreen
import com.example.habitflow.ui.signup.SignupViewModel
import io.mockk.mockk
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class SignupScreenAndroidTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var userRepository: UserRepository
    private lateinit var syncScheduler: HabitSyncScheduler
    private lateinit var signupViewModel: SignupViewModel
    private lateinit var navController: TestNavHostController

    // State for anonymous fakes
    private var signUpResult: Result<User> = Result.failure(Exception())
    private var signUpDelay: Long = 0
    private var createUserResult: Result<Unit> = Result.success(Unit)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        // Use anonymous implementations inside the test file to avoid MockK + Result ClassCastException
        authRepository = object : AuthRepository {
            override suspend fun signUp(name: String, email: String, password: String): Result<User> {
                if (signUpDelay > 0) delay(signUpDelay.milliseconds)
                return signUpResult
            }
            override suspend fun login(email: String, password: String): Result<User> = Result.failure(Exception())
            override fun getCurrentUser(): User? = null
            override fun logout() {}
        }

        userRepository = object : UserRepository {
            override suspend fun createUser(user: User): Result<Unit> = createUserResult
            override suspend fun getUser(userId: String): Result<User> = Result.failure(Exception())
            override suspend fun updateUser(user: User): Result<Unit> = Result.success(Unit)
            override suspend fun deleteUser(user: User): Result<Unit> = Result.success(Unit)
        }

        syncScheduler = mockk(relaxed = true)
        
        signupViewModel = SignupViewModel(
            authRepository = authRepository,
            userRepository = userRepository,
            syncScheduler = syncScheduler
        )

        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(
                navController = navController,
                startDestination = AppRoutes.SignupRoute
            ) {
                composable<AppRoutes.SignupRoute> {
                    SignupScreen(
                        navController = navController,
                        signupViewModel = signupViewModel
                    )
                }
                composable<AppRoutes.HomeRoute> {
                    Text("Home Screen")
                }
                composable<AppRoutes.LoginRoute> {
                    Text("Login Screen")
                }
            }
        }
    }

    @Test
    fun signup_nameField_updatesState() {
        composeTestRule.onNodeWithTag(TestTags.NAME_TEXT_FIELD).performTextInput("John Doe")
        composeTestRule.waitForIdle()
        assertEquals("John Doe", signupViewModel.uiState.value.name)
    }

    @Test
    fun signup_emailField_updatesState() {
        composeTestRule.onNodeWithTag(TestTags.EMAIL_TEXT_FIELD).performTextInput("test@example.com")
        composeTestRule.waitForIdle()
        assertEquals("test@example.com", signupViewModel.uiState.value.email)
    }

    @Test
    fun signup_passwordFields_updateState() {
        composeTestRule.onNodeWithTag(TestTags.PASSWORD_TEXT_FIELD).performTextInput("password123")
        composeTestRule.onNodeWithTag(TestTags.CONFIRM_PASSWORD_TEXT_FIELD).performTextInput("password123")
        composeTestRule.waitForIdle()
        assertEquals("password123", signupViewModel.uiState.value.password)
        assertEquals("password123", signupViewModel.uiState.value.confirmPassword)
    }

    @Test
    fun signup_validationError_showsErrorMessage() {
        // Try to signup with empty fields
        composeTestRule.onNodeWithTag(TestTags.SIGNUP_BUTTON).performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithTag(TestTags.ERROR_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithText("Email is Required").assertIsDisplayed()
    }

    @Test
    fun signup_loadingState_disablesInputs() {
        signUpDelay = 5000
        signUpResult = Result.success(User("1", "John", "john@test.com", 0L))

        composeTestRule.onNodeWithTag(TestTags.NAME_TEXT_FIELD).performTextInput("John")
        composeTestRule.onNodeWithTag(TestTags.EMAIL_TEXT_FIELD).performTextInput("john@test.com")
        composeTestRule.onNodeWithTag(TestTags.PASSWORD_TEXT_FIELD).performTextInput("password123")
        composeTestRule.onNodeWithTag(TestTags.CONFIRM_PASSWORD_TEXT_FIELD).performTextInput("password123")
        
        composeTestRule.onNodeWithTag(TestTags.SIGNUP_BUTTON).performClick()
        
        // During delay, inputs should be disabled
        composeTestRule.onNodeWithTag(TestTags.NAME_TEXT_FIELD).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(TestTags.EMAIL_TEXT_FIELD).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(TestTags.SIGNUP_BUTTON).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(TestTags.LOADER).assertIsDisplayed()
    }

    @Test
    fun signup_success_navigatesToHome() {
        val user = User("1", "John", "john@test.com", 0L)
        signUpResult = Result.success(user)
        createUserResult = Result.success(Unit)

        composeTestRule.onNodeWithTag(TestTags.NAME_TEXT_FIELD).performTextInput("John")
        composeTestRule.onNodeWithTag(TestTags.EMAIL_TEXT_FIELD).performTextInput("john@test.com")
        composeTestRule.onNodeWithTag(TestTags.PASSWORD_TEXT_FIELD).performTextInput("password123")
        composeTestRule.onNodeWithTag(TestTags.CONFIRM_PASSWORD_TEXT_FIELD).performTextInput("password123")
        
        composeTestRule.onNodeWithTag(TestTags.SIGNUP_BUTTON).performClick()
        
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Home Screen").assertIsDisplayed()
    }

    @Test
    fun loginClicked_navigatesToLogin() {
        composeTestRule.onNodeWithTag(TestTags.LOGIN_TEXT_BUTTON).performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Login Screen").assertIsDisplayed()
    }
}
