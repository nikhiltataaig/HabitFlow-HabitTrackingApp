package com.example.habitflow.ui.analysis


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.habitflow.ui.TestTags

@Composable
 fun HabitAnalysisCard(
    analysis: HabitAnalysis
) {

    Card(
        modifier = Modifier.fillMaxWidth().testTag("${TestTags.HABIT_ANALYSIS_CARD_PREFIX}${analysis.habit.id}")
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = analysis.habit.name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text(
                        text = "Current streak",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "${analysis.currentStreak} days",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Column {
                    Text(
                        text = "Best streak",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "${analysis.bestStreak} days",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Column {
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = analysis.completedCount.toString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "Activity",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            HabitHeatmap(
                completedDates = analysis.completedDates
            )
        }
    }
}
