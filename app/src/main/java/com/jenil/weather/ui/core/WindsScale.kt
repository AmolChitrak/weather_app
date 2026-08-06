package com.jenil.weather.ui.core

import androidx.compose.ui.graphics.Color

/**
 * Single source of truth for the wind-speed → color/width mapping.
 *
 * Previously WeatherMapScreen hardcoded its own color stops for the
 * LineLayer while WindLegend hardcoded a *second*, separately-maintained
 * set of colors/labels. They happened to agree today, but any future edit
 * to one would silently desync from the other. Centralizing them here
 * means the rendered streamlines and the legend are always describing
 * the same thing.
 */
object WindScale {

    data class Stop(
        val speedKmh: Double,
        val color: Color,
        val label: String,
    )

    /** Roughly Beaufort-scale buckets, in km/h. */
    val stops = listOf(
        Stop(0.0, Color(0xFF3B82F6), "Calm"),    // 0–10  km/h
        Stop(10.0, Color(0xFF10B981), "Light"),  // 10–20 km/h
        Stop(20.0, Color(0xFFF59E0B), "Strong"), // 20–35 km/h
        Stop(35.0, Color(0xFFEF4444), "Gale"),   // 35+   km/h
    )

    /** Line-width stops (km/h → dp), thinner for calm air, bolder for gale-force. */
    val widthStops = listOf(
        0.0 to 1.5,
        15.0 to 2.5,
        30.0 to 3.5,
    )

    /** Line-opacity stops — calm streamlines fade back, strong wind pops. */
    val opacityStops = listOf(
        0.0 to 0.45f,
        20.0 to 0.85f,
    )

    fun stopFor(speedKmh: Double): Stop =
        stops.lastOrNull { speedKmh >= it.speedKmh } ?: stops.first()

    /** Human-readable range for a stop, e.g. "10–20 km/h", for legend display. */
    fun rangeLabel(index: Int): String {
        val lower = stops[index].speedKmh.toInt()
        val upper = stops.getOrNull(index + 1)?.speedKmh?.toInt()
        return if (upper != null) "$lower–$upper" else "$lower+"
    }
}