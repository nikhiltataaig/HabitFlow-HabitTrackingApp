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
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.habitflow.ui.AppRoutes

@Composable
fun BottomNavigationBar(
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {

        // HOME
        NavigationBarItem(
            selected = currentRoute?.contains(
                AppRoutes.HomeRoute::class.simpleName ?: ""
            ) == true,

            onClick = {
                navController.navigate(AppRoutes.HomeRoute) {

                    popUpTo<AppRoutes.HomeRoute> {
                        saveState = true
                    }

                    launchSingleTop = true
                    restoreState = true
                }
            },

            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            },

            label = {
                Text("Home")
            }
        )

        // CREATE HABIT
        NavigationBarItem(
            selected = currentRoute?.contains(
                AppRoutes.CreateHabitRoute::class.simpleName ?: ""
            ) == true,

            onClick = {
                navController.navigate(AppRoutes.CreateHabitRoute) {

                    popUpTo<AppRoutes.HomeRoute> {
                        saveState = true
                    }

                    launchSingleTop = true
                    restoreState = true
                }
            },

            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Habit"
                )
            },

            label = {
                Text("Create")
            }
        )

        // ANALYSIS
        NavigationBarItem(
            selected = currentRoute?.contains(
                AppRoutes.AnalysisRoute::class.simpleName ?: ""
            ) == true,

            onClick = {
                navController.navigate(AppRoutes.AnalysisRoute) {

                    popUpTo<AppRoutes.HomeRoute> {
                        saveState = true
                    }

                    launchSingleTop = true
                    restoreState = true
                }
            },

            icon = {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "Analysis"
                )
            },

            label = {
                Text("Analysis")
            }
        )
    }
}
