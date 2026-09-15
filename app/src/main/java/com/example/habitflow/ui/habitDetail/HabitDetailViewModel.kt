package com.example.habitflow.ui.habitDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.CommonUiEvent
import com.example.habitflow.domain.model.Habit
import com.example.habitflow.domain.model.HabitFrequency
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
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val completionRepository: CompletionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(HabitDetailUiState())

    val uiState =
        _uiState.asStateFlow()

    private val _uiEvent =
        Channel<CommonUiEvent>(Channel.BUFFERED)

    val uiEvent =
        _uiEvent.receiveAsFlow()


    fun onEvent(event: HabitDetailScreenEvents) {

        when (event) {

            HabitDetailScreenEvents.OnBackClicked -> {
                viewModelScope.launch {
                    _uiEvent.send(
                        CommonUiEvent.Navigate(AppRoutes.DashboardRoute)
                    )
                }
            }

            HabitDetailScreenEvents.OnDeleteClicked -> {
                deleteHabit()
            }

            HabitDetailScreenEvents.OnToggleComplete -> {
                toggleCompletion()
            }

            HabitDetailScreenEvents.OnDeleteConfirmed->{
                deleteHabit()
            }

            HabitDetailScreenEvents.OnEditClicked->{
                editClicked()
            }
        }
    }



    fun editClicked(){

        viewModelScope.launch {
            _uiEvent.send(CommonUiEvent.Navigate(AppRoutes.CreateHabitRoute(
                name = _uiState.value.habit?.name,

            )))
        }
    }
    fun loadHabit(habitId: String?) {

        if (habitId.isNullOrBlank()) {

            _uiState.update {
                it.copy(

                    errorMessage = "Invalid habit"
                )
            }

            return
        }

        viewModelScope.launch {

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

                return@launch
            }

            val habitResult =
                habitRepository.getHabit(
                    userId = user.id,
                    habitId = habitId
                )

            if (habitResult.isFailure) {

                _uiState.update {
                    it.copy(

                        errorMessage = "Unable to load habit"
                    )
                }

                return@launch
            }

            val habit =
                habitResult.getOrThrow()

            val completionResult =
                completionRepository.getCompletionsForHabit(
                    userId = user.id,
                    habitId = habitId
                )

            if (completionResult.isFailure) {

                _uiState.update {
                    it.copy(
                        habit = habit,

                        errorMessage = "Unable to load completion data"
                    )
                }

                return@launch
            }

            val completions =
                completionResult.getOrThrow()

            val completedDates =
                completions
                    .map { LocalDate.parse(it.date) }
                    .toSet()

            val currentStreak =
                calculateCurrentStreak(
                    habit = habit,
                    completedDates = completedDates
                )

            val bestStreak =
                calculateBestStreak(
                    habit = habit,
                    completedDates = completedDates
                )

            val completionRate =
                calculateCompletionRate(
                    habit = habit,
                    completedDates = completedDates
                )

            _uiState.update {

                it.copy(
                    habit = habit,
                    completedDates = completedDates,
                    currentStreak = currentStreak,
                    bestStreak = bestStreak,
                    completedCount = completions.size,
                    completionRate = completionRate,

                    errorMessage = null
                )
            }
        }
    }


    private fun toggleCompletion() {

        viewModelScope.launch {

            val habit =
                _uiState.value.habit
                    ?: return@launch

            val user =
                authRepository.getCurrentUser()
                    ?: return@launch

            val today =
                LocalDate.now()

            /*
             * Only allow completion on a scheduled day.
             */
            if (!isScheduledDay(habit, today)) {

                _uiEvent.send(
                    CommonUiEvent.ShowToast(
                        "This habit is not scheduled for today"
                    )
                )

                return@launch
            }

            val isCompleted =
                today in _uiState.value.completedDates

            if (isCompleted) {

                val result =
                    completionRepository.deleteCompletion(
                        userId = user.id,
                        habitId = habit.id,
                        date = today.toString()
                    )

                if (result.isFailure) {

                    _uiEvent.send(
                        CommonUiEvent.ShowToast(
                            "Unable to remove completion"
                        )
                    )

                    return@launch
                }

            } else {

                val completion =
                    com.example.habitflow.domain.model.HabitCompletion(
                        id = "${habit.id}_${today}",
                        userId = user.id,
                        habitId = habit.id,
                        date = today.toString(),
                        completedAt = System.currentTimeMillis()
                    )

                val result =
                    completionRepository.createCompletion(
                        completion
                    )

                if (result.isFailure) {

                    _uiEvent.send(
                        CommonUiEvent.ShowToast(
                            "Unable to mark habit as complete"
                        )
                    )

                    return@launch
                }
            }

            /*
             * Repository is offline-first, so update the UI
             * immediately by reloading local completion data.
             */
            loadHabit(habit.id)
        }
    }


    private fun deleteHabit() {

        viewModelScope.launch {

            val habit =
                _uiState.value.habit
                    ?: return@launch

            val user =
                authRepository.getCurrentUser()

            if (user == null) {

                _uiEvent.send(
                    CommonUiEvent.ShowToast(
                        "User not found"
                    )
                )

                return@launch
            }

            _uiState.update {
                it.copy(

                    errorMessage = null
                )
            }

            val result =
                habitRepository.deleteHabit(
                    userId = user.id,
                    habitId = habit.id
                )

            if (result.isFailure) {

                _uiState.update {
                    it.copy(

                        errorMessage = "Unable to delete habit"
                    )
                }

                _uiEvent.send(
                    CommonUiEvent.ShowToast(
                        "Unable to delete habit"
                    )
                )

                return@launch
            }

            _uiEvent.send(
                CommonUiEvent.ShowToast(
                    "Habit deleted"
                )
            )

            /*
             * Go back to Home after successful deletion.
             */
            _uiEvent.send(
                CommonUiEvent.Navigate(
                    AppRoutes.DashboardRoute
                )
            )
        }
    }


    private fun calculateCompletionRate(
        habit: Habit,
        completedDates: Set<LocalDate>
    ): Int {

        if (completedDates.isEmpty()) {
            return 0
        }

        val today = LocalDate.now()

        val startDate =
            completedDates.minOrNull()
                ?: return 0

        var scheduledDays = 0
        var completedDays = 0

        var currentDate = startDate

        while (!currentDate.isAfter(today)) {

            if (isScheduledDay(habit, currentDate)) {

                scheduledDays++

                if (currentDate in completedDates) {
                    completedDays++
                }
            }

            currentDate =
                currentDate.plusDays(1)
        }

        if (scheduledDays == 0) {
            return 0
        }

        return ((completedDays.toDouble() / scheduledDays) * 100)
            .toInt()
    }


    private fun calculateCurrentStreak(
        habit: Habit,
        completedDates: Set<LocalDate>
    ): Int {

        var currentDate = LocalDate.now()
        var streak = 0

        /*
         * For weekly habits, today might not be scheduled.
         * Start from the previous scheduled day.
         */
        if (!isScheduledDay(habit, currentDate)) {

            currentDate =
                findPreviousScheduledDay(
                    habit = habit,
                    date = currentDate
                )
                    ?: return 0
        }

        while (true) {

            if (!isScheduledDay(habit, currentDate)) {

                currentDate =
                    currentDate.minusDays(1)

                continue
            }

            /*
             * A missed scheduled day breaks the streak.
             */
            if (currentDate !in completedDates) {
                break
            }

            streak++

            currentDate =
                currentDate.minusDays(1)
        }

        return streak
    }


    private fun calculateBestStreak(
        habit: Habit,
        completedDates: Set<LocalDate>
    ): Int {

        if (completedDates.isEmpty()) {
            return 0
        }

        val sortedDates =
            completedDates.sorted()

        var currentStreak = 0
        var bestStreak = 0

        var previousScheduledDate: LocalDate? = null

        for (date in sortedDates) {

            /*
             * Ignore dates that aren't scheduled
             * for this habit.
             */
            if (!isScheduledDay(habit, date)) {
                continue
            }

            if (previousScheduledDate == null) {

                currentStreak = 1

            } else {

                val expectedPreviousDate =
                    findPreviousScheduledDay(
                        habit = habit,
                        date = date
                    )

                if (expectedPreviousDate == previousScheduledDate) {

                    currentStreak++

                } else {

                    currentStreak = 1
                }
            }

            bestStreak =
                maxOf(
                    bestStreak,
                    currentStreak
                )

            previousScheduledDate = date
        }

        return bestStreak
    }


    private fun findPreviousScheduledDay(
        habit: Habit,
        date: LocalDate
    ): LocalDate? {

        var currentDate =
            date.minusDays(1)

        repeat(7) {

            if (isScheduledDay(habit, currentDate)) {
                return currentDate
            }

            currentDate =
                currentDate.minusDays(1)
        }

        return null
    }


    private fun isScheduledDay(
        habit: Habit,
        date: LocalDate
    ): Boolean {

        return when (habit.frequency) {

            HabitFrequency.DAILY -> {
                true
            }

            HabitFrequency.WEEKLY -> {
                date.dayOfWeek.value in habit.targetDays
            }
            else -> false
        }
    }
}