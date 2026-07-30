package com.jenil.weather.domain.repository

import com.jenil.weather.domain.model.LocationSearchResult
import com.jenil.weather.domain.model.WeatherData
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun getWeatherData(lat: Double, lon: Double, cityName: String): Result<WeatherData>

    suspend fun searchLocation(query: String): Result<List<LocationSearchResult>>

    fun getFavoriteLocations(): Flow<List<LocationSearchResult>>
    suspend fun saveToFavorites(location: LocationSearchResult)
    suspend fun removeFromFavorites(location: LocationSearchResult)
    suspend fun isLocationFavorite(id: Int): Boolean


    suspend fun cacheOfflineWeather(weatherData: WeatherData)
    suspend fun getCachedWeather(): WeatherData?
    suspend fun clearCache()
}