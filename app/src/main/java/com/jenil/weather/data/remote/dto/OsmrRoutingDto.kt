package com.jenil.weather.data.remote.dto



import kotlinx.serialization.Serializable

@Serializable
data class OsrmRouteResponseDto(
    val code: String,
    val routes: List<OsrmRouteDto>
)

@Serializable
data class OsrmRouteDto(
    val geometry: OsrmGeometryDto,
    val duration: Double,
    val distance: Double
)

@Serializable
data class OsrmGeometryDto(
    val coordinates: List<List<Double>>,
    val type: String
)