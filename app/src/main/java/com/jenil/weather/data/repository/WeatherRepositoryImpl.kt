package com.jenil.weather.data.repository

import com.jenil.weather.data.local.dao.CachedWeatherDao
import com.jenil.weather.data.local.dao.FavoriteLocationDao
import com.jenil.weather.data.local.entity.OfflineWeatherEntity
import com.jenil.weather.data.remote.WeatherApi
import com.jenil.weather.data.remote.dto.toFavoriteEntity
import com.jenil.weather.data.remote.dto.toLocationSearchResult
import com.jenil.weather.data.remote.dto.toWeatherData
import com.jenil.weather.domain.model.LocationSearchResult
import com.jenil.weather.domain.model.WeatherData
import com.jenil.weather.domain.repository.WeatherRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi,
    private val favoriteDao: FavoriteLocationDao,
    private val cachedDao: CachedWeatherDao,
    private val json: Json
) : WeatherRepository {

    override fun getFavoriteLocations(): Flow<List<LocationSearchResult>> {
        return favoriteDao.getAllFavorites().map { entities ->
            entities.map { it.toLocationSearchResult() }
        }
    }

    override suspend fun saveToFavorites(location: LocationSearchResult) {
        favoriteDao.insertFavorite(location.toFavoriteEntity())
    }

    override suspend fun removeFromFavorites(location: LocationSearchResult) {
        favoriteDao.deleteFavorite(location.toFavoriteEntity())
    }

    override suspend fun isLocationFavorite(id: Int): Boolean {
        return favoriteDao.isLocationFavorite(id)
    }

    override suspend fun getWeatherData(lat: Double, lon: Double, cityName: String): Result<WeatherData> {
        return try {
            // Fetch weather and air quality concurrently for better performance
            coroutineScope {
                val weatherDeferred = async { api.getWeatherData(lat = lat, lon = lon) }
                val airQualityDeferred = async {
                    runCatching { api.getAirQualityData(lat = lat, lon = lon) }.getOrNull()
                }

                val weatherResponse = weatherDeferred.await()
                val airQualityResponse = airQualityDeferred.await()

                val weatherData = weatherResponse.toWeatherData(cityName = cityName, airQualityDto = airQualityResponse)
                Result.success(weatherData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun searchLocation(query: String): Result<List<LocationSearchResult>> {
        return try {
            val response = api.searchLocation(query = query)
            val results = response.results?.map { it.toLocationSearchResult() } ?: emptyList()
            Result.success(results)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun cacheOfflineWeather(weatherData: WeatherData) {
        val weatherJson = json.encodeToString(weatherData)
        val entity = OfflineWeatherEntity(
            locationName = weatherData.cityName,
            weatherDataJson = weatherJson
        )
        cachedDao.cacheOfflineWeather(entity)
    }

    override suspend fun getCachedWeather(): WeatherData? {
        return try {
            val entity = cachedDao.getCachedWeather()
            if (entity != null) {
                json.decodeFromString<WeatherData>(entity.weatherDataJson)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun clearCache() {
        cachedDao.clearCache()
    }
}