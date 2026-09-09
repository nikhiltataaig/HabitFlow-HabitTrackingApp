package com.example.habitflow.ui.habitDetail

sealed interface HabitDetailScreenEvents {

    data object OnBackClicked : HabitDetailScreenEvents

    data object OnDeleteClicked : HabitDetailScreenEvents

    data object  OnDeleteConfirmed : HabitDetailScreenEvents
    data object OnToggleComplete : HabitDetailScreenEvents

    data object OnEditClicked : HabitDetailScreenEvents

}