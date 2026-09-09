package com.example.habitflow.ui.analysis




import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Composable
fun HabitHeatmap(
    completedDates: Set<LocalDate>,
    weeks: Int = 20
) {

    val today = LocalDate.now()

    val startDate = today
        .minusWeeks((weeks - 1).toLong())
        .with(
            TemporalAdjusters.previousOrSame(
                DayOfWeek.MONDAY
            )
        )

    val totalDays = weeks * 7

    val dates = (0 until totalDays).map {
        startDate.plusDays(it.toLong())
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            ),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        repeat(weeks) { weekIndex ->

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                repeat(7) { dayIndex ->

                    val index =
                        weekIndex * 7 + dayIndex

                    val date = dates[index]

                    HeatmapCell(
                        date = date,
                        completed =
                            date in completedDates,
                        enabled =
                            !date.isAfter(today)
                    )
                }
            }
        }
    }
}
@Composable
private fun HeatmapCell(
    date: LocalDate,
    completed: Boolean,
    enabled: Boolean
) {

    val backgroundColor = when {
        completed ->
            MaterialTheme.colorScheme.primary

        else ->
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    }

    val borderColor = when {
        !enabled ->
            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)

        else ->
            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    }

    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(2.dp)
            )
    )
}
