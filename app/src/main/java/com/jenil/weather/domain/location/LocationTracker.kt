package com.jenil.weather.domain.location

interface LocationTracker {
    suspend fun getCurrentLocation(): Location?
}

data class Location(
    val latitude: Double,
    val longitude: Double,
    val cityName: String? = null
)