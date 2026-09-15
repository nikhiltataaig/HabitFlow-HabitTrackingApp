package com.example.habitflow.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.habitflow.domain.repository.AuthRepository
import com.example.habitflow.ui.navigation.authNavGraph
import com.example.habitflow.ui.navigation.mainNavGraph

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authRepository: AuthRepository
) {
    val navController = rememberNavController()

    val currentUser = authRepository.getCurrentUser()

    val startDestination =
        if (currentUser != null) {
            AppRoutes.DashboardGraph
        } else {
            AppRoutes.AuthGraph
        }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        authNavGraph(navController)
        mainNavGraph(navController)
    }
}
