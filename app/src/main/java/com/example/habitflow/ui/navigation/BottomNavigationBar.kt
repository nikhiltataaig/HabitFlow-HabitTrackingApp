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
    selectedPage: Int,
    onPageSelected: (Int) -> Unit
) {

    NavigationBar {

        NavigationBarItem(
            selected = selectedPage == 0,
            onClick = {
                onPageSelected(0)
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

        NavigationBarItem(
            selected = selectedPage == 1,
            onClick = {
                onPageSelected(1)
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

        NavigationBarItem(
            selected = selectedPage == 2,
            onClick = {
                onPageSelected(2)
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
