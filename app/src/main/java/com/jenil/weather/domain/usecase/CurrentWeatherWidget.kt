package com.jenil.weather.domain.usecase

import com.jenil.weather.domain.model.WeatherData
import com.jenil.weather.domain.repository.WeatherRepository
import javax.inject.Inject

class GetWidgetWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(lat: Double, lon: Double, cityName: String): Result<WeatherData> {
        return try {
            // 1. Try fetching fresh data from remote/repository
            val result = repository.getWeatherData(lat, lon, cityName)
            if (result.isSuccess) {
                val data = result.getOrThrow()
                // 2. Cache it successfully for the widget to read offline
                repository.cacheOfflineWeather(data)
                Result.success(data)
            } else {
                // 3. Fallback to cached data if network fails
                val cached = repository.getCachedWeather()
                if (cached != null) {
                    Result.success(cached)
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            }
        } catch (e: Exception) {
            // 4. Ultimate fallback to local cache on crash/exception
            val cached = repository.getCachedWeather()
            if (cached != null) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }
}