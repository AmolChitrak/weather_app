package com.jenil.weather.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherDto(
    @SerialName("current")
    val current: CurrentWeatherDto,
    @SerialName("hourly")
    val hourly: HourlyWeatherDto,
    @SerialName("daily")
    val daily: DailyWeatherDto
)

@Serializable
data class CurrentWeatherDto(
    @SerialName("temperature_2m")
    val temperature: Double,
    @SerialName("apparent_temperature")
    val apparentTemperature: Double,
    @SerialName("relative_humidity_2m")
    val humidity: Int,
    @SerialName("precipitation")
    val precipitation: Double,
    @SerialName("weather_code")
    val weatherCode: Int,
    @SerialName("wind_speed_10m")
    val windSpeed: Double,
    @SerialName("wind_direction_10m")
    val windDirection: Int,
    @SerialName("surface_pressure")
    val surfacePressure: Double,
    @SerialName("visibility")
    val visibility: Double,
    @SerialName("dew_point_2m")
    val dewPoint2m: Double? = null
)

@Serializable
data class HourlyWeatherDto(
    val time: List<String>,
    @SerialName("is_day")
    val isDay: List<Int>? = null,
    @SerialName("temperature_2m")
    val temperatures: List<Double>,
    @SerialName("relative_humidity_2m")
    val humidities: List<Int>,
    @SerialName("precipitation_probability")
    val precipitationProbabilities: List<Int>,
    val precipitation: List<Double>,
    val rain: List<Double>,
    @SerialName("weather_code")
    val weatherCodes: List<Int>,
    @SerialName("wind_speed_10m")
    val windSpeeds: List<Double>,
    @SerialName("wind_direction_10m")
    val windDirections: List<Int>,
    @SerialName("uv_index")
    val uvIndices: List<Double>,
    @SerialName("dew_point_2m")
    val dewPoint2m: List<Double>? = null
)

@Serializable
data class DailyWeatherDto(
    val time: List<String>,
    val sunrise: List<String>? = null,
    val sunset: List<String>? = null,
    @SerialName("weather_code")
    val weatherCodes: List<Int>,
    @SerialName("temperature_2m_max")
    val maxTemperatures: List<Double>,
    @SerialName("temperature_2m_min")
    val minTemperatures: List<Double>,
    @SerialName("uv_index_max")
    val maxUvIndices: List<Double>,
    @SerialName("precipitation_sum")
    val precipitationSums: List<Double>,
    @SerialName("moon_phase")
    val moonPhase: List<Double>? = null
)

@Serializable
data class AirQualityDto(
    @SerialName("current")
    val current: CurrentAirQualityDto
)

@Serializable
data class CurrentAirQualityDto(
    @SerialName("us_aqi")
    val usAqi: Int?,
    @SerialName("pm2_5")
    val pm25: Double?,
    @SerialName("pm10")
    val pm10: Double?,
    @SerialName("carbon_monoxide")
    val carbonMonoxide: Double?,
    @SerialName("nitrogen_dioxide")
    val nitrogenDioxide: Double?,
    @SerialName("ozone")
    val ozone: Double?
)