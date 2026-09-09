package com.example.habitflow.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.CommonUiEvent
import com.example.habitflow.domain.model.Habit
import com.example.habitflow.domain.model.HabitCompletion
import com.example.habitflow.domain.repository.AuthRepository
import com.example.habitflow.domain.repository.CompletionRepository
import com.example.habitflow.domain.repository.HabitRepository
import com.example.habitflow.ui.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val habitRepository: HabitRepository,
    private val completionRepository: CompletionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()


    private val _uiEvent = Channel<CommonUiEvent>(Channel.BUFFERED)

    val uiEvent = _uiEvent.receiveAsFlow()



    init {
        loadHome()
    }

    fun onEvent(event: HomeScreenEvents) {

        when (event) {

            is HomeScreenEvents.onLogoutClicked ->{
                viewModelScope.launch {
                    _uiEvent.send(CommonUiEvent.Navigate(AppRoutes.LogoutRoute))
                }
            }
            is HomeScreenEvents.onHabitToggled -> {
                toggleHabit(event.habit)
            }
            is HomeScreenEvents.onHabitClicked ->
                viewModelScope.launch {
                    _uiEvent.send(CommonUiEvent.Navigate(AppRoutes.HabitDetailRoute(event.habitId)))
                 }
        }
    }



    private fun loadHome() {

        viewModelScope.launch {

            val user = authRepository.getCurrentUser()

            if (user == null) {
                _uiState.update {
                    it.copy(
                        errorMessage = "User not logged in"
                    )
                }
                return@launch
            }

            val habitsResult =
                habitRepository.getHabits(user.id)

            val completionsResult =
                completionRepository.getAllCompletions(user.id)

            if (habitsResult.isFailure) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to load habits"
                    )
                }
                return@launch
            }


            if (completionsResult.isFailure) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to load completions"
                    )
                }
                return@launch
            }

            val today = LocalDate.now().toString()

            val activeHabits = habitsResult
                .getOrThrow()
                .filter { it.isActive }

            val completedToday = completionsResult
                .getOrThrow()
                .filter { it.date == today }
                .map { it.habitId }
                .toSet()

            _uiState.update {
                it.copy(
                    habits = activeHabits,
                    completedHabitIds = completedToday,
                    errorMessage = null
                )
            }
        }
    }



    private fun toggleHabit(habit: Habit) {

        viewModelScope.launch {

            val user = authRepository.getCurrentUser()
                ?: return@launch

            val today = LocalDate.now()

            val isCompleted =
                habit.id in _uiState.value.completedHabitIds


            if (isCompleted) {

                val result =
                    completionRepository.deleteCompletion(
                        userId = user.id,
                        habitId = habit.id,
                        date = today.toString()
                    )

                if (result.isSuccess) {

                    _uiState.update {
                        it.copy(
                            completedHabitIds =
                                it.completedHabitIds - habit.id
                        )
                    }
                }

            } else {

                val completion = HabitCompletion(
                    id = "${habit.id}_$today",
                    userId = user.id,
                    habitId = habit.id,
                    date = today.toString(),
                    completedAt = System.currentTimeMillis()
                )

                val result =
                    completionRepository.createCompletion(
                        completion
                    )

                if (result.isSuccess) {

                    _uiState.update {
                        it.copy(
                            completedHabitIds =
                                it.completedHabitIds + habit.id
                        )
                    }
                }
            }
        }
    }
}