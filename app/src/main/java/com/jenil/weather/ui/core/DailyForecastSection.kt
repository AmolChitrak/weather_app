package com.jenil.weather.ui.core

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jenil.weather.domain.model.DailyForecast
import kotlin.math.roundToInt

@Composable
fun DailyForecastSection(
    dailyData: List<DailyForecast>,
    isCelsius: Boolean,
    modifier: Modifier = Modifier
) {
    if (dailyData.isEmpty()) return

    fun convert(temp: Int): Int =
        if (isCelsius) temp else (temp * 9f / 5f + 32f).roundToInt()

    val overallMin = dailyData.minOf { convert(it.lowTemp) }
    val overallMax = dailyData.maxOf { convert(it.highTemp) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)

    ) {
        Text(
            text = "7-day forecast",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                dailyData.forEachIndexed { index, forecast ->
                    DailyForecastItem(
                        forecast = forecast,
                        isCelsius = isCelsius,
                        isToday = index == 0,
                        overallMin = overallMin,
                        overallMax = overallMax
                    )

                    if (index < dailyData.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyForecastItem(
    forecast: DailyForecast,
    isCelsius: Boolean,
    isToday: Boolean = false,
    overallMin: Int? = null,
    overallMax: Int? = null
) {
    val displayLow = if (isCelsius) forecast.lowTemp else (forecast.lowTemp * 9f / 5f + 32f).roundToInt()
    val displayHigh = if (isCelsius) forecast.highTemp else (forecast.highTemp * 9f / 5f + 32f).roundToInt()

    val minBound = overallMin ?: displayLow
    val maxBound = overallMax ?: displayHigh
    val totalRange = (maxBound - minBound).coerceAtLeast(1)

    val beforeFraction = ((displayLow - minBound).toFloat() / totalRange).coerceIn(0f, 1f)
    val rangeFraction = ((displayHigh - displayLow).toFloat() / totalRange).coerceIn(0.05f, 1f)
    val afterFraction = (1f - beforeFraction - rangeFraction).coerceAtLeast(0f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isToday) "Today" else forecast.day,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.Center
        ) {

            WeatherConditionIcon(
                condition = forecast.condition,
                modifier = Modifier.size(24.dp),
                sourceTag = "DailyItem-${forecast.day}",
                showBackgroundBadge = true
            )
        }

        Text(
            text = "${displayLow}°",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            modifier = Modifier.width(32.dp)
        )

        // Low–high range bar, scaled against the week's overall min/max
        Row(
            modifier = Modifier
                .weight(1.4f)
                .height(4.dp)
                .padding(horizontal = 6.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
        ) {
            if (beforeFraction > 0f) Spacer(modifier = Modifier.weight(beforeFraction))
            Box(
                modifier = Modifier
                    .weight(rangeFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            if (afterFraction > 0f) Spacer(modifier = Modifier.weight(afterFraction))
        }

        Text(
            text = "${displayHigh}°",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End
        )
    }
}