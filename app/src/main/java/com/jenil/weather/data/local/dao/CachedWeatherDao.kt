package com.jenil.weather.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jenil.weather.data.local.entity.OfflineWeatherEntity

@Dao
interface CachedWeatherDao {
    // Replace completely overwrites the row with ID 1
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheOfflineWeather(entity: OfflineWeatherEntity)

    @Query("SELECT * FROM offline_cache WHERE id = 1")
    suspend fun getCachedWeather(): OfflineWeatherEntity?

    @Query("DELETE FROM offline_cache")
    suspend fun clearCache()
}