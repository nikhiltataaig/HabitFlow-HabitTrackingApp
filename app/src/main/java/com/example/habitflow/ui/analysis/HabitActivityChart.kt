package com.example.habitflow.ui.analysis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlin.math.ceil

@Composable
fun HabitActivityChart(
    dailyActivity: List<DailyHabitActivity>,
    modifier: Modifier = Modifier
) {

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val totalHabitColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {

        Text(
            text = "Habit Activity",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "Your habits over the last 30 days",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        androidx.compose.foundation.layout.Spacer(
            modifier = Modifier.height(16.dp)
        )

        if (dailyActivity.isEmpty()) {

            Text(
                text = "No activity data available.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 32.dp)
            )

            return@Column
        }

        val maxHabits = dailyActivity
            .maxOfOrNull {
                maxOf(
                    it.totalHabits,
                    it.completedHabits
                )
            }
            ?.coerceAtLeast(1)
            ?: 1

        val yAxisMax = ceil(maxHabits.toDouble()).toInt()

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {


            Column(
                modifier = Modifier
                    .height(220.dp)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {

                for (value in yAxisMax downTo 0) {

                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            /*
             * Chart
             */
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(220.dp)
            ) {

                val chartWidth = size.width
                val chartHeight = size.height

                val horizontalPadding = 8.dp.toPx()
                val verticalPadding = 8.dp.toPx()

                val graphWidth =
                    chartWidth - horizontalPadding * 2

                val graphHeight =
                    chartHeight - verticalPadding * 2

                val dataCount = dailyActivity.size

                /*
                 * Convert data index into X coordinate.
                 */
                fun xPosition(index: Int): Float {

                    if (dataCount == 1) {
                        return chartWidth / 2f
                    }

                    return horizontalPadding +
                            (index.toFloat() / (dataCount - 1)) *
                            graphWidth
                }

                /*
                 * Convert habit count into Y coordinate.
                 */
                fun yPosition(value: Int): Float {

                    return verticalPadding +
                            graphHeight -
                            (value.toFloat() / yAxisMax) *
                            graphHeight
                }


                /*
                 * ---------------------------------------------
                 * Horizontal grid lines
                 * ---------------------------------------------
                 */

                for (value in 0..yAxisMax) {

                    val y = yPosition(value)

                    drawLine(
                        color = gridColor,
                        start = Offset(
                            x = horizontalPadding,
                            y = y
                        ),
                        end = Offset(
                            x = chartWidth - horizontalPadding,
                            y = y
                        ),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(
                                6.dp.toPx(),
                                6.dp.toPx()
                            )
                        )
                    )
                }


                /*
                 * ---------------------------------------------
                 * Total habits area
                 * ---------------------------------------------
                 */

                val totalPath = Path()

                dailyActivity.forEachIndexed { index, activity ->

                    val x = xPosition(index)
                    val y = yPosition(activity.totalHabits)

                    if (index == 0) {
                        totalPath.moveTo(x, y)
                    } else {
                        totalPath.lineTo(x, y)
                    }
                }

                /*
                 * Close the path at the bottom
                 * so we can fill the area underneath
                 * the total-habits line.
                 */

                totalPath.lineTo(
                    xPosition(dataCount - 1),
                    yPosition(0)
                )

                totalPath.lineTo(
                    xPosition(0),
                    yPosition(0)
                )

                totalPath.close()

                drawPath(
                    path = totalPath,
                    color = totalHabitColor
                )


                /*
                 * ---------------------------------------------
                 * Total habits line
                 * ---------------------------------------------
                 */

                val totalLinePath = Path()

                dailyActivity.forEachIndexed { index, activity ->

                    val x = xPosition(index)
                    val y = yPosition(activity.totalHabits)

                    if (index == 0) {
                        totalLinePath.moveTo(x, y)
                    } else {
                        totalLinePath.lineTo(x, y)
                    }
                }

                drawPath(
                    path = totalLinePath,
                    color = primaryColor.copy(alpha = 0.35f),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )


                /*
                 * ---------------------------------------------
                 * Completed habits line
                 * ---------------------------------------------
                 */

                val completedPath = Path()

                dailyActivity.forEachIndexed { index, activity ->

                    val x = xPosition(index)
                    val y = yPosition(activity.completedHabits)

                    if (index == 0) {
                        completedPath.moveTo(x, y)
                    } else {
                        completedPath.lineTo(x, y)
                    }
                }

                drawPath(
                    path = completedPath,
                    color = primaryColor,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )


                /*
                 * ---------------------------------------------
                 * Completed data points
                 * ---------------------------------------------
                 */

                dailyActivity.forEachIndexed { index, activity ->

                    val x = xPosition(index)
                    val y = yPosition(activity.completedHabits)

                    drawCircle(
                        color = primaryColor,
                        radius = 3.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }


        /*
         * ---------------------------------------------
         * X-axis labels
         * ---------------------------------------------
         */

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 40.dp,
                    top = 8.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            /*
             * Show a limited number of labels so
             * 30 dates don't overlap.
             */
            val labelIndexes = when {

                dailyActivity.size <= 7 ->
                    dailyActivity.indices.toList()

                else ->
                    listOf(
                        0,
                        dailyActivity.size / 4,
                        dailyActivity.size / 2,
                        (dailyActivity.size * 3) / 4,
                        dailyActivity.lastIndex
                    ).distinct()
            }

            labelIndexes.forEach { index ->

                Text(
                    text = dailyActivity[index].date
                        .month
                        .name
                        .take(3)
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }
                            + " " +
                            dailyActivity[index].date.dayOfMonth,

                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }


        /*
         * ---------------------------------------------
         * Legend
         * ---------------------------------------------
         */

        androidx.compose.foundation.layout.Spacer(
            modifier = Modifier.height(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {

            LegendItem(
                color = primaryColor.copy(alpha = 0.35f),
                text = "Total habits"
            )

            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            LegendItem(
                color = primaryColor,
                text = "Completed"
            )
        }
    }
}


@Composable
private fun LegendItem(
    color: Color,
    text: String
) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Canvas(
            modifier = Modifier
                .padding(end = 6.dp)
                .height(8.dp)
                .fillMaxWidth(0.02f)
        ) {

            drawCircle(
                color = color,
                radius = size.minDimension / 2
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium
        )
    }
}