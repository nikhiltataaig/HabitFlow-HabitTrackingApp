package com.example.habitflow.ui.home


import com.example.habitflow.domain.model.Habit

sealed interface HomeScreenEvents {


    data class  onHabitClicked(val habitId : String): HomeScreenEvents

    data object onLogoutClicked: HomeScreenEvents
    data class onHabitToggled(
        val habit: Habit
    ) : HomeScreenEvents

}