package com.jenil.weather.ui.core

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jenil.weather.domain.model.DailyForecast
import com.jenil.weather.ui.theme.WeatherTheme
import com.jenil.weather.ui.theme.glassCard
import com.jenil.weather.utils.WeatherConditionIcon
import dev.chrisbanes.haze.HazeState
import kotlin.math.roundToInt

@Composable
fun DailyForecastSection(
    forecasts: List<DailyForecast>,
    isCelsius: Boolean,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    if (forecasts.isEmpty()) return

    val globalMinTemp = forecasts.minOf { it.lowTemp.toFloat() }
    val globalMaxTemp = forecasts.maxOf { it.highTemp.toFloat() }
    val cardShape = RoundedCornerShape(24.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "7-DAY FORECAST",
            style = WeatherTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = WeatherTheme.colors.onSurfaceMuted,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        Column(
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
                .padding(20.dp)
        ) {
            forecasts.forEachIndexed { index, forecast ->
                DailyForecastRow(
                    forecast = forecast,
                    isCelsius = isCelsius,
                    globalMin = globalMinTemp,
                    globalMax = globalMaxTemp
                )

                if (index < forecasts.lastIndex) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun DailyForecastRow(
    forecast: DailyForecast,
    isCelsius: Boolean,
    globalMin: Float,
    globalMax: Float
) {
    val lowFloat = forecast.lowTemp.toFloat()
    val highFloat = forecast.highTemp.toFloat()

    val targetHigh = if (isCelsius) highFloat else (highFloat * 9f / 5f + 32f)
    val targetLow = if (isCelsius) lowFloat else (lowFloat * 9f / 5f + 32f)
    val unitSuffix = "°"

    val animatedHighTemp by animateIntAsState(
        targetValue = targetHigh.roundToInt(),
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "daily_high_anim"
    )
    val animatedLowTemp by animateIntAsState(
        targetValue = targetLow.roundToInt(),
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "daily_low_anim"
    )

    val range = (globalMax - globalMin).coerceAtLeast(1f)
    val startFraction = ((lowFloat - globalMin) / range).coerceIn(0f, 1f)
    val endFraction = ((highFloat - globalMin) / range).coerceIn(0f, 1f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = forecast.day,
            style = WeatherTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = WeatherTheme.colors.onSurface,
            modifier = Modifier.width(54.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(44.dp)
        ) {
            WeatherConditionIcon(
                condition = forecast.condition,
                modifier = Modifier.size(28.dp),
                sourceTag = "DailyForecastRow",
                showBackgroundBadge = true
            )
        }

        AnimatedContent(
            targetState = animatedLowTemp,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "low_temp_text",
            modifier = Modifier.width(36.dp)
        ) { low ->
            Text(
                text = "$low$unitSuffix",
                style = WeatherTheme.typography.bodyMedium,
                color = WeatherTheme.colors.onSurfaceMuted,
                fontWeight = FontWeight.Medium
            )
        }

        TemperatureRangeBar(
            startFraction = startFraction,
            endFraction = endFraction,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
                .height(6.dp)
        )

        AnimatedContent(
            targetState = animatedHighTemp,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "high_temp_text",
            modifier = Modifier.width(36.dp)
        ) { high ->
            Text(
                text = "$high$unitSuffix",
                style = WeatherTheme.typography.bodyMedium,
                color = WeatherTheme.colors.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TemperatureRangeBar(
    startFraction: Float,
    endFraction: Float,
    modifier: Modifier = Modifier
) {
    val trackColor = WeatherTheme.colors.surfaceElevated
    val activeBrush = Brush.horizontalGradient(
        colors = listOf(
            WeatherTheme.colors.accentSky,
            WeatherTheme.colors.accentSun,
            WeatherTheme.colors.badgeRed
        )
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val strokeWidth = height

        drawLine(
            color = trackColor,
            start = Offset(x = 0f, y = height / 2f),
            end = Offset(x = width, y = height / 2f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        val startX = (width * startFraction).coerceIn(0f, width)
        val endX = (width * endFraction).coerceIn(startX + strokeWidth, width)

        drawLine(
            brush = activeBrush,
            start = Offset(x = startX, y = height / 2f),
            end = Offset(x = endX, y = height / 2f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}