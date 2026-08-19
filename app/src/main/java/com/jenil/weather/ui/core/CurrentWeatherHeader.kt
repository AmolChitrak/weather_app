package com.jenil.weather.ui.core

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jenil.weather.data.remote.dto.toDisplayName
import com.jenil.weather.domain.model.WeatherData
import com.jenil.weather.ui.theme.WeatherTheme
import com.jenil.weather.ui.theme.glassCard
import com.jenil.weather.utils.WeatherConditionIcon
import dev.chrisbanes.haze.HazeState
import kotlin.math.roundToInt


@Composable
fun HomeTopBar(
    modifier: Modifier = Modifier,
    cityName: String,
    onLocationClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(WeatherTheme.colors.surfaceElevated)
                .clickable { onLocationClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.NearMe,
                contentDescription = "Current Location",
                tint = WeatherTheme.colors.onSurfaceMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = cityName,
            style = WeatherTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = WeatherTheme.colors.onSurface
        )

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(WeatherTheme.colors.surfaceElevated)
                .clickable { onMenuClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.GridView,
                contentDescription = "Menu Options",
                tint = WeatherTheme.colors.onSurfaceMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CurrentWeatherHeader(
    weatherData: WeatherData,
    isCelsius: Boolean,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val targetTemp = if (isCelsius) {
        weatherData.temperature
    } else {
        (weatherData.temperature * 9f / 5f + 32f).roundToInt()
    }
    val unitSuffix = if (isCelsius) "°C" else "°F"

    val animatedTemp by animateIntAsState(
        targetValue = targetTemp,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "temp_anim"
    )

    val heroShape = WeatherTheme.extraShapes.heroCard

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .glassCard(hazeState, heroShape)
            .background(WeatherTheme.colors.surfaceCard, heroShape)
            .padding(vertical = 28.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            WeatherConditionIcon(
                condition = weatherData.condition,
                modifier = Modifier.size(140.dp),
                sourceTag = "CurrentWeatherHeader"
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = animatedTemp,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(150)) },
                label = "temp_crossfade"
            ) { temp ->
                Text(
                    text = "$temp$unitSuffix",
                    style = WeatherTheme.extraType.tempDisplay,
                    color = WeatherTheme.colors.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = weatherData.condition.toDisplayName(),
                style = WeatherTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = WeatherTheme.colors.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = weatherData.date,
                style = WeatherTheme.typography.bodyMedium,
                color = WeatherTheme.colors.onSurfaceMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}