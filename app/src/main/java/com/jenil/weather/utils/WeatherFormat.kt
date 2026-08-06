package com.jenil.weather.utils

import androidx.compose.ui.graphics.Color
import com.jenil.weather.ui.settings.PressureUnit

fun getWindDirectionString(degrees: Int): String {
    val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
    val index = ((degrees + 11.25) / 22.5).toInt() % 16
    return directions[index]
}

fun getUvDescription(uv: Double): String {
    return when {
        uv < 3.0 -> "Low risk"
        uv < 6.0 -> "Moderate risk"
        uv < 8.0 -> "High risk"
        uv < 11.0 -> "Very high risk"
        else -> "Extreme danger"
    }
}
fun getUvColorScale(): List<Color> = listOf(
    Color(0xFF4CAF50),   // Green (Low)
    Color(0xFFFBC02D),   // Yellow (Moderate)
    Color(0xFFFF9800),   // Orange (High)
    Color(0xFFE53935),   // Red (Very High)
    Color(0xFF9C27B0)    // Purple (Extreme)
)


fun getUvColor(uv: Double): Color = when {
    uv < 3.0 -> Color(0xFF4CAF50)   // Green (Low)
    uv < 6.0 -> Color(0xFFFBC02D)   // Yellow (Moderate)
    uv < 8.0 -> Color(0xFFFF9800)   // Orange (High)
    uv < 11.0 -> Color(0xFFE53935)  // Red (Very High)
    else -> Color(0xFF9C27B0)       // Purple (Extreme)
}


fun getUvProgress(uv: Double): Float {
    return (uv.toFloat() / 12f).coerceIn(0f, 1f)
}

fun getAqiDescription(aqi: Int?): String {
    if (aqi == null) return "Data unavailable"
    return when {
        aqi <= 50 -> "Good"
        aqi <= 100 -> "Moderate"
        aqi <= 150 -> "Sensitive groups"
        aqi <= 200 -> "Unhealthy"
        aqi <= 300 -> "Very unhealthy"
        else -> "Hazardous"
    }
}

fun getAqiColor(aqi: Int?): Color? {
    if (aqi == null) return null
    return when {
        aqi <= 50 -> Color(0xFF4CAF50)    // Green (Good)
        aqi <= 100 -> Color(0xFFFBC02D)   // Yellow (Moderate)
        aqi <= 150 -> Color(0xFFFF9800)   // Orange (Sensitive Groups)
        aqi <= 200 -> Color(0xFFE53935)   // Red (Unhealthy)
        aqi <= 300 -> Color(0xFF9C27B0)   // Purple (Very Unhealthy)
        else -> Color(0xFF7B1FA2)         // Maroon (Hazardous)
    }
}

fun getAqiColorScale(): List<Color> = listOf(
    Color(0xFF4CAF50),   // Green (Good)
    Color(0xFFFBC02D),   // Yellow (Moderate)
    Color(0xFFFF9800),   // Orange (Sensitive Groups)
    Color(0xFFE53935),   // Red (Unhealthy)
    Color(0xFF9C27B0),   // Purple (Very Unhealthy)
    Color(0xFF7B1FA2)    // Maroon (Hazardous)
)
fun getAqiProgress(aqi: Int?): Float {
    if (aqi == null) return 0f
    return (aqi.toFloat() / 300f).coerceIn(0f, 1f)
}

fun getPressureDescription(hPa: Double): String {
    return when {
        hPa < 1009 -> "Low pressure"
        hPa > 1022 -> "High pressure"
        else -> "Normal"
    }
}


fun getVisibilityDescription(km: Double): String {
    return when {
        km >= 10.0 -> "Clear view"
        km >= 4.0 -> "Moderate haze"
        else -> "Poor visibility"
    }
}


fun getFeelsLikeDescription(actual: Int, apparent: Int): String {
    val diff = apparent - actual
    return when {
        diff >= 2 -> "Feels warmer than actual"
        diff <= -2 -> "Feels cooler than actual"
        else -> "Similar to actual"
    }
}

fun formatPrecipitation(mm: Double, isPrecipitationMm: Boolean): String {
    return if (isPrecipitationMm) {
        "%.1f mm".format(mm)
    } else {
        val inches = mm * 0.0393701
        "%.2f in".format(inches)
    }
}


fun formatPressure(hpa: Double, unit: PressureUnit): String {
    return when (unit) {
        PressureUnit.HPA -> "${hpa.toInt()} hPa"
        PressureUnit.MBAR -> "${hpa.toInt()} mbar" // 1 hPa = 1 mbar
        PressureUnit.INHG -> {
            val inHg = hpa * 0.02953
            "%.2f inHg".format(inHg)
        }
    }
}

fun getDewPointDescription(dewPointCelsius: Int): String {
    return when {
        dewPointCelsius < 10 -> "Very dry air"
        dewPointCelsius <= 15 -> "Comfortable"
        dewPointCelsius <= 20 -> "Humid"
        else -> "Very muggy"
    }
}
