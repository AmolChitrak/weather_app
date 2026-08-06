package com.jenil.weather.domain.repository

import com.jenil.weather.domain.model.RadarMetaData

interface RadarRepository {
    suspend fun getRadarMetaData(): Result<RadarMetaData>
}