package com.jenil.weather.utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class WindVector(
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Double,
    val directionFromDeg: Double
) {
    val directionTowardDeg: Double get() = (directionFromDeg + 180.0) % 360.0
}

object WindStreamlines {

    data class Streamline(
        val points: List<Pair<Double, Double>>,
        val averageSpeedKmh: Double,
    )

    fun trace(
        grid: List<WindVector>,
        seedCount: Int = 45,
        maxStepsTotal: Int = 30,
        stepDegrees: Double = 0.012,
    ): List<Streamline> {
        if (grid.isEmpty()) return emptyList()

        val seeds = pickSeedPoints(grid, seedCount)

        return seeds.mapNotNull { seed ->
            val forwardPoints = mutableListOf<Pair<Double, Double>>()
            val backwardPoints = mutableListOf<Pair<Double, Double>>()
            val speeds = mutableListOf<Double>()

            var lat = seed.latitude
            var lon = seed.longitude
            val halfSteps = maxStepsTotal / 2

            repeat(halfSteps) {
                val vector = interpolateAt(grid, lat, lon) ?: return@repeat
                speeds += vector.speedKmh

                val nextPos = rk4Step(grid, lat, lon, stepDegrees, forward = true)
                    ?: return@repeat
                lat = nextPos.first
                lon = nextPos.second
                forwardPoints += lon to lat
            }

            lat = seed.latitude
            lon = seed.longitude
            repeat(halfSteps) {
                val vector = interpolateAt(grid, lat, lon) ?: return@repeat
                speeds += vector.speedKmh

                val nextPos = rk4Step(grid, lat, lon, stepDegrees, forward = false)
                    ?: return@repeat
                lat = nextPos.first
                lon = nextPos.second
                backwardPoints += lon to lat
            }

            val allPoints = backwardPoints.reversed() + listOf(seed.longitude to seed.latitude) + forwardPoints

            if (allPoints.size < 4) return@mapNotNull null
            Streamline(
                points = allPoints,
                averageSpeedKmh = if (speeds.isNotEmpty()) speeds.average() else 0.0
            )
        }
    }

    /**
     * Classic 4th-order Runge-Kutta integration through the wind field.
     *
     * The previous implementation used a single midpoint (RK2) correction,
     * which is fine where the field is smooth but visibly kinks the
     * streamlines near shear lines and fronts, where direction changes
     * quickly over a short distance. RK4 samples the field four times per
     * step (start, two midpoints, end) and blends them with the standard
     * 1-2-2-1 weighting, which tracks curved flow noticeably more
     * faithfully for the same step size — the practical effect is smoother,
     * more "hand-drawn" looking streamlines that don't visibly bend at
     * every sample point.
     *
     * Returns null if the field can't be sampled at the current position
     * (e.g. the point has drifted outside the available grid).
     */
    private fun rk4Step(
        grid: List<WindVector>,
        lat: Double,
        lon: Double,
        stepSize: Double,
        forward: Boolean,
    ): Pair<Double, Double>? {
        fun velocity(atLat: Double, atLon: Double): Pair<Double, Double>? {
            val vector = interpolateAt(grid, atLat, atLon) ?: return null
            val dir = if (forward) vector.directionTowardDeg
            else (vector.directionTowardDeg + 180.0) % 360.0
            val rad = Math.toRadians(dir)
            val lonScale = cos(Math.toRadians(atLat)).let { if (it == 0.0) 1e-6 else it }
            // Unit heading per degree of lat/lon, direction only (RK weighting
            // applies stepSize once at the end) — matches the original
            // implementation's convention of scaling longitude by cos(lat).
            return cos(rad) to sin(rad) / lonScale
        }

        val k1 = velocity(lat, lon) ?: return null
        val k2 = velocity(lat + stepSize * 0.5 * k1.first, lon + stepSize * 0.5 * k1.second) ?: k1
        val k3 = velocity(lat + stepSize * 0.5 * k2.first, lon + stepSize * 0.5 * k2.second) ?: k1
        val k4 = velocity(lat + stepSize * k3.first, lon + stepSize * k3.second) ?: k1

        val dLat = (k1.first + 2 * k2.first + 2 * k3.first + k4.first) / 6.0
        val dLon = (k1.second + 2 * k2.second + 2 * k3.second + k4.second) / 6.0

        return (lat + stepSize * dLat) to (lon + stepSize * dLon)
    }

    private fun pickSeedPoints(grid: List<WindVector>, count: Int): List<WindVector> {
        if (grid.size <= count) return grid
        val step = grid.size / count
        return grid.filterIndexed { index, _ -> index % step == 0 }.take(count)
    }

    private fun interpolateAt(grid: List<WindVector>, lat: Double, lon: Double): WindVector? {
        val nearest = grid
            .map { it to distanceSquared(it.latitude, it.longitude, lat, lon) }
            .sortedBy { it.second }
            .take(4)

        if (nearest.isEmpty()) return null
        if (nearest.first().second < 1e-9) return nearest.first().first

        var weightSum = 0.0
        var u = 0.0
        var v = 0.0

        nearest.forEach { (vector, distSq) ->
            val weight = 1.0 / distSq
            val radians = Math.toRadians(vector.directionTowardDeg)
            u += weight * vector.speedKmh * sin(radians)
            v += weight * vector.speedKmh * cos(radians)
            weightSum += weight
        }

        if (weightSum == 0.0) return null

        u /= weightSum
        v /= weightSum

        val speed = sqrt(u * u + v * v)
        val direction = Math.toDegrees(atan2(u, v)).let { if (it < 0) it + 360 else it }

        return WindVector(
            latitude = lat,
            longitude = lon,
            speedKmh = speed,
            directionFromDeg = (direction + 180.0).mod(360.0),
        )
    }

    /** Corrects distance calculation for earth's converging longitude lines. */
    private fun distanceSquared(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = lat1 - lat2
        val avgLatRad = Math.toRadians((lat1 + lat2) / 2.0)
        val dLon = (lon1 - lon2) * cos(avgLatRad)
        return (dLat * dLat) + (dLon * dLon)
    }
}