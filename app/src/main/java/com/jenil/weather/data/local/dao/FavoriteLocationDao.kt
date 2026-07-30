package com.jenil.weather.data.local.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jenil.weather.data.local.entity.FavoriteLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteLocationDao {

    @Query("SELECT * FROM favorite_locations")
    fun getAllFavorites(): Flow<List<FavoriteLocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(location: FavoriteLocationEntity)

    @Delete
    suspend fun deleteFavorite(location: FavoriteLocationEntity)

    @Query("SELECT EXISTS(SELECT * FROM favorite_locations WHERE id = :id)")
    suspend fun isLocationFavorite(id: Int): Boolean
}