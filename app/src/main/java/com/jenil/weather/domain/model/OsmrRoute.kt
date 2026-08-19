package com.jenil.weather.domain.model

data class Route(
    val geoJsonLineString: String,
    val durationSeconds: Double,
    val distanceMeters: Double,
    val waypoints: List<RouteWaypoint>,
    val coordinates: List<Pair<Double, Double>>
)

data class RouteWaypoint(
    val latitude: Double,
    val longitude: Double,
    val etaOffsetSeconds: Long
)