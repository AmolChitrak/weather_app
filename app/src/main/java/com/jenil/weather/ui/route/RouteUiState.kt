package com.jenil.weather.ui.route

import com.jenil.weather.domain.model.HourlyForecast
import com.jenil.weather.domain.model.LocationSearchResult
import com.jenil.weather.domain.model.RouteWaypoint

data class RouteWaypointWeather(
    val waypoint: RouteWaypoint,
    val weather: HourlyForecast?,
    val locationName: String
)

data class RouteWeatherUiState(
    val origin: LocationSearchResult? = null,
    val destination: LocationSearchResult? = null,

    val routeGeoJson: String? = null,
    val waypointsWeather: List<RouteWaypointWeather> = emptyList(),

    val isCalculatingRoute: Boolean = false,
    val isFetchingWeather: Boolean = false,
    val isFetchingInitialLocation: Boolean = true,
    val error: String? = null,

    val currentDeviceLat: Double? = null,
    val currentDeviceLon: Double? = null,

    val routeCoordinates: List<Pair<Double, Double>> = emptyList(),
)