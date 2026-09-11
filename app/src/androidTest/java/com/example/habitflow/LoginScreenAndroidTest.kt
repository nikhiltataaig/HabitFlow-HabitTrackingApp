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
import com.example.habitflow.ui.AppRoutes
import com.example.habitflow.ui.TestTags
import com.example.habitflow.ui.login.LoginScreen
import com.example.habitflow.ui.login.LoginViewModel
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
class LoginScreenAndroidTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var syncScheduler: HabitSyncScheduler
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var navController: TestNavHostController

    // State for anonymous fake
    private var loginResult: Result<User> = Result.failure(Exception())
    private var loginDelay: Long = 0

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        authRepository = object : AuthRepository {
            override suspend fun login(email: String, password: String): Result<User> {
                if (loginDelay > 0) delay(loginDelay.milliseconds)
                return loginResult
            }
            override suspend fun signUp(name: String, email: String, password: String): Result<User> = Result.failure(Exception())
            override fun getCurrentUser(): User? = null
            override fun logout() {}
        }

        syncScheduler = mockk(relaxed = true)
        loginViewModel = LoginViewModel(
            authRepository = authRepository,
            syncScheduler = syncScheduler
        )

        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(
                navController = navController,
                startDestination = AppRoutes.LoginRoute
            ) {
                composable<AppRoutes.LoginRoute> {
                    LoginScreen(
                        navController = navController,
                        loginViewModel = loginViewModel
                    )
                }
                composable<AppRoutes.HomeRoute> {
                    Text("Home Screen")
                }
                composable<AppRoutes.SignupRoute> {
                    Text("Signup Screen")
                }
            }
        }
    }

    @Test
    fun login_emailField_updatesState() {
        composeTestRule.onNodeWithTag(TestTags.EMAIL_TEXT_FIELD).performTextInput("test@example.com")
        composeTestRule.waitForIdle()
        assertEquals("test@example.com", loginViewModel.uiState.value.email)
    }

    @Test
    fun login_passwordField_updatesState() {
        composeTestRule.onNodeWithTag(TestTags.PASSWORD_TEXT_FIELD).performTextInput("password123")
        composeTestRule.waitForIdle()
        assertEquals("password123", loginViewModel.uiState.value.password)
    }

    @Test
    fun login_emptyFields_showsErrorMessage() {
        composeTestRule.onNodeWithTag(TestTags.LOGIN_BUTTON).performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithTag(TestTags.ERROR_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithText("Email and password cannot be empty").assertIsDisplayed()
    }

    @Test
    fun login_loadingState_disablesInputs() {
        loginDelay = 5000
        loginResult = Result.success(User("1", "John", "john@test.com", 0L))

        composeTestRule.onNodeWithTag(TestTags.EMAIL_TEXT_FIELD).performTextInput("john@test.com")
        composeTestRule.onNodeWithTag(TestTags.PASSWORD_TEXT_FIELD).performTextInput("password123")
        
        composeTestRule.onNodeWithTag(TestTags.LOGIN_BUTTON).performClick()
        
        // During delay, inputs should be disabled
        composeTestRule.onNodeWithTag(TestTags.EMAIL_TEXT_FIELD).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(TestTags.PASSWORD_TEXT_FIELD).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(TestTags.LOGIN_BUTTON).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(TestTags.LOADER).assertIsDisplayed()
    }

    @Test
    fun login_success_navigatesToHome() {
        val user = User("1", "John", "john@test.com", 0L)
        loginResult = Result.success(user)

        composeTestRule.onNodeWithTag(TestTags.EMAIL_TEXT_FIELD).performTextInput("john@test.com")
        composeTestRule.onNodeWithTag(TestTags.PASSWORD_TEXT_FIELD).performTextInput("password123")
        
        composeTestRule.onNodeWithTag(TestTags.LOGIN_BUTTON).performClick()
        
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Home Screen").assertIsDisplayed()
    }

    @Test
    fun login_failure_showsErrorMessage() {
        loginResult = Result.failure(Exception("Login Failed"))

        composeTestRule.onNodeWithTag(TestTags.EMAIL_TEXT_FIELD).performTextInput("john@test.com")
        composeTestRule.onNodeWithTag(TestTags.PASSWORD_TEXT_FIELD).performTextInput("wrong")
        
        composeTestRule.onNodeWithTag(TestTags.LOGIN_BUTTON).performClick()
        
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithTag(TestTags.ERROR_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithText("Login Failed").assertIsDisplayed()
    }

    @Test
    fun signupClicked_navigatesToSignup() {
        composeTestRule.onNodeWithTag(TestTags.SIGNUP_TEXT_BUTTON).performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Signup Screen").assertIsDisplayed()
    }
}
