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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jenil.weather.domain.model.HourlyForecast
import com.jenil.weather.ui.theme.WeatherExtraShapes
import com.jenil.weather.ui.theme.WeatherTheme
import com.jenil.weather.ui.theme.glassCard
import com.jenil.weather.utils.Sunrise
import com.jenil.weather.utils.Sunset
import dev.chrisbanes.haze.HazeState
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
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val timelineItems = remember(hourlyData) {
        buildList {
            hourlyData.forEachIndexed { index, forecast ->
                add(HourlyTimelineItem.ForecastItem(forecast = forecast, isNow = index == 0))

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

    val cardShape = RoundedCornerShape(24.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "HOURLY FORECAST",
            style = WeatherTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = WeatherTheme.colors.onSurfaceMuted,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(
                    hazeState = hazeState,
                    shape = cardShape
                )
                .background(
                    color = WeatherTheme.colors.surfaceCard,
                    shape = cardShape
                )
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
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

    val itemShape = WeatherExtraShapes.forecastChip

    Column(
        modifier = Modifier
            .width(64.dp)
            .clip(itemShape)
            .then(
                if (isNow) {
                    Modifier
                        .background(WeatherTheme.colors.surfaceElevatedHigh)
                        .border(
                            width = 1.dp,
                            color = WeatherTheme.colors.accentSky.copy(alpha = 0.5f),
                            shape = itemShape
                        )
                } else {
                    Modifier
                }
            )
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isNow) "Now" else forecast.time,
            style = WeatherTheme.typography.labelMedium,
            fontWeight = if (isNow) FontWeight.Bold else FontWeight.Medium,
            color = if (isNow) WeatherTheme.colors.accentSky else WeatherTheme.colors.onSurfaceMuted,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(8.dp))

        WeatherConditionIcon(
            condition = forecast.condition,
            isDay = forecast.isDay,
            modifier = Modifier.size(30.dp),
            sourceTag = "HourlyItem-${forecast.time}",
            showBackgroundBadge = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${displayTemp}°",
            style = WeatherTheme.typography.titleMedium,
            color = WeatherTheme.colors.onSurface,
            fontWeight = if (isNow) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@Composable
fun SunEventItem(
    time: String,
    isSunset: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = WeatherExtraShapes.forecastChip
    val accentColor = WeatherTheme.colors.accentSun

    Column(
        modifier = modifier
            .width(64.dp)
            .clip(shape)
            .background(accentColor.copy(alpha = 0.1f))
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.25f),
                shape = shape
            )
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = time,
            style = WeatherTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = WeatherTheme.colors.onSurfaceMuted,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(8.dp))

        Icon(
            imageVector = if (isSunset) Sunset else Sunrise,
            contentDescription = if (isSunset) "Sunset" else "Sunrise",
            tint = accentColor,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isSunset) "Sunset" else "Sunrise",
            style = WeatherTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = WeatherTheme.colors.onSurface,
            maxLines = 1
        )
    }
}