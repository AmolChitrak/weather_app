package com.jenil.weather.utils

fun zoomForSpan(spanDegrees: Double): Double = when {
    spanDegrees > 20 -> 4.0
    spanDegrees > 10 -> 5.0
    spanDegrees > 5 -> 6.0
    spanDegrees > 2.5 -> 7.0
    spanDegrees > 1.2 -> 8.0
    spanDegrees > 0.6 -> 9.0
    spanDegrees > 0.3 -> 10.0
    spanDegrees > 0.15 -> 11.0
    spanDegrees > 0.07 -> 12.0
    spanDegrees > 0.035 -> 13.0
    spanDegrees > 0.018 -> 14.0
    else -> 15.0
}