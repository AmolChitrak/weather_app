package com.jenil.weather.ui.core

import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.jenil.weather.domain.model.WeatherCondition
import com.jenil.weather.R

@Composable
fun WeatherConditionIcon(
    condition: WeatherCondition,
    isDay: Boolean = true,
    modifier: Modifier = Modifier,
    sourceTag: String = "General",
    showBackgroundBadge: Boolean = false
) {
    val context = LocalContext.current

    val resId = when (condition) {
        WeatherCondition.CLEAR -> if (isDay) R.raw.anim_clear_day else R.raw.anim_clear_night
        WeatherCondition.PARTLY_CLOUDY -> if (isDay) R.raw.anim_partly_cloudy_day else R.raw.anim_partly_cloudy_night
        WeatherCondition.CLOUDY -> R.raw.anim_cloudy
        WeatherCondition.RAINY -> R.raw.anim_rain
        WeatherCondition.SNOW -> R.raw.anim_snow
        WeatherCondition.HEAVY_RAIN -> R.raw.anim_extreme_rain
        WeatherCondition.OVERCAST -> R.raw.anim_overcast
        WeatherCondition.DRIZZLE -> R.raw.anim_drizzle
        WeatherCondition.THUNDERSTORM -> R.raw.anim_thunderstorms
        WeatherCondition.THUNDERSTORM_RAIN -> R.raw.anim_thunderstorms_rain
        WeatherCondition.THUNDERSTORM_RAIN_HEAVY -> R.raw.anim_thunderstorms_extreme_rain
        else -> R.raw.anim_clear_day
    }

    // Safely extract the file name string from the resources ID
    val fileName = remember(resId) {
        runCatching { context.resources.getResourceEntryName(resId) }.getOrNull() ?: "unknown"
    }

    // Log the clean, readable file name instead of the raw integer ID
    Log.d("WeatherIconTag", "Context: [$sourceTag] -> Condition: $condition -> File: $fileName.json")

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
    if (showBackgroundBadge) {
        // Enclosed in a subtle contrast badge for small views (Hourly / Daily lists)
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(26.dp)
            )
        }
    } else {
        // Unconstrained for the large hero header view
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = modifier
        )
    }
}

