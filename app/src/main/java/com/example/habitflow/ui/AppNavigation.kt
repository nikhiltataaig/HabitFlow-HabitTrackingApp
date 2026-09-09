package com.example.habitflow.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.habitflow.ui.analysis.AnalysisScreen
import com.example.habitflow.ui.analysis.AnalysisViewModel
import com.example.habitflow.ui.createHabit.CreateHabitScreen
import com.example.habitflow.ui.createHabit.CreateHabitViewModel
import com.example.habitflow.ui.habitDetail.HabitDetailScreen
import com.example.habitflow.ui.habitDetail.HabitDetailViewModel
import com.example.habitflow.ui.home.HomeScreen
import com.example.habitflow.ui.home.HomeViewModel
import com.example.habitflow.ui.login.LoginScreen
import com.example.habitflow.ui.login.LoginViewModel
import com.example.habitflow.ui.navigation.BottomNavigationBar
import com.example.habitflow.ui.signup.SignupScreen
import com.example.habitflow.ui.signup.SignupViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    val showBottomBar =
        currentRoute?.contains(AppRoutes.HomeRoute::class.simpleName ?: "") == true ||
                currentRoute?.contains(AppRoutes.CreateHabitRoute::class.simpleName ?: "") == true ||
                currentRoute?.contains(AppRoutes.AnalysisRoute::class.simpleName ?: "") == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    navController = navController
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = AppRoutes.LoginRoute,
            modifier = modifier.padding(innerPadding)
        ) {

            composable<AppRoutes.LoginRoute> {

                val loginViewModel: LoginViewModel = hiltViewModel()

                LoginScreen(
                    navController,
                    loginViewModel
                )
            }

            composable<AppRoutes.SignupRoute> {

                val signupViewModel: SignupViewModel = hiltViewModel()

                SignupScreen(
                    navController,
                    signupViewModel
                )
            }

            composable<AppRoutes.HomeRoute> {
                val homeViewModel : HomeViewModel = hiltViewModel()
                HomeScreen(navController, homeViewModel)
            }

            composable<AppRoutes.CreateHabitRoute> {
                val createHabitViewModel : CreateHabitViewModel = hiltViewModel()
                CreateHabitScreen(navController,createHabitViewModel)
            }

            composable<AppRoutes.AnalysisRoute> {
                val analysisViewModel : AnalysisViewModel = hiltViewModel()
                AnalysisScreen(analysisViewModel)
            }
            composable<AppRoutes.HabitDetailRoute>{
                    backStackEntry ->

                val route = backStackEntry.toRoute<AppRoutes.HabitDetailRoute>()

                val viewModel: HabitDetailViewModel = hiltViewModel()

                HabitDetailScreen(
                    navController = navController,
                    habitId = route.habitId,
                    habitDetailViewModel = viewModel
                )
            }
        }
    }
}
