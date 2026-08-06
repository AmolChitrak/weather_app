package com.jenil.weather.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WeatherData(
    val cityName: String,
    val date: String,
    val temperature: Int,
    val apparentTemperature: Int,
    val condition: WeatherCondition,
    val highTemp: Int,
    val lowTemp: Int,
    val humidity: Int,
    val dewPoint: Int,
    val windSpeed: Double,
    val windDirection: Int, // in degrees (0 - 360)
    val pressure: Double,    // hPa
    val visibility: Double,  // km
    val aqi: Int?,           // US AQI index
    val sunrise: String,
    val sunset: String,
    val sunriseSecondsOfDay: Int = 0,
    val sunsetSecondsOfDay: Int = 0,
    val moonPhase: MoonPhase = MoonPhase.NEW_MOON,
    val hourlyForecast: List<HourlyForecast>,
    val dailyForecast: List<DailyForecast>
)
@Serializable
enum class WeatherCondition {
    CLEAR,
    CLOUDY,
    PARTLY_CLOUDY,
    OVERCAST,
    HEAVY_RAIN,
    RAINY,
    DRIZZLE,
    THUNDERSTORM,
    THUNDERSTORM_RAIN,
    THUNDERSTORM_RAIN_HEAVY,
    SNOW
}
@Serializable
data class HourlyForecast(
    val time: String,
    val isDay: Boolean,
    val temperature: Int,
    val dewPoint: Int = 0,
    val condition: WeatherCondition,
    val precipitationProbability: Int,
    val uvIndex: Double,
    val sunEventTime: String? = null,
    val isSunsetEvent: Boolean = false
)
@Serializable
data class DailyForecast(
    val day: String,
    val highTemp: Int,
    val lowTemp: Int,
    val condition: WeatherCondition,
    val uvIndexMax: Double,
    val precipitationSum: Double,
    val moonPhase: MoonPhase = MoonPhase.NEW_MOON
)

@Serializable
enum class MoonPhase(val title: String, val description: String) {
    NEW_MOON("New Moon", "New cycle begins"),
    WAXING_CRESCENT("Waxing Crescent", "Visible crescent"),
    FIRST_QUARTER("First Quarter", "Half illuminated"),
    WAXING_GIBBOUS("Waxing Gibbous", "Growing brighter"),
    FULL_MOON("Full Moon", "Fully illuminated"),
    WANING_GIBBOUS("Waning Gibbous", "Slowly fading"),
    THIRD_QUARTER("Third Quarter", "Half fading"),
    WANING_CRESCENT("Waning Crescent", "Thin sliver");

    companion object {
        fun fromValue(value: Double): MoonPhase {
            return when (value) {
                in 0.0..0.02, in 0.98..1.0 -> NEW_MOON
                in 0.03..0.22 -> WAXING_CRESCENT
                in 0.23..0.27 -> FIRST_QUARTER
                in 0.28..0.47 -> WAXING_GIBBOUS
                in 0.48..0.52 -> FULL_MOON
                in 0.53..0.72 -> WANING_GIBBOUS
                in 0.73..0.77 -> THIRD_QUARTER
                in 0.78..0.97 -> WANING_CRESCENT
                else -> NEW_MOON
            }
        }
    }
}