package com.jenil.weather.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeocodingResponseDto(
    val results: List<LocationDto>? = null // Can be null if the search yields no results
)

@Serializable
data class LocationDto(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null // This is usually the state/province
)