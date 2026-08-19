package com.jenil.weather.domain.repository

import com.jenil.weather.domain.model.LifeStyleIndex
import com.jenil.weather.domain.model.WeatherData

interface LifeStyleIndexRepository {
    suspend fun getLifeStyleIndices(weatherData: WeatherData): List<LifeStyleIndex>
}
