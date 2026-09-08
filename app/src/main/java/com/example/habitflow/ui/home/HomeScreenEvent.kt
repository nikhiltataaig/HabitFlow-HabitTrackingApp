package com.example.habitflow.ui.home


import com.example.habitflow.domain.model.Habit

sealed interface HomeScreenEvents {


    data object onLogoutClicked: HomeScreenEvents
    data class onHabitToggled(
        val habit: Habit
    ) : HomeScreenEvents

}