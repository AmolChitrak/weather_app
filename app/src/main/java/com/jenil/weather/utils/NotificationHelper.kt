package com.jenil.weather.utils

fun getAqiAlertMessage(aqi: Int): String = when (aqi) {
    in 101..150 ->
        "AQI is $aqi (Unhealthy for Sensitive Groups). Reduce prolonged outdoor activity."

    in 151..200 ->
        "AQI is $aqi (Unhealthy). Limit outdoor activity when possible."

    in 201..300 ->
        "AQI is $aqi (Very Unhealthy). Stay indoors if possible."

    in 301..500 ->
        "AQI is $aqi (Hazardous). Avoid outdoor exposure."

    else ->
        "AQI is $aqi. Air quality is hazardous."
}