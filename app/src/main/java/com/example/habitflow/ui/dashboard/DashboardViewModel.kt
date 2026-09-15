package com.example.habitflow.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitflow.CommonUiEvent
import com.example.habitflow.domain.model.Habit
import com.example.habitflow.domain.model.HabitCompletion
import com.example.habitflow.domain.model.HabitFrequency
import com.example.habitflow.domain.repository.AuthRepository
import com.example.habitflow.domain.repository.CompletionRepository
import com.example.habitflow.domain.repository.HabitRepository
import com.example.habitflow.ui.AppRoutes
import com.example.habitflow.ui.analysis.AnalysisScreenEvents
import com.example.habitflow.ui.analysis.AnalysisUiState
import com.example.habitflow.ui.analysis.DailyHabitActivity
import com.example.habitflow.ui.analysis.HabitAnalysis
import com.example.habitflow.ui.createHabit.CreateHabitScreenEvent
import com.example.habitflow.ui.createHabit.CreateHabitUiState
import com.example.habitflow.ui.home.HomeScreenEvents
import com.example.habitflow.ui.home.HomeUiState
import com.google.android.gms.maps.model.Dash
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
import kotlin.collections.count


@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val authRepository: AuthRepository,
    private val completionRepository: CompletionRepository
) : ViewModel() {

    private val _uiStateCreateHabit = MutableStateFlow(CreateHabitUiState())
    val uiStateCreateHabit = _uiStateCreateHabit.asStateFlow()

    private val _uiStateAnalysis = MutableStateFlow(AnalysisUiState())
    val uiStateAnalysis = _uiStateAnalysis.asStateFlow()

    private val _uiStateHome = MutableStateFlow(HomeUiState())
    val uiStateHome= _uiStateHome.asStateFlow()

    private val _uiEvent = Channel<CommonUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadHome()
        loadAnalysis()
    }

    fun onEvent(event: DashboardScreenEvent) {

        when (event) {

            is DashboardScreenEvent.OnNameChanged -> {
                _uiStateCreateHabit.update {
                    it.copy(
                        name = event.name,
                        errorMessage = null
                    )
                }
            }

            is DashboardScreenEvent.OnDescriptionChanged -> {
                _uiStateCreateHabit.update {
                    it.copy(
                        description = event.description,
                        errorMessage = null
                    )
                }
            }

            is DashboardScreenEvent.OnFrequencyChanged -> {

                _uiStateCreateHabit.update {
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

            is DashboardScreenEvent.OnDaySelected -> {
                toggleDay(event.day)
            }

            is DashboardScreenEvent.OnCreateClicked -> {
                createHabit()
            }

            is DashboardScreenEvent.OnBackClicked -> {
                viewModelScope.launch {
                    _uiEvent.send(
                        CommonUiEvent.Navigate(AppRoutes.HomeRoute)
                    )
                }
            }
            is DashboardScreenEvent.OnRefresh -> {
                loadAnalysis()
            }

            is DashboardScreenEvent.OnLogoutClicked ->{
                viewModelScope.launch {
                    authRepository.logout()
                    _uiEvent.send(CommonUiEvent.Navigate(AppRoutes.AuthGraph))
                }
            }
            is DashboardScreenEvent.OnHabitToggled -> {
                toggleHabit(event.habit)
            }
            is DashboardScreenEvent.OnHabitClicked ->
                viewModelScope.launch {
                    _uiEvent.send(CommonUiEvent.Navigate(AppRoutes.HabitDetailRoute(event.habitId)))
                }

        }
    }


    private fun toggleDay(day: Int) {

        _uiStateCreateHabit.update { state ->

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

        val state = _uiStateCreateHabit.value

        if (state.name.isBlank()) {
            _uiStateCreateHabit.update {
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
            _uiStateCreateHabit.update {
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

                _uiStateCreateHabit.update {
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
                        AppRoutes.DashboardRoute
                    )
                )

            } else {

                _uiStateCreateHabit.update {
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





    private fun loadAnalysis() {

        viewModelScope.launch {

            _uiEvent.send(CommonUiEvent.ShowLoader)
            _uiStateAnalysis.update {
                it.copy(

                    errorMessage = null
                )
            }

            val user = authRepository.getCurrentUser()

            if (user == null) {

                _uiStateAnalysis.update {
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

                _uiStateAnalysis.update {
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

            _uiStateAnalysis.update {

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







    private fun loadHome() {

        viewModelScope.launch {

            val user = authRepository.getCurrentUser()

            if (user == null) {
                _uiStateHome.update {
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
                _uiStateHome.update {
                    it.copy(
                        errorMessage = "Failed to load habits"
                    )
                }
                return@launch
            }


            if (completionsResult.isFailure) {
                _uiStateHome.update {
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

            _uiStateHome.update {
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
                habit.id in _uiStateHome.value.completedHabitIds


            if (isCompleted) {

                val result =
                    completionRepository.deleteCompletion(
                        userId = user.id,
                        habitId = habit.id,
                        date = today.toString()
                    )

                if (result.isSuccess) {

                    _uiStateHome.update {
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

                    _uiStateHome.update {
                        it.copy(
                            completedHabitIds =
                                it.completedHabitIds + habit.id
                        )
                    }
                }
            }
            loadAnalysis()
        }
    }
}