package com.jenil.weather.worker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jenil.weather.data.local.PreferenceKeys
import com.jenil.weather.domain.location.LocationTracker
import com.jenil.weather.domain.repository.WeatherRepository
import com.jenil.weather.utils.WeatherNotificationManager
import com.jenil.weather.utils.getAqiAlertMessage
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@HiltWorker
class WeatherWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: WeatherRepository,
    private val notificationManager: WeatherNotificationManager,
    private val locationTracker: LocationTracker,
    private val dataStore: DataStore<Preferences>
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val WIND_ALERT_THRESHOLD_KMH = 40.0
        private const val TEMP_ALERT_HIGH_C = 40
        private const val TEMP_ALERT_LOW_C = 5
    }

    override suspend fun doWork(): Result {
        return try {
            val prefs = dataStore.data.first()
            val isEnabled = prefs[PreferenceKeys.NOTIFICATIONS_ENABLED] ?: true
            if (!isEnabled) return Result.success()

            val useCurrentLocation = prefs[PreferenceKeys.USE_CURRENT_LOCATION] ?: true
            val liveLocation = if (useCurrentLocation) locationTracker.getCurrentLocation() else null

            val lat: Double
            val lon: Double
            val cityName: String

            if (liveLocation != null) {
                lat = liveLocation.latitude
                lon = liveLocation.longitude
                cityName = liveLocation.cityName ?: "Current Location"
            } else {
                lat = prefs[PreferenceKeys.OFFLINE_LAT] ?: 23.0225
                lon = prefs[PreferenceKeys.OFFLINE_LON] ?: 72.5714
                cityName = prefs[PreferenceKeys.OFFLINE_NAME] ?: "Ahmedabad"
            }

            val result = repository.getWeatherData(lat, lon, cityName)
            if (result.isFailure) return Result.retry()

            val weatherData = result.getOrNull() ?: return Result.failure()

            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

            // 1. Morning Brief (~7-8 AM window)
            val morningEnabled = prefs[PreferenceKeys.MORNING_BRIEF_ENABLED] ?: true
            if (morningEnabled && hour in 7..8 && prefs[PreferenceKeys.LAST_MORNING_BRIEF_DATE] != today) {
                notificationManager.showMorningBrief(
                    "Morning Weather Brief ($cityName)",
                    "Current temperature is ${weatherData.temperature}°C with ${weatherData.condition}. Have a wonderful day!"
                )
                dataStore.edit { it[PreferenceKeys.LAST_MORNING_BRIEF_DATE] = today }
            }

            // 2. Evening Brief (~7-8 PM window)
            val eveningEnabled = prefs[PreferenceKeys.EVENING_BRIEF_ENABLED] ?: true
            if (eveningEnabled && hour in 19..20 && prefs[PreferenceKeys.LAST_EVENING_BRIEF_DATE] != today) {
                notificationManager.showEveningBrief(
                    "Evening Weather Update",
                    "The night temperature is settling around ${weatherData.temperature}°C. Check out tomorrow's outlook in the app."
                )
                dataStore.edit { it[PreferenceKeys.LAST_EVENING_BRIEF_DATE] = today }
            }

            // 3. AQI Alerts
            val aqiEnabled = prefs[PreferenceKeys.AQI_ALERTS_ENABLED] ?: true
            val currentAqi = weatherData.aqi
            if (aqiEnabled && currentAqi != null && currentAqi > 100 && prefs[PreferenceKeys.LAST_AQI_ALERT_DATE] != today) {
                notificationManager.showAqiAlert(
                   getAqiAlertMessage(currentAqi)
                )
                dataStore.edit { it[PreferenceKeys.LAST_AQI_ALERT_DATE] = today }
            }

            // 4. Rain Alerts — honors RAIN_LEAD_TIME_MINUTES
            val rainEnabled = prefs[PreferenceKeys.RAIN_ALERTS_ENABLED] ?: true
            if (rainEnabled) {
                val leadTimeMinutes = prefs[PreferenceKeys.RAIN_LEAD_TIME_MINUTES] ?: 30
                val rainIndexed = weatherData.hourlyForecast.withIndex()
                    .firstOrNull { (_, hourly) -> hourly.precipitationProbability > 60 }

                if (rainIndexed != null) {
                    val (index, rainHour) = rainIndexed
                    val minutesUntilRain = (index * 60) - minute
                    val rainEventKey = "$today-${rainHour.time}"
                    val alreadyAlerted = prefs[PreferenceKeys.LAST_RAIN_ALERT_KEY] == rainEventKey

                    if (!alreadyAlerted && minutesUntilRain in 1..leadTimeMinutes) {
                        notificationManager.showRainAlert(
                            "Rain expected around ${rainHour.time} in $cityName — about $minutesUntilRain min away. You might want to grab an umbrella!"
                        )
                        dataStore.edit { it[PreferenceKeys.LAST_RAIN_ALERT_KEY] = rainEventKey }
                    }
                }
            }

            // 5. Wind Alerts
            val windEnabled = prefs[PreferenceKeys.WIND_ALERTS_ENABLED] ?: true
            if (windEnabled &&
                weatherData.windSpeed > WIND_ALERT_THRESHOLD_KMH &&
                prefs[PreferenceKeys.LAST_WIND_ALERT_DATE] != today
            ) {
                notificationManager.showWindAlert(
                    "High winds expected in $cityName — gusts up to ${weatherData.windSpeed.toInt()} km/h. Secure loose outdoor items."
                )
                dataStore.edit { it[PreferenceKeys.LAST_WIND_ALERT_DATE] = today }
            }

            // 6. Temperature Alerts (extreme heat or cold)
            val tempAlertEnabled = prefs[PreferenceKeys.TEMPERATURE_ALERTS_ENABLED] ?: true
            if (tempAlertEnabled && prefs[PreferenceKeys.LAST_TEMPERATURE_ALERT_DATE] != today) {
                val temp = weatherData.temperature
                when {
                    temp >= TEMP_ALERT_HIGH_C -> {
                        notificationManager.showTemperatureAlert(
                            "Extreme Heat Warning",
                            "It's ${temp}°C in $cityName. Stay hydrated and avoid prolonged sun exposure."
                        )
                        dataStore.edit { it[PreferenceKeys.LAST_TEMPERATURE_ALERT_DATE] = today }
                    }
                    temp <= TEMP_ALERT_LOW_C -> {
                        notificationManager.showTemperatureAlert(
                            "Cold Weather Advisory",
                            "It's ${temp}°C in $cityName. Dress warmly if you're heading out."
                        )
                        dataStore.edit { it[PreferenceKeys.LAST_TEMPERATURE_ALERT_DATE] = today }
                    }
                }
            }

            // 7. Smart Recommendations — fires when the relevant tip category changes
            val recommendationsEnabled = prefs[PreferenceKeys.SMART_RECOMMENDATIONS_ENABLED] ?: true
            if (recommendationsEnabled) {
                val uvIndex = weatherData.hourlyForecast.firstOrNull()?.uvIndex ?: 0.0
                val aqi = weatherData.aqi
                val condition = weatherData.condition
                val temp = weatherData.temperature
                val windSpeed = weatherData.windSpeed

                val rainyConditions = setOf(
                    com.jenil.weather.domain.model.WeatherCondition.HEAVY_RAIN,
                    com.jenil.weather.domain.model.WeatherCondition.RAINY,
                    com.jenil.weather.domain.model.WeatherCondition.DRIZZLE,
                    com.jenil.weather.domain.model.WeatherCondition.THUNDERSTORM,
                    com.jenil.weather.domain.model.WeatherCondition.THUNDERSTORM_RAIN,
                    com.jenil.weather.domain.model.WeatherCondition.THUNDERSTORM_RAIN_HEAVY
                )
                val pleasantConditions = setOf(
                    com.jenil.weather.domain.model.WeatherCondition.CLEAR,
                    com.jenil.weather.domain.model.WeatherCondition.PARTLY_CLOUDY
                )

                // Evaluate in priority order — first match wins
                val recommendation: Pair<String, String>? = when {
                    uvIndex >= 8.0 -> "UV_VERY_HIGH" to
                            "UV index is very high (%.1f) in $cityName. Wear sunscreen, sunglasses, and a hat if you're heading out.".format(uvIndex)
                    uvIndex >= 6.0 -> "UV_HIGH" to
                            "UV index is high (%.1f) today. Consider sunscreen if you'll be outside for a while.".format(uvIndex)

                    aqi != null && aqi in 51..100 -> "AQI_MODERATE" to
                            "Air quality is moderate (AQI $aqi) in $cityName. Sensitive groups may want to limit prolonged outdoor exertion."

                    condition in rainyConditions -> "CLOTHING_RAIN" to
                            "Rain's in the forecast for $cityName — grab a jacket or umbrella before heading out."
                    temp <= 10 -> "CLOTHING_COLD" to
                            "It's chilly (${temp}°C) in $cityName — dress warmly today."
                    temp >= 35 -> "CLOTHING_HOT" to
                            "It's hot (${temp}°C) in $cityName — light, breathable clothing recommended."

                    temp in 18..28 && windSpeed < 20.0 && condition in pleasantConditions &&
                            (aqi == null || aqi <= 50) -> "ACTIVITY_GOOD" to
                            "Pleasant conditions in $cityName (${temp}°C, ${condition}) — a great day for outdoor activities!"

                    else -> null
                }

                if (recommendation != null) {
                    val (category, message) = recommendation
                    val recommendationKey = "$today:$category"
                    if (prefs[PreferenceKeys.LAST_RECOMMENDATION_KEY] != recommendationKey) {
                        notificationManager.showSmartRecommendation(message)
                        dataStore.edit { it[PreferenceKeys.LAST_RECOMMENDATION_KEY] = recommendationKey }
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}