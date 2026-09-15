package com.example.habitflow.ui.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.example.habitflow.ui.AppRoutes
import com.example.habitflow.ui.dashboard.DashboardScreen
import com.example.habitflow.ui.dashboard.DashboardViewModel
import com.example.habitflow.ui.habitDetail.HabitDetailScreen
import com.example.habitflow.ui.habitDetail.HabitDetailViewModel

fun NavGraphBuilder.mainNavGraph(
    navController: NavHostController
) {
    navigation<AppRoutes.DashboardGraph>(
        startDestination = AppRoutes.DashboardRoute
    ) {

        composable<AppRoutes.DashboardRoute>{

                val dashboardViewModel : DashboardViewModel = hiltViewModel()
                DashboardScreen(navController,dashboardViewModel)

        }

        composable<AppRoutes.HabitDetailRoute> { backStackEntry ->

            val route =
                backStackEntry.toRoute<AppRoutes.HabitDetailRoute>()

            val viewModel: HabitDetailViewModel = hiltViewModel()

            HabitDetailScreen(
                navController = navController,
                habitId = route.habitId,
                habitDetailViewModel = viewModel
            )
        }

    }
}