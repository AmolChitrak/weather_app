package com.jenil.weather.domain.model



enum class MapLayerType(val displayName: String) {
    RADAR("Radar"),
    WIND("Wind")
}

data class MapLayerConfig(
    val selectedLayer: MapLayerType = MapLayerType.RADAR,
    val openWeatherApiKey: String = ""
)