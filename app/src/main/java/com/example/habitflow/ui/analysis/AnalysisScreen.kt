package com.example.habitflow.ui.analysis

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.example.habitflow.CommonUiEvent
import com.example.habitflow.ui.AppRoutes

@Composable
fun AnalysisScreen(
    analysisViewModel: AnalysisViewModel
) {

    val uiState by analysisViewModel.uiState
        .collectAsStateWithLifecycle()


    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val showLoader = remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        analysisViewModel.uiEvent
            .flowWithLifecycle(
                lifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            )
            .collect { event ->
                showLoader.value = false
                when (event) {

                    is CommonUiEvent.Navigate -> {


                    }


                    is CommonUiEvent.ShowToast -> {


                        Toast.makeText(
                            context,
                            event.msg,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    CommonUiEvent.ShowLoader -> {

                        showLoader.value = true
                    }

                    else -> {
                        showLoader.value = false
                    }
                }
            }
    }

    PullToRefreshBox(
        isRefreshing = showLoader.value,
        onRefresh = {
            analysisViewModel.onEvent(AnalysisScreenEvents.OnRefresh)
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),

            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            /*
         * ------------------------------------------------
         * Header
         * ------------------------------------------------
         */

            item {

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = "Your Progress",
                    style = MaterialTheme.typography.headlineMedium
                )
            }


            /*
         * ------------------------------------------------
         * Loading
         * ------------------------------------------------
         */

            if (showLoader.value) {

                item {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator()
                    }
                }

            } else {

                /*
             * ------------------------------------------------
             * Overall Activity Chart
             * ------------------------------------------------
             */

                item {

                    HabitActivityChart(
                        dailyActivity = uiState.dailyActivity
                    )
                }


                /*
             * ------------------------------------------------
             * Overall Statistics
             * ------------------------------------------------
             */

                item {

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = "Overview",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                item {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        AnalysisCard(
                            modifier = Modifier.weight(1f),
                            title = "Active Habits",
                            value = uiState.totalHabits.toString()
                        )

                        AnalysisCard(
                            modifier = Modifier.weight(1f),
                            title = "Completed Today",
                            value = uiState.completedToday.toString()
                        )
                    }
                }

                item {

                    AnalysisCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = "Completion Rate",
                        value = "${uiState.completionRate}%"
                    )
                }


                /*
             * ------------------------------------------------
             * Habit Performance
             * ------------------------------------------------
             */

                item {

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = "Habit Performance",
                        style = MaterialTheme.typography.titleLarge
                    )
                }


                /*
             * ------------------------------------------------
             * Empty State
             * ------------------------------------------------
             */

                if (uiState.habitAnalysis.isEmpty()) {

                    item {

                        Text(
                            text = "No habit data available yet.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                } else {

                    /*
                 * ------------------------------------------------
                 * Individual Habit Analysis
                 * ------------------------------------------------
                 */

                    items(
                        items = uiState.habitAnalysis,
                        key = { it.habit.id }
                    ) { analysis ->

                        HabitAnalysisCard(
                            analysis = analysis
                        )
                    }
                }
            }


            /*
         * ------------------------------------------------
         * Error
         * ------------------------------------------------
         */

            uiState.errorMessage?.let { error ->

                item {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }


            /*
         * ------------------------------------------------
         * Bottom spacing
         * ------------------------------------------------
         */

            item {

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }
        }

    }
}