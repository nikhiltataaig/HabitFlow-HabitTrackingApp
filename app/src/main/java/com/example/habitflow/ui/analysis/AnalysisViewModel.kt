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

            _uiEvent.send(CommonUiEvent.ShowLoader)
            _uiState.update {
                it.copy(

                    errorMessage = null
                )
            }

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

                        errorMessage = "Failed to load analytics"
                    )
                }
                _uiEvent.send(CommonUiEvent.DoNothing)

                return@launch
            }

            val habits = habitsResult
                .getOrThrow()
                .filter { it.isActive }

            val completions =
                completionsResult.getOrThrow()

            val dailyActivity =
                calculateDailyActivity(
                    habits = habits,
                    completions = completions
                )

            val today = LocalDate.now().toString()

            val completedToday = completions
                .count { it.date == today }

            val habitAnalysis = habits.map { habit ->

                val habitCompletions = completions
                    .filter { it.habitId == habit.id }

                val completedDates = habitCompletions
                    .map { LocalDate.parse(it.date) }
                    .toSet()

                HabitAnalysis(
                    habit = habit,
                    currentStreak = calculateCurrentStreak(
                        habit,
                        habitCompletions
                    ),
                    bestStreak = calculateBestStreak(
                        habit,
                        habitCompletions
                    ),
                    completedCount = habitCompletions.size,
                    completedDates = completedDates
                )
            }
            val completionRate =
                calculateOverallCompletionRate(
                    habits = habits,
                    completions = completions
                )

            _uiState.update {

                it.copy(
                    totalHabits = habits.size,
                    completedToday = completedToday,
                    completionRate = completionRate,
                    dailyActivity = dailyActivity,
                    habitAnalysis = habitAnalysis,

                )
            }
            _uiEvent.send(CommonUiEvent.DoNothing)
        }
    }

    private fun calculateCurrentStreak(
        habit: Habit,
        completions: List<HabitCompletion>
    ): Int {

        val completedDates = completions
            .map { LocalDate.parse(it.date) }
            .toSet()

        var date = LocalDate.now()
        var streak = 0

        while (isScheduledDay(habit, date)) {

            if (date in completedDates) {
                streak++
            } else {
                break
            }

            date = date.minusDays(1)
        }

        return streak
    }

    private fun calculateBestStreak(
        habit: Habit,
        completions: List<HabitCompletion>
    ): Int {

        val completedDates = completions
            .map { LocalDate.parse(it.date) }
            .toSet()

        if (completedDates.isEmpty()) {
            return 0
        }

        val sortedDates = completedDates.sorted()

        var bestStreak = 1
        var currentStreak = 1

        for (index in 1 until sortedDates.size) {

            val previousDate = sortedDates[index - 1]
            val currentDate = sortedDates[index]

            if (
                areConsecutiveScheduledDays(
                    habit = habit,
                    previousDate = previousDate,
                    currentDate = currentDate
                )
            ) {

                currentStreak++

                bestStreak =
                    maxOf(
                        bestStreak,
                        currentStreak
                    )

            } else {

                currentStreak = 1
            }
        }

        return bestStreak
    }

    private fun isScheduledDay(
        habit: Habit,
        date: LocalDate
    ): Boolean {

        return when (habit.frequency) {

            HabitFrequency.DAILY -> true

            HabitFrequency.WEEKLY ->
                date.dayOfWeek.value in habit.targetDays
            else -> {
                false
            }
        }
    }

    private fun calculateDailyActivity(
        habits: List<Habit>,
        completions: List<HabitCompletion>,
        days: Int = 30
    ): List<DailyHabitActivity> {

        val today = LocalDate.now()

        val startDate = today.minusDays(
            (days - 1).toLong()
        )

        val completionMap =
            completions.groupBy {
                LocalDate.parse(it.date)
            }

        return (0 until days).map { offset ->

            val date = startDate.plusDays(
                offset.toLong()
            )

            val totalHabits = habits.count { habit ->

                isScheduledDay(
                    habit = habit,
                    date = date
                )
            }

            val completedHabits =
                completionMap[date]
                    ?.count { completion ->
                        habits.any {
                            it.id == completion.habitId
                        }
                    }
                    ?: 0

            DailyHabitActivity(
                date = date,
                totalHabits = totalHabits,
                completedHabits = completedHabits
            )
        }
    }
    private fun areConsecutiveScheduledDays(
        habit: Habit,
        previousDate: LocalDate,
        currentDate: LocalDate
    ): Boolean {

        var date = previousDate.plusDays(1)

        while (date.isBefore(currentDate)) {

            if (isScheduledDay(habit, date)) {
                return false
            }

            date = date.plusDays(1)
        }

        return isScheduledDay(habit, currentDate)
    }

    private fun calculateOverallCompletionRate(
        habits: List<Habit>,
        completions: List<HabitCompletion>
    ): Int {

        if (habits.isEmpty()) {
            return 0
        }

        val today = LocalDate.now()

        val firstDate = completions
            .minOfOrNull {
                LocalDate.parse(it.date)
            }
            ?: today

        var possibleCompletions = 0

        habits.forEach { habit ->

            var date = firstDate

            while (!date.isAfter(today)) {

                if (isScheduledDay(habit, date)) {
                    possibleCompletions++
                }

                date = date.plusDays(1)
            }
        }

        if (possibleCompletions == 0) {
            return 0
        }

        return (
                completions.size.toFloat() /
                        possibleCompletions *
                        100
                ).toInt()
    }
}