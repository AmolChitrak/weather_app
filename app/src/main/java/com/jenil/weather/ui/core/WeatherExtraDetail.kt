package com.jenil.weather.ui.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jenil.weather.domain.model.WeatherData
import com.jenil.weather.ui.settings.PressureUnit
import com.jenil.weather.utils.Sunrise
import com.jenil.weather.utils.Sunset
import com.jenil.weather.utils.getAqiColor
import com.jenil.weather.utils.getAqiColorScale
import com.jenil.weather.utils.getAqiDescription
import com.jenil.weather.utils.getAqiProgress
import com.jenil.weather.utils.getFeelsLikeDescription
import com.jenil.weather.utils.getPressureDescription
import com.jenil.weather.utils.getUvColor
import com.jenil.weather.utils.getUvColorScale
import com.jenil.weather.utils.getUvDescription
import com.jenil.weather.utils.getUvProgress
import com.jenil.weather.utils.getVisibilityDescription
import com.jenil.weather.utils.getWindDirectionString
import kotlin.math.roundToInt

@Composable
fun WeatherExtraDetailsGrid(
    weatherData: WeatherData,
    isCelsius: Boolean,
    isKmh: Boolean,
    isPrecipitationMm: Boolean,
    pressureUnit: PressureUnit,
    modifier: Modifier = Modifier
) {

    val currentUv = weatherData.hourlyForecast.firstOrNull()?.uvIndex ?: 0.0
    val uvDescription = getUvDescription(currentUv)
    val aqiDescription = getAqiDescription(weatherData.aqi)

    val rainfallSum = weatherData.dailyForecast.firstOrNull()?.precipitationSum ?: 0.0
    val displayRainfall = if (isPrecipitationMm) {
        "%.1f mm".format(rainfallSum)
    } else {
        "%.2f in".format(rainfallSum * 0.0393701)
    }

    val isCurrentlyDay = weatherData.hourlyForecast.firstOrNull()?.isDay ?: true

    val sunEventTitle = if (isCurrentlyDay) "SUNSET" else "SUNRISE"
    val sunEventIcon = if (isCurrentlyDay) Sunset else Sunrise
    val sunEventTime = if (isCurrentlyDay) weatherData.sunset else weatherData.sunrise
    val sunEventSubtitle = if (isCurrentlyDay) "Sunrise: ${weatherData.sunrise}" else "Sunset: ${weatherData.sunset}"

    val displayApparentTemp = if (isCelsius) {
        weatherData.apparentTemperature
    } else {
        (weatherData.apparentTemperature * 9f / 5f + 32f).roundToInt()
    }
    val feelsLikeDescription = getFeelsLikeDescription(
        actual = weatherData.temperature,
        apparent = weatherData.apparentTemperature
    )

    val displayVisibility = if (isKmh) weatherData.visibility else weatherData.visibility * 0.6213711922
    val visibilityUnit = if (isKmh) "km" else "mi"
    val visibilityDescription = getVisibilityDescription(weatherData.visibility)

    val displayPressure = when (pressureUnit) {
        PressureUnit.HPA -> "${weatherData.pressure.toInt()} hPa"
        PressureUnit.MBAR -> "${weatherData.pressure.toInt()} mbar" // 1 hPa = 1 mbar
        PressureUnit.INHG -> "%.2f inHg".format(weatherData.pressure * 0.02953)
    }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Weather Insights",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Grid Row 1: Sun Event & Wind Direction
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            WeatherDetailCard(
                title = sunEventTitle,
                icon = sunEventIcon,
                value = sunEventTime,
                subtitle = sunEventSubtitle,
                modifier = Modifier.weight(1f)
            )

            WindCompassCard(
                windDirectionDegree = weatherData.windDirection,
                windDirectionString = getWindDirectionString(weatherData.windDirection),
                modifier = Modifier.weight(1f)
            )

        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid Row 2: AQI & UV Index
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            WeatherDetailCard(
                title = "AIR QUALITY",
                icon = Icons.Outlined.Air,
                value = weatherData.aqi?.toString() ?: "N/A",
                subtitle = aqiDescription,
                accentColor = getAqiColor(weatherData.aqi),
                progress = getAqiProgress(weatherData.aqi),
                progressTrackColors = getAqiColorScale(),
                modifier = Modifier.weight(1f)
            )

            WeatherDetailCard(
                title = "UV INDEX",
                icon = Icons.Outlined.LightMode,
                value = "%.1f".format(currentUv),
                subtitle = uvDescription,
                accentColor = getUvColor(currentUv),
                progress = getUvProgress(currentUv),
                progressTrackColors = getUvColorScale(),
                modifier = Modifier.weight(1f)
            )


        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid Row 3: Pressure & Rainfall
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            WeatherDetailCard(
                title = "RAINFALL",
                icon = Icons.Outlined.WaterDrop,
                value = displayRainfall,
                subtitle = if (rainfallSum > 0.0) "Expected today" else "No precipitation",
                modifier = Modifier.weight(1f)
            )

            WeatherDetailCard(
                title = "PRESSURE",
                icon = Icons.Outlined.Compress,
                value = displayPressure,
                subtitle = getPressureDescription(weatherData.pressure),
                modifier = Modifier.weight(1f)
            )

        }

        Spacer(modifier = Modifier.height(16.dp))

        // Row 4: Feels Like & Visibility
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WeatherDetailCard(
                title = "FEELS LIKE",
                icon = Icons.Outlined.Thermostat,
                value = "${displayApparentTemp}°",
                subtitle = feelsLikeDescription,
                modifier = Modifier.weight(1f)
            )
            WeatherDetailCard(
                title = "VISIBILITY",
                icon = Icons.Outlined.Visibility,
                value = "%.1f $visibilityUnit".format(displayVisibility),
                subtitle = visibilityDescription,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
