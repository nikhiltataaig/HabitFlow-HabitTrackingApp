package com.example.habitflow.ui.habitDetail


import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import com.example.habitflow.CommonUiEvent
import com.example.habitflow.domain.model.HabitFrequency
import com.example.habitflow.ui.TestTags
import com.example.habitflow.ui.analysis.HabitHeatmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    navController: NavController,
    habitId: String?,
    habitDetailViewModel: HabitDetailViewModel
) {

    val uiState by habitDetailViewModel.uiState
        .collectAsStateWithLifecycle()

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    val showLoader = remember {
        mutableStateOf(false)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    /*
     * Load the habit when this screen is opened.
     */
    LaunchedEffect(habitId) {
        habitDetailViewModel.loadHabit(habitId)
    }

    LaunchedEffect(Unit) {
        habitDetailViewModel.uiEvent
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

    /*
     * -----------------------------------------
     * Delete confirmation dialog
     * -----------------------------------------
     */

    if (showDeleteDialog) {

        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },

            title = {
                Text(
                    text = "Delete habit?"
                )
            },

            text = {
                Text(
                    text = "This will permanently delete this habit and its associated data."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        showDeleteDialog = false

                        habitDetailViewModel.onEvent(
                            HabitDetailScreenEvents.OnDeleteConfirmed
                        )
                    },
                    modifier = Modifier.testTag(TestTags.CONFIRM_DELETE_BUTTON)
                ) {

                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    },
                    modifier = Modifier.testTag(TestTags.CANCEL_DELETE_BUTTON)
                ) {

                    Text(
                        text = "Cancel"
                    )
                }
            }
        )
    }

    Scaffold(

        /*
         * -----------------------------------------
         * Top App Bar
         * -----------------------------------------
         */

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        text = uiState.habit?.name ?: "Habit",
                        modifier = Modifier.testTag("HABIT_APP_BAR_TITLE")
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },

                actions = {



                    /*
                     * Delete
                     */

                    IconButton(
                        onClick = {
                            showDeleteDialog = true
                        },
                        modifier = Modifier.testTag(TestTags.DELETE_HABIT_ICON)
                    ) {

                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete habit",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }

    ) { innerPadding ->

        /*
         * -----------------------------------------
         * Loading
         * -----------------------------------------
         */

        if (showLoader.value) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                CircularProgressIndicator()
            }

            return@Scaffold
        }

        /*
         * -----------------------------------------
         * Error
         * -----------------------------------------
         */

        if (uiState.errorMessage != null && uiState.habit == null) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                TextButton(
                    onClick = {
                        habitDetailViewModel.loadHabit(habitId)
                    },
                    modifier = Modifier.testTag(TestTags.RETRY_BUTTON)
                ) {

                    Text(
                        text = "Retry"
                    )
                }
            }

            return@Scaffold
        }

        val habit = uiState.habit

        if (habit == null) {
            return@Scaffold
        }

        /*
         * -----------------------------------------
         * Content
         * -----------------------------------------
         */

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            /*
             * -----------------------------------------
             * Habit information
             * -----------------------------------------
             */

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.testTag(TestTags.HABIT_DETAIL_NAME)
                    )

                    if (habit.description.isNotBlank()) {

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag(TestTags.HABIT_DETAIL_DESC)
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text = "Frequency",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = when (habit.frequency) {
                            HabitFrequency.DAILY -> "Every day"

                            HabitFrequency.WEEKLY ->
                                formatTargetDays(habit.targetDays)
                            else -> "false"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }


            /*
             * -----------------------------------------
             * Statistics
             * -----------------------------------------
             */

            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                StatisticCard(
                    modifier = Modifier.weight(1f).testTag("${TestTags.STAT_CARD_PREFIX}Current Streak"),
                    title = "Current Streak",
                    value = "${uiState.currentStreak} days"
                )

                StatisticCard(
                    modifier = Modifier.weight(1f).testTag("${TestTags.STAT_CARD_PREFIX}Best Streak"),
                    title = "Best Streak",
                    value = "${uiState.bestStreak} days"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                StatisticCard(
                    modifier = Modifier.weight(1f).testTag("${TestTags.STAT_CARD_PREFIX}Completed"),
                    title = "Completed",
                    value = uiState.completedCount.toString()
                )

                StatisticCard(
                    modifier = Modifier.weight(1f).testTag("${TestTags.STAT_CARD_PREFIX}Completion"),
                    title = "Completion",
                    value = "${uiState.completionRate}%"
                )
            }


            /*
             * -----------------------------------------
             * Heatmap
             * -----------------------------------------
             */

            Text(
                text = "Activity",
                style = MaterialTheme.typography.titleLarge
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Completion history",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    HabitHeatmap(
                        completedDates = uiState.completedDates
                    )
                }
            }


            /*
             * -----------------------------------------
             * Error message
             * -----------------------------------------
             */

            uiState.errorMessage?.let { error ->

                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }


            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }
    }
}


/*
 * -----------------------------------------
 * Statistic Card
 * -----------------------------------------
 */

@Composable
private fun StatisticCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {

    Card(
        modifier = modifier
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}


/*
 * -----------------------------------------
 * Weekly habit day formatter
 * -----------------------------------------
 */

private fun formatTargetDays(
    targetDays: List<Int>
): String {

    if (targetDays.isEmpty()) {
        return "No days selected"
    }

    val dayNames = mapOf(
        1 to "Monday",
        2 to "Tuesday",
        3 to "Wednesday",
        4 to "Thursday",
        5 to "Friday",
        6 to "Saturday",
        7 to "Sunday"
    )

    return targetDays
        .sorted()
        .mapNotNull { dayNames[it] }
        .joinToString(", ")
}