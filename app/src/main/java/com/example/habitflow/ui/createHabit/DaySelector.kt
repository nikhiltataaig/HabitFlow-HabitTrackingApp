package com.example.habitflow.ui.createHabit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp


@Composable
 fun DaySelector(
    selectedDays: List<Int>,
    enabled: Boolean,
    onDaySelected: (Int) -> Unit
) {

    val days = listOf(
        1 to "Mon",
        2 to "Tue",
        3 to "Wed",
        4 to "Thu",
        5 to "Fri",
        6 to "Sat",
        7 to "Sun"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            days.take(4).forEach { (dayNumber, dayName) ->

                FilterChip(
                    selected = dayNumber in selectedDays,
                    onClick = {
                        onDaySelected(dayNumber)
                    },
                    label = {
                        Text(dayName)
                    },
                    enabled = enabled
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            days.drop(4).forEach { (dayNumber, dayName) ->

                FilterChip(
                    selected = dayNumber in selectedDays,
                    onClick = {
                        onDaySelected(dayNumber)
                    },
                    label = {
                        Text(dayName)
                    },
                    enabled = enabled
                )
            }
        }
    }
}