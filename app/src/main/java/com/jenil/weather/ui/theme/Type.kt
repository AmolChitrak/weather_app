package com.jenil.weather.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

private val WeatherFont = FontFamily.Default

// Standard Material3 slots — used by default components (buttons, app bars, etc.)
val WeatherTypography = Typography(
    headlineLarge = TextStyle(     // screen titles: "Daily Weather", "Weather Insight"
        fontFamily = WeatherFont,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(    // sub-page titles: "Forecast Report"
        fontFamily = WeatherFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(       // card titles: "Nearby Locations", city names
        fontFamily = WeatherFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(         // condition text: "Rainy - Cloudy"
        fontFamily = WeatherFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(        // subtitles: "The Official Weather App"
        fontFamily = WeatherFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(        // small-caps stat labels: "WIND", "HUMIDITY"
        fontFamily = WeatherFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.8.sp,
        lineHeight = 14.sp
    )
)

// Not part of Material3's default slots — the hero "22°" needs to go bigger
// than displayLarge's typical use case, so it's exposed separately and applied
// directly where the big temperature is rendered.
object WeatherExtraType {
    val tempDisplay = TextStyle(
        fontFamily = WeatherFont,
        fontWeight = FontWeight.Bold,
        fontSize = 64.sp,
        lineHeight = 64.sp,
        letterSpacing = (-2).sp
    )
    val tempDisplayCompact = TextStyle( // used in smaller cards (nearby locations, chips)
        fontFamily = WeatherFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.5).sp
    )
    val graphValueBubble = TextStyle(   // the "23°" callout on the trend graph
        fontFamily = WeatherFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        textAlign = TextAlign.Center
    )
}