package com.example.habitflow.ui.createHabit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.CommonUiEvent
import com.example.habitflow.domain.model.Habit
import com.example.habitflow.domain.model.HabitFrequency
import com.example.habitflow.domain.repository.AuthRepository
import com.example.habitflow.domain.repository.HabitRepository
import com.example.habitflow.ui.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateHabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateHabitUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<CommonUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: CreateHabitScreenEvent) {

        when (event) {

            is CreateHabitScreenEvent.OnNameChanged -> {
                _uiState.update {
                    it.copy(
                        name = event.name,
                        errorMessage = null
                    )
                }
            }

            is CreateHabitScreenEvent.OnDescriptionChanged -> {
                _uiState.update {
                    it.copy(
                        description = event.description,
                        errorMessage = null
                    )
                }
            }

            is CreateHabitScreenEvent.OnFrequencyChanged -> {

                _uiState.update {
                    it.copy(
                        frequency = event.frequency,
                        targetDays = if (
                            event.frequency == HabitFrequency.DAILY
                        ) {
                            emptyList()
                        } else {
                            it.targetDays
                        },
                        errorMessage = null
                    )
                }
            }

            is CreateHabitScreenEvent.OnDaySelected -> {
                toggleDay(event.day)
            }

            CreateHabitScreenEvent.OnCreateClicked -> {
                createHabit()
            }

            CreateHabitScreenEvent.OnBackClicked -> {
                viewModelScope.launch {
                    _uiEvent.send(
                        CommonUiEvent.Navigate(AppRoutes.HomeRoute)
                    )
                }
            }
        }
    }

    private fun toggleDay(day: Int) {

        _uiState.update { state ->

            val updatedDays =
                if (day in state.targetDays) {
                    state.targetDays - day
                } else {
                    state.targetDays + day
                }

            state.copy(
                targetDays = updatedDays.sorted(),
                errorMessage = null
            )
        }
    }

    private fun createHabit() {

        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Habit name cannot be empty"
                )
            }
            return
        }

        if (
            state.frequency == HabitFrequency.WEEKLY &&
            state.targetDays.isEmpty()
        ) {
            _uiState.update {
                it.copy(
                    errorMessage = "Select at least one day"
                )
            }
            return
        }

        viewModelScope.launch {

            _uiEvent.send(CommonUiEvent.ShowLoader)

            val user = authRepository.getCurrentUser()

            if (user == null) {

                _uiState.update {
                    it.copy(
                        errorMessage = "User not found"
                    )
                }

                _uiEvent.send(CommonUiEvent.DoNothing)
                return@launch
            }

            val now = System.currentTimeMillis()

            val habit = Habit(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                name = state.name.trim(),
                description = state.description.trim(),
                frequency = state.frequency,
                targetDays = state.targetDays,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )

            val result = habitRepository.createHabit(habit)

            if (result.isSuccess) {

                _uiEvent.send(
                    CommonUiEvent.ShowToast(
                        "Habit created successfully"
                    )
                )

                _uiEvent.send(
                    CommonUiEvent.Navigate(
                        AppRoutes.HomeRoute
                    )
                )

            } else {

                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to create habit"
                    )
                }

                _uiEvent.send(
                    CommonUiEvent.DoNothing
                )
            }
        }
    }
}