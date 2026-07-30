package com.jenil.weather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_cache")
data class OfflineWeatherEntity(
    @PrimaryKey val id: Int = 1, // Hardcoded to 1 so it always overwrites the same row
    val locationName: String,
    val weatherDataJson: String, // Use Gson or Kotlinx.Serialization to store the object
    val lastUpdated: Long = System.currentTimeMillis()
)