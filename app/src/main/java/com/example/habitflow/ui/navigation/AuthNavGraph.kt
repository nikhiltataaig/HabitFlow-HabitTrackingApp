package com.example.habitflow.ui.navigation



import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.example.habitflow.ui.AppRoutes
import com.example.habitflow.ui.dashboard.DashboardScreen
import com.example.habitflow.ui.dashboard.DashboardViewModel
import com.example.habitflow.ui.habitDetail.HabitDetailScreen
import com.example.habitflow.ui.habitDetail.HabitDetailViewModel
import com.example.habitflow.ui.home.HomeViewModel
import com.example.habitflow.ui.login.LoginScreen
import com.example.habitflow.ui.login.LoginViewModel
import com.example.habitflow.ui.signup.SignupScreen
import com.example.habitflow.ui.signup.SignupViewModel

fun NavGraphBuilder.authNavGraph(
    navController: NavHostController
) {
    navigation<AppRoutes.AuthGraph>(
        startDestination = AppRoutes.LoginRoute
    ) {

        composable<AppRoutes.LoginRoute> {

            val viewModel: LoginViewModel = hiltViewModel()

            LoginScreen(
                navController = navController,
                loginViewModel = viewModel
            )
        }

        composable<AppRoutes.SignupRoute> {

            val viewModel: SignupViewModel = hiltViewModel()

            SignupScreen(
                navController = navController,
                signupViewModel = viewModel
            )
        }
    }
}