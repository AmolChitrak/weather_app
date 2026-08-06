package com.jenil.weather.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider
import androidx.glance.unit.ColorProvider

object AtmosphereColors {
    val surfaceLight = Color(0xE6FFFFFF)
    val surfaceDark = Color(0xE61B1E22)

    val textPrimaryLight = Color(0xFF111418)
    val textPrimaryDark = Color(0xFFFFFFFF)

    val mutedTextLight = Color(0xFF6B7076)
    val mutedTextDark = Color(0xFF8A8F98)

    // Semantic Weather Accents
    val skyAccent = Color(0xFF4FC3F7)
    val sunAccent = Color(0xFFFFC24B)
    val rainAccent = Color(0xFF5B8DEF)
    val heatAccent = Color(0xFFEF4444)
    val stormAccent = Color(0xFF9C27B0)
    val fogAccent = Color(0xFF9E9E9E)
}

@Composable
fun cardSurfaceColor(): ColorProvider =
    ColorProvider(day = AtmosphereColors.surfaceLight, night = AtmosphereColors.surfaceDark)

@Composable
fun primaryTextColor(): ColorProvider =
    ColorProvider(day = AtmosphereColors.textPrimaryLight, night = AtmosphereColors.textPrimaryDark)

@Composable
 fun mutedTextColor(): ColorProvider =
    ColorProvider(day = AtmosphereColors.mutedTextLight, night = AtmosphereColors.mutedTextDark)

@Composable
 fun accentColor(color: Color): ColorProvider =
    ColorProvider(day = color, night = color)

@Composable
fun thermalColor(temperatureC: Int): ColorProvider {
    val color = when {
        temperatureC >= 30 -> AtmosphereColors.heatAccent
        temperatureC >= 22 -> AtmosphereColors.sunAccent
        temperatureC >= 10 -> AtmosphereColors.skyAccent
        else -> AtmosphereColors.rainAccent
    }
    return accentColor(color)
}

@Composable
fun semanticConditionColor(condition: String): ColorProvider {
    val lowerCondition = condition.lowercase()
    val color = when {
        "rain" in lowerCondition || "drizzle" in lowerCondition -> AtmosphereColors.rainAccent
        "snow" in lowerCondition || "ice" in lowerCondition -> AtmosphereColors.skyAccent
        "storm" in lowerCondition || "thunder" in lowerCondition -> AtmosphereColors.stormAccent
        "fog" in lowerCondition || "mist" in lowerCondition -> AtmosphereColors.fogAccent
        "clear" in lowerCondition || "sun" in lowerCondition -> AtmosphereColors.sunAccent
        else -> AtmosphereColors.mutedTextLight // Mostly Cloudy / Neutral
    }
    return ColorProvider(day = color, night = color)
}