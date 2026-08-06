package com.jenil.weather.ui.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jenil.weather.domain.model.WeatherData
import com.jenil.weather.ui.theme.WeatherTheme
import com.jenil.weather.ui.theme.glassCard
import dev.chrisbanes.haze.HazeState

@Composable
fun WeatherMetricsGrid(
    weatherData: WeatherData,
    isKmh: Boolean,
    isPrecipitationMm: Boolean,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val windSpeedValue = if (isKmh) weatherData.windSpeed.toFloat() else (weatherData.windSpeed * 0.621371).toFloat()
    val windSpeedUnit = if (isKmh) "km/h" else "mph"
    val cardShape = RoundedCornerShape(24.dp)

    // Same sourcing pattern as WeatherExtraDetailsGrid's displayRainfall:
    // dailyForecast's precipitationSum with a safe fallback, unit/precision
    // switched on isPrecipitationMm rather than a hardcoded placeholder.
    val rainfallSum = weatherData.dailyForecast.firstOrNull()?.precipitationSum ?: 0.0
    val rainfallValue = if (isPrecipitationMm) rainfallSum.toFloat() else (rainfallSum * 0.0393701).toFloat()
    val rainfallUnit = if (isPrecipitationMm) "mm" else "in"
    val rainfallDecimals = if (isPrecipitationMm) 1 else 2

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "CURRENT DETAILS",
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WeatherStateItem(
                    label = "Humidity",
                    value = weatherData.humidity.toFloat(),
                    unit = "%",
                    icon = Icons.Outlined.WaterDrop,
                    tint = WeatherTheme.colors.accentSky,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp)
                        .background(WeatherTheme.colors.divider)
                )

                WeatherStateItem(
                    label = "Wind Speed",
                    value = windSpeedValue,
                    unit = windSpeedUnit,
                    decimals = 1,
                    icon = Icons.Outlined.Air,
                    tint = WeatherTheme.colors.accentSky,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp)
                        .background(WeatherTheme.colors.divider)
                )

                WeatherStateItem(
                    label = "Rainfall",
                    value = rainfallValue,
                    unit = rainfallUnit,
                    decimals = rainfallDecimals,
                    icon = Icons.Outlined.Umbrella,
                    tint = WeatherTheme.colors.accentSky,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}