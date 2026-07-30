package com.jenil.weather.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jenil.weather.data.local.dao.CachedWeatherDao
import com.jenil.weather.data.local.dao.FavoriteLocationDao
import com.jenil.weather.data.local.entity.FavoriteLocationEntity
import com.jenil.weather.data.local.entity.OfflineWeatherEntity

@Database(
    entities = [FavoriteLocationEntity::class, OfflineWeatherEntity::class],  // Add other entities here later
    version = 2,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract val favoriteLocationDao: FavoriteLocationDao
    abstract val cachedWeatherDao: CachedWeatherDao
}