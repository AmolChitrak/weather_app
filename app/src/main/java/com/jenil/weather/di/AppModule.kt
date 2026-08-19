package com.jenil.weather.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.jenil.weather.BuildConfig
import com.jenil.weather.data.local.WeatherDatabase
import com.jenil.weather.data.local.dao.CachedWeatherDao
import com.jenil.weather.data.local.dao.FavoriteLocationDao
import com.jenil.weather.data.remote.RadarMapApi
import com.jenil.weather.data.remote.RoutingApi
import com.jenil.weather.data.remote.WeatherApi
import com.jenil.weather.data.repository.DefaultLocationTracker
import com.jenil.weather.data.repository.LifeStyleIndexRepositoryImpl
import com.jenil.weather.data.repository.RadarRepositoryImpl
import com.jenil.weather.data.repository.RouteRepositoryImpl
import com.jenil.weather.data.repository.WeatherRepositoryImpl
import com.jenil.weather.domain.location.LocationTracker
import com.jenil.weather.domain.repository.LifeStyleIndexRepository
import com.jenil.weather.domain.repository.RadarRepository
import com.jenil.weather.domain.repository.RouteRepository
import com.jenil.weather.domain.repository.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

object AppModule {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_prefs")
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WeatherWidgetEntryPoint {
        fun weatherRepository(): WeatherRepository
    }

    @Provides
    @Singleton
    fun provideWeatherApi(json: Json): WeatherApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(WeatherApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRainViewerApi(json: Json): RadarMapApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://api.rainviewer.com/")
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(RadarMapApi::class.java)
    }
    @Provides
    @Singleton
    fun provideCachedWeatherDao(db: WeatherDatabase): CachedWeatherDao {
        return db.cachedWeatherDao
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(
        api: WeatherApi,
        favoriteDao: FavoriteLocationDao,
        cachedDao: CachedWeatherDao,
        json: Json
    ): WeatherRepository {
        return WeatherRepositoryImpl(api, favoriteDao, cachedDao, json)
    }

    @Provides
    @Singleton
    fun provideRadarRepository(
        api: RadarMapApi
    ): RadarRepository {
        return RadarRepositoryImpl(api)
    }
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(app: Application): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(app)
    }

    @Provides
    @Singleton
    fun provideLocationTracker(
        locationClient: FusedLocationProviderClient,
        app: Application
    ): LocationTracker {
        return DefaultLocationTracker(locationClient, app)
    }

    @Provides
    @Singleton
    fun provideWeatherDatabase(app: Application): WeatherDatabase {
        return Room.databaseBuilder(
                app,
                WeatherDatabase::class.java,
                "weather_db"
        )
            .fallbackToDestructiveMigration(false).build()
    }

    @Provides
    @Singleton
    fun provideFavoriteLocationDao(db: WeatherDatabase): FavoriteLocationDao {
        return db.favoriteLocationDao
    }

    @Provides
    @Singleton
    fun provideDataStore(app: Application): DataStore<Preferences> {
        return app.dataStore
    }


    @Provides
    @Singleton
    @Named("open_weather_api_key")
    fun provideOpenWeatherApiKey(): String {
        return BuildConfig.MAPS_WIND_API_KEY
    }

    @Provides
    @Singleton
    fun provideRoutingApi(json: Json): RoutingApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://router.project-osrm.org/")
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(RoutingApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRouteRepository(
        routingApi: RoutingApi,
        json: Json
    ): RouteRepository {
        return RouteRepositoryImpl(routingApi, json)
    }

    @Provides
    @Singleton
    fun provideLifeStyleIndexRepository(
        generativeModel: GenerativeModel,
        dataStore: DataStore<Preferences>
    ): LifeStyleIndexRepository {
        return LifeStyleIndexRepositoryImpl(
            generativeModel = generativeModel,
            dataStore = dataStore
        )
    }
}