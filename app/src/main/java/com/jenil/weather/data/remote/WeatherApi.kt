package com.jenil.weather.data.remote

import com.jenil.weather.data.remote.dto.AirQualityDto
import com.jenil.weather.data.remote.dto.GeocodingResponseDto
import com.jenil.weather.data.remote.dto.WeatherDto
import com.jenil.weather.data.remote.dto.WindPointDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("v1/forecast")
    suspend fun getWeatherData(
        @Query("latitude") lat: Double = 23.0225,
        @Query("longitude") lon: Double = 72.5714,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,dew_point_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,wind_direction_10m,surface_pressure,visibility",
        @Query("hourly") hourly: String = "is_day,temperature_2m,relative_humidity_2m,dew_point_2m,precipitation_probability,precipitation,rain,weather_code,wind_speed_10m,wind_direction_10m,uv_index",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_sum,moon_phase",
        @Query("timezone") timezone: String = "auto"
    ): WeatherDto

    @GET("https://air-quality-api.open-meteo.com/v1/air-quality")
    suspend fun getAirQualityData(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "us_aqi,pm10,pm2_5,carbon_monoxide,nitrogen_dioxide,ozone"
    ): AirQualityDto

    @GET
    suspend fun searchLocation(
        @retrofit2.http.Url url: String = "https://geocoding-api.open-meteo.com/v1/search",
        @Query("name") query: String,
        @Query("count") count: Int = 5, // Return top 5 results
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponseDto

    @GET("v1/forecast")
    suspend fun getWindGridData(
        @Query("latitude") latitudes: String, // e.g. "23.0,23.5,24.0"
        @Query("longitude") longitudes: String, // e.g. "72.0,72.5,73.0"
        @Query("current") current: String = "wind_speed_10m,wind_direction_10m"
    ): List<WindPointDto>
}