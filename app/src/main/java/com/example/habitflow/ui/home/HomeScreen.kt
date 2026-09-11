package com.example.habitflow.ui.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.example.habitflow.CommonUiEvent
import com.example.habitflow.ui.TestTags

@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel
) {

    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val showLoader = remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        homeViewModel.uiEvent
            .flowWithLifecycle(
                lifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            )
            .collect { event ->

                when (event) {

                    is CommonUiEvent.Navigate -> {

                        navController.navigate(event.route)
                    }

                    is CommonUiEvent.ShowToast -> {

                        Toast.makeText(
                            context,
                            event.msg,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is CommonUiEvent.ShowLoader -> {

                        showLoader.value = true
                    }

                    else -> {
                        showLoader.value = false
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {


        Text(
            text = "Today's Habits",
            style = MaterialTheme.typography.headlineMedium
        )



        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Completed today: ${uiState.completedHabitIds.size}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.testTag(TestTags.COMPLETED_COUNT)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {

            CircularProgressIndicator()

        } else {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag(TestTags.HABIT_LIST)
            ) {
                items(
                    items = uiState.habits,
                    key = { it.id}
                ) { habit ->

                    HabitItem(
                        habit = habit,
                        isCompleted =
                            uiState.completedHabitIds.contains(habit.id),
                        onToggle = {
                            homeViewModel.onEvent(
                                HomeScreenEvents.onHabitToggled(habit)
                            )
                        },
                        onClick = {
                            homeViewModel.onEvent(HomeScreenEvents.onHabitClicked(habit.id))
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))


    }
}