package com.example.habitflow.ui.analysis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AnalysisScreen(
    analysisViewModel: AnalysisViewModel
) {

    val uiState by analysisViewModel.uiState
        .collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Your Progress",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        if (uiState.isLoading) {

            CircularProgressIndicator()

        } else {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                AnalysisCard(
                    title = "Active Habits",
                    value = uiState.totalHabits.toString(),
                    modifier = Modifier.weight(1f)
                )

                AnalysisCard(
                    title = "Completed Today",
                    value = uiState.completedToday.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                AnalysisCard(
                    title = "Completion Rate",
                    value = "${uiState.completionRate}%",
                    modifier = Modifier.weight(1f)
                )

                AnalysisCard(
                    title = "Current Streak",
                    value = "${uiState.currentStreak} days",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            AnalysisCard(
                title = "Best Streak",
                value = "${uiState.bestStreak} days",
                modifier = Modifier.fillMaxWidth()
            )
        }

        uiState.errorMessage?.let { error ->

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}