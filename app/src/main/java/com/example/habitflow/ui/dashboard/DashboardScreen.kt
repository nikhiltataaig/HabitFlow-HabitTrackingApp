package com.example.habitflow.ui.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.habitflow.ui.AppRoutes
import com.example.habitflow.ui.analysis.AnalysisScreen
import com.example.habitflow.ui.createHabit.CreateHabitScreen
import com.example.habitflow.ui.home.HomeScreen
import com.example.habitflow.ui.navigation.BottomNavigationBar
import kotlinx.coroutines.launch


@Composable
fun DashboardScreen(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 3 }
    )
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedPage = pagerState.currentPage,
                onPageSelected = { page ->

                    scope.launch {
                        pagerState.animateScrollToPage(page)
                    }
                }
            )
        }
    ) { paddingValues ->

        HorizontalPager(
            modifier = Modifier.padding(paddingValues),
            state = pagerState
        ) { page ->

            when (page) {
                0 -> HomeScreen(navController, dashboardViewModel)

                1 -> CreateHabitScreen(navController, dashboardViewModel)

                2 -> AnalysisScreen(dashboardViewModel)

            }
        }


    }
}