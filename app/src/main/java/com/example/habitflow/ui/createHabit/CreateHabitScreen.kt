package com.example.habitflow.ui.createHabit

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
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
import androidx.navigation.NavController
import com.example.habitflow.CommonUiEvent
import com.example.habitflow.domain.model.HabitFrequency
import com.example.habitflow.ui.AppRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitScreen(
    navController: NavController,
    createHabitViewModel: CreateHabitViewModel
) {

    val uiState by createHabitViewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val showLoader = remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        createHabitViewModel.uiEvent
            .flowWithLifecycle(
                lifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            )
            .collect { event ->
                showLoader.value = false
                when (event) {

                    is CommonUiEvent.Navigate -> {



                        navController.navigate(event.route) {
                            popUpTo(AppRoutes.CreateHabitRoute) {
                                inclusive = true
                            }
                        }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Create Habit")
                },
                navigationIcon = {
                    TextButton(
                        onClick = {
                            createHabitViewModel.onEvent(
                                CreateHabitScreenEvent.OnBackClicked
                            )
                        },
                        enabled = !showLoader.value
                    ) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = uiState.name,
                onValueChange = {
                    createHabitViewModel.onEvent(
                        CreateHabitScreenEvent.OnNameChanged(it)
                    )
                },
                label = {
                    Text("Habit name")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !showLoader.value
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = {
                    createHabitViewModel.onEvent(
                        CreateHabitScreenEvent.OnDescriptionChanged(it)
                    )
                },
                label = {
                    Text("Description")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                enabled = !showLoader.value
            )

            Text(
                text = "Frequency",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                FilterChip(
                    selected = uiState.frequency == HabitFrequency.DAILY,
                    onClick = {
                        createHabitViewModel.onEvent(
                            CreateHabitScreenEvent.OnFrequencyChanged(
                                HabitFrequency.DAILY
                            )
                        )
                    },
                    label = {
                        Text("Daily")
                    },
                    enabled = !showLoader.value
                )

                FilterChip(
                    selected = uiState.frequency == HabitFrequency.WEEKLY,
                    onClick = {
                        createHabitViewModel.onEvent(
                            CreateHabitScreenEvent.OnFrequencyChanged(
                                HabitFrequency.WEEKLY
                            )
                        )
                    },
                    label = {
                        Text("Weekly")
                    },
                    enabled = !showLoader.value
                )
            }

            if (uiState.frequency == HabitFrequency.WEEKLY) {

                Text(
                    text = "Select days",
                    style = MaterialTheme.typography.titleMedium
                )

                DaySelector(
                    selectedDays = uiState.targetDays,
                    enabled = !showLoader.value,
                    onDaySelected = { day ->

                        createHabitViewModel.onEvent(
                            CreateHabitScreenEvent.OnDaySelected(day)
                        )
                    }
                )
            }

            uiState.errorMessage?.let { error ->

                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Button(
                onClick = {
                    createHabitViewModel.onEvent(
                        CreateHabitScreenEvent.OnCreateClicked
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !showLoader.value
            ) {

                if (showLoader.value) {

                    CircularProgressIndicator()

                } else {

                    Text("Create Habit")
                }
            }
        }
    }
}