package com.jenil.weather.data.repository

import android.util.Log
import com.jenil.weather.data.remote.RoutingApi
import com.jenil.weather.domain.model.Route
import com.jenil.weather.domain.model.RouteWaypoint
import com.jenil.weather.domain.repository.RouteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class RouteRepositoryImpl @Inject constructor(
    private val routingApi: RoutingApi,
    private val json: Json
) : RouteRepository {

    override suspend fun getRoute(
        startLat: Double, startLon: Double,
        endLat: Double, endLon: Double
    ): Result<Route> = withContext(Dispatchers.IO) {
        try {
            Log.d("RouteRepository", "Fetching route from ($startLat, $startLon) to ($endLat, $endLon)")
            val coordinates = "$startLon,$startLat;$endLon,$endLat"

            val response = routingApi.getRoute(coordinates = coordinates)
            Log.d("RouteRepository", "OSRM Response code: ${response.code}, Routes found: ${response.routes.size}")

            if (response.code != "Ok" || response.routes.isEmpty()) {
                Log.e("RouteRepository", "Invalid route response: ${response.code}")
                return@withContext Result.failure(Exception("Could not find a valid route."))
            }

            val routeDto = response.routes.first()
            val coordsArray = routeDto.geometry.coordinates
            val routeCoordinates = coordsArray.map { it[0] to it[1] }

            val geometryJson = json.encodeToString(routeDto.geometry)
            val featureGeoJson = """
                {
                  "type": "Feature",
                  "properties": {},
                  "geometry": $geometryJson
                }
            """.trimIndent()

            val duration = routeDto.duration
            val distanceKm = routeDto.distance / 1000.0

            val durationBasedIntervals = (duration / 600.0).toInt()
            val distanceBasedIntervals = (distanceKm / 10.0).toInt()
            val numIntervals = maxOf(durationBasedIntervals, distanceBasedIntervals).coerceIn(2, 8)

            Log.d(
                "RouteRepository",
                "duration=${duration}s (${duration / 60}min), distance=${routeDto.distance}m (${"%.1f".format(distanceKm)}km), " +
                        "durationIntervals=$durationBasedIntervals, distanceIntervals=$distanceBasedIntervals, numIntervals=$numIntervals"
            )

            val waypoints = mutableListOf<RouteWaypoint>()

            for (i in 0..numIntervals) {
                val fraction = i.toDouble() / numIntervals

                val coordIndex = (fraction * (coordsArray.size - 1)).toInt()
                val point = coordsArray[coordIndex]

                waypoints.add(
                    RouteWaypoint(
                        longitude = point[0],
                        latitude = point[1],
                        etaOffsetSeconds = (fraction * duration).toLong()
                    )
                )
            }
            Log.d("RouteRepository", "Generated ${waypoints.size} waypoints for weather analysis")

            Result.success(
                Route(
                    geoJsonLineString = featureGeoJson,
                    durationSeconds = duration,
                    distanceMeters = routeDto.distance,
                    waypoints = waypoints,
                    coordinates = routeCoordinates
                )
            )

        } catch (e: Exception) {
            Log.e("RouteRepository", "Error during route calculation", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }
}