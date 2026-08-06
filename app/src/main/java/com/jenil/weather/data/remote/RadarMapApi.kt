package com.jenil.weather.data.remote

import com.jenil.weather.data.remote.dto.RainViewerResponseDto
import retrofit2.http.GET

interface RadarMapApi {
    @GET("https://api.rainviewer.com/public/weather-maps.json")
    suspend fun getRadarMaps(): RainViewerResponseDto
}