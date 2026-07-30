package com.jenil.weather.ui.core

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jenil.weather.domain.model.HourlyForecast
import com.jenil.weather.utils.Sunrise
import com.jenil.weather.utils.Sunset
import kotlin.math.roundToInt

private sealed interface HourlyTimelineItem {
    data class ForecastItem(
        val forecast: HourlyForecast,
        val isNow: Boolean
    ) : HourlyTimelineItem

    data class SunEventItem(
        val time: String,
        val isSunset: Boolean
    ) : HourlyTimelineItem
}

@Composable
fun HourlyForecastSection(
    hourlyData: List<HourlyForecast>,
    isCelsius: Boolean,
    modifier: Modifier = Modifier,
) {
    val timelineItems = remember(hourlyData) {
        buildList {
            hourlyData.forEachIndexed { index, forecast ->
                // Add the regular hourly item
                add(HourlyTimelineItem.ForecastItem(forecast = forecast, isNow = index == 0))


                // Sunset: Day (true) -> Night (false)
                if (forecast.sunEventTime != null) {
                    add(
                        HourlyTimelineItem.SunEventItem(
                            time = forecast.sunEventTime,
                            isSunset = forecast.isSunsetEvent
                        )
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Hourly forecast",
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
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = timelineItems,
                    key = { item ->
                        when (item) {
                            is HourlyTimelineItem.ForecastItem -> "forecast_${item.forecast.time}"
                            is HourlyTimelineItem.SunEventItem -> if (item.isSunset) "sunset_${item.time}" else "sunrise_${item.time}"
                        }
                    }
                ) { item ->
                    when (item) {
                        is HourlyTimelineItem.ForecastItem -> {
                            HourlyForecastItem(
                                forecast = item.forecast,
                                isCelsius = isCelsius,
                                isNow = item.isNow
                            )
                        }

                        is HourlyTimelineItem.SunEventItem -> {
                            SunEventItem(
                                time = item.time,
                                isSunset = item.isSunset
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyForecastItem(
    forecast: HourlyForecast,
    isCelsius: Boolean,
    isNow: Boolean = false
) {
    val displayTemp = if (isCelsius) {
        forecast.temperature
    } else {
        (forecast.temperature * 9f / 5f + 32f).roundToInt()
    }

    Column(
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isNow) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                else Modifier
            )
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isNow) "Now" else forecast.time,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isNow) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isNow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(
                alpha = 0.8f
            ),
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(8.dp))

        WeatherConditionIcon(
            condition = forecast.condition,
            isDay = forecast.isDay,
            modifier = Modifier.size(26.dp),
            sourceTag = "HourlyItem-${forecast.time}",
            showBackgroundBadge = true
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${displayTemp}°",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SunEventItem(
    time: String,
    isSunset: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(8.dp))

        Icon(
            imageVector = if (isSunset) Sunset else Sunrise,
            contentDescription = if (isSunset) "Sunset" else "Sunrise",
            tint = if (isSunset) Color(0xFFFF7043) else Color(0xFFFFCA28),
            modifier = Modifier.size(26.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isSunset) "Sunset" else "Sunrise",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
            maxLines = 1
        )
    }
}