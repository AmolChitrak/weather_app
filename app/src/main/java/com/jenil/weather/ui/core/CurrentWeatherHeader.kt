package com.jenil.weather.ui.core

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jenil.weather.data.remote.dto.toDisplayName
import com.jenil.weather.domain.model.WeatherData
import kotlin.math.roundToInt

@Composable
fun CurrentWeatherHeader(
    weatherData: WeatherData,
    isCelsius: Boolean,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ---- Unit conversion ----
    val targetTemp = if (isCelsius) weatherData.temperature else (weatherData.temperature * 9f / 5f + 32f).roundToInt()
    val targetHigh = if (isCelsius) weatherData.highTemp else (weatherData.highTemp * 9f / 5f + 32f).roundToInt()
    val targetLow = if (isCelsius) weatherData.lowTemp else (weatherData.lowTemp * 9f / 5f + 32f).roundToInt()
    val unitString = if (isCelsius) "°" else "°"
    val unitSuffix = if (isCelsius) "C" else "F"

    // ---- Animated values ----
    val animatedTemp by animateIntAsState(
        targetValue = targetTemp,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "temp_anim"
    )
    val animatedHighTemp by animateIntAsState(
        targetValue = targetHigh,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "high_temp_anim"
    )
    val animatedLowTemp by animateIntAsState(
        targetValue = targetLow,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "low_temp_anim"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val settingsScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "settings_scale"
    )

    Column(modifier = modifier.fillMaxWidth()) {

        // ---- Top bar: location + settings ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = weatherData.cityName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = weatherData.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                )
            }

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = settingsScale
                        scaleY = settingsScale
                    }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                IconButton(
                    onClick = onSettingsClick,
                    interactionSource = interactionSource
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ---- Hero section ----
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Icon inside a soft tonal circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                WeatherConditionIcon(
                    condition = weatherData.condition,
                    modifier = Modifier.size(100.dp),
                    sourceTag = "CurrentWeatherHeader"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Temperature with animated crossfade
            AnimatedContent(
                targetState = animatedTemp,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(150)) },
                label = "temp_crossfade"
            ) { temp ->
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "$temp",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "$unitString$unitSuffix",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = weatherData.condition.toDisplayName(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // High / Low pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HighLowChip(
                    icon = Icons.Outlined.ArrowUpward,
                    value = animatedHighTemp,
                    unit = "$unitString$unitSuffix"
                )
                HighLowChip(
                    icon = Icons.Outlined.ArrowDownward,
                    value = animatedLowTemp,
                    unit = "$unitString$unitSuffix"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun HighLowChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Int,
    unit: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$value$unit",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}