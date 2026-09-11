package com.example.habitflow.ui.home


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.habitflow.domain.model.Habit
import com.example.habitflow.ui.TestTags

@Composable
fun HabitItem(
    habit: Habit,
    isCompleted: Boolean,
    onToggle: () -> Unit,
    onClick : () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth().testTag("${TestTags.HABIT_ITEM_PREFIX}${habit.id}"),
        onClick = onClick
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = habit.name
                )

                if (habit.description.isNotBlank()) {

                    Text(
                        text = habit.description
                    )
                }
            }

            Switch(
                checked = isCompleted,
                onCheckedChange = {
                    onToggle()
                },
                modifier = Modifier.testTag("${TestTags.HABIT_TOGGLE_PREFIX}${habit.id}")
            )
        }
    }
}