package com.jenil.weather.data.remote.dto



import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WindPointDto(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWindDataDto
)

@Serializable
data class CurrentWindDataDto(
    @SerialName("wind_speed_10m")
    val windSpeed: Double,
    @SerialName("wind_direction_10m")
    val windDirection: Int
)