package com.jenil.weather.ui.core

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jenil.weather.R
import com.jenil.weather.domain.model.WeatherData
import com.jenil.weather.ui.settings.PressureUnit
import com.jenil.weather.ui.theme.WeatherTheme
import com.jenil.weather.utils.getAqiColor
import com.jenil.weather.utils.getAqiColorScale
import com.jenil.weather.utils.getAqiDescription
import com.jenil.weather.utils.getAqiProgress
import com.jenil.weather.utils.getDewPointDescription
import com.jenil.weather.utils.getFeelsLikeDescription
import com.jenil.weather.utils.getPressureDescription
import com.jenil.weather.utils.getUvColor
import com.jenil.weather.utils.getUvColorScale
import com.jenil.weather.utils.getUvDescription
import com.jenil.weather.utils.getUvProgress
import com.jenil.weather.utils.getVisibilityDescription
import com.jenil.weather.utils.getWindDirectionString
import dev.chrisbanes.haze.HazeState
import kotlin.math.roundToInt

@Composable
fun WeatherExtraDetailsGrid(
    weatherData: WeatherData,
    isCelsius: Boolean,
    isKmh: Boolean,
    pressureUnit: PressureUnit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val currentUv = weatherData.hourlyForecast.firstOrNull()?.uvIndex ?: 0.0
    val uvDescription = getUvDescription(currentUv)
    val aqiDescription = getAqiDescription(weatherData.aqi)

    val isCurrentlyDay = weatherData.hourlyForecast.firstOrNull()?.isDay ?: true

    val sunEventTitle = if (isCurrentlyDay) "SUNSET" else "SUNRISE"
    val sunEventTime = if (isCurrentlyDay) weatherData.sunset else weatherData.sunrise

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
        PressureUnit.MBAR -> "${weatherData.pressure.toInt()} mbar"
        PressureUnit.INHG -> "%.2f inHg".format(weatherData.pressure * 0.02953)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "WEATHER INSIGHTS",
            style = WeatherTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = WeatherTheme.colors.onSurfaceMuted,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        // Grid Row 1: Sun Event & Wind Direction
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SunTimeCard(
                title = sunEventTitle,
                iconRes = if (isCurrentlyDay) R.drawable.ic_sunset else R.drawable.ic_sunrise,
                time = sunEventTime,
                hazeState = hazeState,
                modifier = Modifier.weight(1f)
            )

            WindCompassCard(
                windDirectionDegree = weatherData.windDirection,
                windDirectionString = getWindDirectionString(weatherData.windDirection),
                hazeState = hazeState,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid Row 2: Air Quality & UV Index
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeatherDetailCard(
                title = "AIR QUALITY",
                icon = Icons.Outlined.Air,
                value = weatherData.aqi?.toString() ?: "N/A",
                subtitle = aqiDescription,
                accentColor = getAqiColor(weatherData.aqi),
                progress = getAqiProgress(weatherData.aqi),
                progressTrackColors = getAqiColorScale(),
                hazeState = hazeState,
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
                hazeState = hazeState,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        // Grid Row 3: Dew Point & Moon Phase

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GenericWeatherCard(
                title = "DEW POINT",
                graphicContent = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_dew_point),
                        contentDescription = "Dew Point",
                        modifier = Modifier.size(64.dp)
                    )
                },
                value = "${weatherData.dewPoint}°",
                subtitle = getDewPointDescription(weatherData.dewPoint),
                hazeState = hazeState,
                modifier = Modifier.weight(1f)
            )

            GenericWeatherCard(
                title = "MOON PHASE",
                graphicContent = {
                    MoonPhaseIcon(
                        modifier = Modifier.size(64.dp),
                        phase = weatherData.moonPhase
                    )
                },
                value = weatherData.moonPhase.title,
                subtitle = weatherData.moonPhase.description,
                hazeState = hazeState,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid Row 4: Feels Like & Visibility
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GenericWeatherCard(
                title = "FEELS LIKE",
                graphicContent = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_temprature),
                        contentDescription = "temp",
                        modifier = Modifier.size(64.dp)
                    )
                },
                value = "${displayApparentTemp}°",
                subtitle = feelsLikeDescription,
                hazeState = hazeState,
                modifier = Modifier.weight(1f)
            )

            GenericWeatherCard(
                title = "VISIBILITY",
                graphicContent = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_visibility),
                        contentDescription = "Visibility",
                        modifier = Modifier.size(64.dp)
                    )
                },
                value = "%.1f $visibilityUnit".format(displayVisibility),
                subtitle = visibilityDescription,
                hazeState = hazeState,
                modifier = Modifier.weight(1f)
            )
        }


        Spacer(modifier = Modifier.height(12.dp))
        // Grid Row 5: Pressure
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GenericWeatherCard(
                title = "PRESSURE",
                graphicContent = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_pressure),
                        contentDescription = "Pressure",
                        modifier = Modifier.size(64.dp)
                    )
                },
                value = displayPressure,
                subtitle = getPressureDescription(weatherData.pressure),
                hazeState = hazeState,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.weight(1f))
        }

    }
}