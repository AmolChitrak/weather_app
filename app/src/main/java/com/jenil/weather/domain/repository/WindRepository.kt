package com.jenil.weather.domain.repository

import com.jenil.weather.domain.model.MapLayerType

interface WindRepository {
    fun getTileUrl(layerType: MapLayerType): String?
    suspend fun fetchWindGridGeoJson(latitude: Double, longitude: Double, zoom: Double): String
}