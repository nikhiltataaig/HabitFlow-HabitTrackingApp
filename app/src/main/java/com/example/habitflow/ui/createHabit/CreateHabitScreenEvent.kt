package com.example.habitflow.ui.createHabit

import com.example.habitflow.domain.model.HabitFrequency

sealed interface CreateHabitScreenEvent {

    data class OnNameChanged(
        val name: String
    ) : CreateHabitScreenEvent

    data class OnDescriptionChanged(
        val description: String
    ) : CreateHabitScreenEvent

    data class OnFrequencyChanged(
        val frequency: HabitFrequency
    ) : CreateHabitScreenEvent

    data class OnDaySelected(
        val day: Int
    ) : CreateHabitScreenEvent

    data object OnCreateClicked : CreateHabitScreenEvent

    data object OnBackClicked : CreateHabitScreenEvent
}