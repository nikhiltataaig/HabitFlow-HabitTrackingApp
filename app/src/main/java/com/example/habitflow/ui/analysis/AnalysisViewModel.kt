package com.example.habitflow.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.CommonUiEvent
import com.example.habitflow.domain.model.Habit
import com.example.habitflow.domain.model.HabitCompletion
import com.example.habitflow.domain.model.HabitFrequency
import com.example.habitflow.domain.repository.AuthRepository
import com.example.habitflow.domain.repository.CompletionRepository
import com.example.habitflow.domain.repository.HabitRepository
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
class AnalysisViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val completionRepository: CompletionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<CommonUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadAnalysis()
    }

    fun onEvent(event: AnalysisScreenEvents) {

        when (event) {

            AnalysisScreenEvents.OnRefresh -> {
                loadAnalysis()
            }
        }
    }

    private fun loadAnalysis() {

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val user = authRepository.getCurrentUser()

            if (user == null) {

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "User not found"
                    )
                }

                return@launch
            }

            val habitsResult =
                habitRepository.getHabits(user.id)

            val completionsResult =
                completionRepository.getAllCompletions(user.id)

            if (
                habitsResult.isFailure ||
                completionsResult.isFailure
            ) {

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load analytics"
                    )
                }

                return@launch
            }

            val habits = habitsResult
                .getOrThrow()
                .filter { it.isActive }

            val completions =
                completionsResult.getOrThrow()

            val today = LocalDate.now()

            val completedToday = completions
                .count {
                    it.date == today.toString()
                }

            val totalPossibleCompletions =
                calculateTotalPossibleCompletions(
                    habits,
                    completions
                )

            val completionRate =
                if (totalPossibleCompletions == 0) {
                    0
                } else {
                    (
                            completions.size.toFloat() /
                                    totalPossibleCompletions
                                    * 100
                            ).toInt()
                }

            val currentStreak =
                calculateCurrentStreak(completions)

            val bestStreak =
                calculateBestStreak(completions)

            _uiState.update {

                it.copy(
                    totalHabits = habits.size,
                    completedToday = completedToday,
                    completionRate = completionRate,
                    currentStreak = currentStreak,
                    bestStreak = bestStreak,
                    isLoading = false
                )
            }
        }
    }

    private fun calculateTotalPossibleCompletions(
        habits: List<Habit>,
        completions: List<HabitCompletion>
    ): Int {

        if (habits.isEmpty()) return 0

        val firstCompletionDate =
            completions
                .minOfOrNull {
                    LocalDate.parse(it.date)
                }
                ?: LocalDate.now()

        val today = LocalDate.now()

        var total = 0

        habits.forEach { habit ->

            var date = firstCompletionDate

            while (!date.isAfter(today)) {

                val shouldComplete =
                    when (habit.frequency) {

                        HabitFrequency.DAILY -> true

                        HabitFrequency.WEEKLY ->
                            date.dayOfWeek.value in habit.targetDays

                        else -> { false}
                    }

                if (shouldComplete) {
                    total++
                }

                date = date.plusDays(1)
            }
        }

        return total
    }

    private fun calculateCurrentStreak(
        completions: List<HabitCompletion>
    ): Int {

        val completedDates = completions
            .map { LocalDate.parse(it.date) }
            .toSet()

        var date = LocalDate.now()
        var streak = 0

        while (completedDates.contains(date)) {

            streak++
            date = date.minusDays(1)
        }

        return streak
    }

    private fun calculateBestStreak(
        completions: List<HabitCompletion>
    ): Int {

        val dates = completions
            .map { LocalDate.parse(it.date) }
            .distinct()
            .sorted()

        if (dates.isEmpty()) return 0

        var best = 1
        var current = 1

        for (i in 1 until dates.size) {

            if (dates[i] == dates[i - 1].plusDays(1)) {
                current++
                best = maxOf(best, current)
            } else {
                current = 1
            }
        }

        return best
    }
}