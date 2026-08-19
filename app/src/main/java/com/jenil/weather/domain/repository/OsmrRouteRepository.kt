package com.jenil.weather.domain.repository

import com.jenil.weather.domain.model.Route

interface RouteRepository {
    suspend fun getRoute(
        startLat: Double, startLon: Double,
        endLat: Double, endLon: Double
    ): Result<Route>
}