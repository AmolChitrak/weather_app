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
    val windSpeed: Double,
    val windDirection: Int, // in degrees (0 - 360)
    val pressure: Double,    // hPa
    val visibility: Double,  // km
    val aqi: Int?,           // US AQI index
    val sunrise: String,
    val sunset: String,
    val sunriseSecondsOfDay: Int = 0,
    val sunsetSecondsOfDay: Int = 0,
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
    val precipitationSum: Double
)