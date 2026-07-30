package com.jenil.weather.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LocationSearchResult(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val state: String // e.g., "Gujarat"
)