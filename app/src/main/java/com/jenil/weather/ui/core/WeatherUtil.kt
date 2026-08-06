package com.jenil.weather.ui.core

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jenil.weather.R
import com.jenil.weather.domain.model.MoonPhase
import com.jenil.weather.domain.model.WeatherCondition

@Composable
fun WeatherConditionIcon(
    modifier: Modifier = Modifier,
    condition: WeatherCondition,
    isDay: Boolean = true,
    sourceTag: String = "General",
    showBackgroundBadge: Boolean = false
) {

    /* --- COMMENTED OUT LOTTIE IMPLEMENTATION ---
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
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
    ------------------------------------------- */

    // NEW DRAWABLE IMPLEMENTATION
    val drawableRes = when (condition) {
        WeatherCondition.CLEAR -> if (isDay) R.drawable.ic_sunny_clear else R.drawable.ic_night_clear
        WeatherCondition.PARTLY_CLOUDY -> if (isDay) R.drawable.partly_cloudy else R.drawable.partly_cloudy
        WeatherCondition.CLOUDY -> R.drawable.ic_cloudy
        WeatherCondition.RAINY -> R.drawable.ic_rainy
        WeatherCondition.SNOW -> R.drawable.ic_snow
        WeatherCondition.HEAVY_RAIN -> R.drawable.ic_rainy
        WeatherCondition.OVERCAST -> R.drawable.ic_overcast
        WeatherCondition.DRIZZLE -> R.drawable.ic_drizzle
        WeatherCondition.THUNDERSTORM -> R.drawable.ic_thunder
        WeatherCondition.THUNDERSTORM_RAIN -> R.drawable.ic_thunder
        WeatherCondition.THUNDERSTORM_RAIN_HEAVY -> R.drawable.ic_thunder
        else -> R.drawable.ic_sunny_clear // Default drawable
    }

    if (showBackgroundBadge) {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
            contentAlignment = Alignment.Center
        ) {
            if (drawableRes != 0) {
                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    } else {
        if (drawableRes != 0) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = null,
                modifier = modifier
            )
        }
    }
}

@Composable
fun MoonPhaseIcon(
    modifier: Modifier,
    phase: MoonPhase
) {
    val drawableRes = when (phase) {
        MoonPhase.FULL_MOON -> R.drawable.ic_full_moon
        MoonPhase.FIRST_QUARTER -> R.drawable.ic_first_quarter
        MoonPhase.NEW_MOON -> R.drawable.ic_new_moon
        MoonPhase.THIRD_QUARTER -> R.drawable.ic_third_quarter
        MoonPhase.WANING_CRESCENT -> R.drawable.ic_waning_crescent
        MoonPhase.WANING_GIBBOUS -> R.drawable.ic_wanning_gibbous
        MoonPhase.WAXING_CRESCENT -> R.drawable.ic_waxing_crescent
        MoonPhase.WAXING_GIBBOUS -> R.drawable.ic_waxing_gibbous

        else -> R.drawable.ic_full_moon
    }

    if (drawableRes != 0) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = "Moon Phase",
            modifier = modifier
        )
    }
}


fun widgetWeatherIcon(condition: WeatherCondition): Int = when (condition) {
    WeatherCondition.CLEAR -> R.drawable.ic_sunny_clear
    WeatherCondition.PARTLY_CLOUDY -> R.drawable.partly_cloudy
    WeatherCondition.CLOUDY -> R.drawable.ic_cloudy
    WeatherCondition.RAINY -> R.drawable.ic_rainy
    WeatherCondition.SNOW -> R.drawable.ic_snow
    WeatherCondition.HEAVY_RAIN -> R.drawable.ic_rainy
    WeatherCondition.OVERCAST -> R.drawable.ic_overcast
    WeatherCondition.DRIZZLE -> R.drawable.ic_drizzle
    WeatherCondition.THUNDERSTORM -> R.drawable.ic_thunder
    WeatherCondition.THUNDERSTORM_RAIN -> R.drawable.ic_thunder
    WeatherCondition.THUNDERSTORM_RAIN_HEAVY -> R.drawable.ic_thunder
    else -> R.drawable.ic_sunny_clear
}