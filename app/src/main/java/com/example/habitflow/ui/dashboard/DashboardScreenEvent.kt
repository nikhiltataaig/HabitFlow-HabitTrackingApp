package com.example.habitflow.ui.dashboard

import com.example.habitflow.domain.model.Habit
import com.example.habitflow.domain.model.HabitFrequency
import com.example.habitflow.ui.analysis.AnalysisScreenEvents
import com.example.habitflow.ui.createHabit.CreateHabitScreenEvent
import com.example.habitflow.ui.home.HomeScreenEvents

sealed interface DashboardScreenEvent{

    data class OnNameChanged(
        val name: String
    ) : DashboardScreenEvent

    data class OnDescriptionChanged(
        val description: String
    ) : DashboardScreenEvent

    data class OnFrequencyChanged(
        val frequency: HabitFrequency
    ) : DashboardScreenEvent

    data class OnDaySelected(
        val day: Int
    ) : DashboardScreenEvent

    data object OnCreateClicked : DashboardScreenEvent

    data object OnBackClicked : DashboardScreenEvent
    data object OnRefresh : DashboardScreenEvent

    data class  OnHabitClicked(val habitId : String): DashboardScreenEvent

    data object OnLogoutClicked: DashboardScreenEvent
    data class OnHabitToggled(
        val habit: Habit
    ) : DashboardScreenEvent

}