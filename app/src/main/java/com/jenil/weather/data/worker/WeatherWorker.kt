package com.jenil.weather.data.worker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.ai.client.generativeai.GenerativeModel
import com.jenil.weather.data.local.PreferenceKeys
import com.jenil.weather.domain.location.LocationTracker
import com.jenil.weather.domain.repository.WeatherRepository
import com.jenil.weather.utils.WeatherNotificationManager
import com.jenil.weather.utils.getAqiAlertMessage
import com.jenil.weather.widget.WeatherAppWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@HiltWorker
class WeatherWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: WeatherRepository,
    private val notificationManager: WeatherNotificationManager,
    private val locationTracker: LocationTracker,
    private val generativeModel: GenerativeModel,
    private val dataStore: DataStore<Preferences>
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val WIND_ALERT_THRESHOLD_KMH = 40.0
        private const val TEMP_ALERT_HIGH_C = 40
        private const val TEMP_ALERT_LOW_C = 5
    }

    private fun formatTemp(temp: Int, isCelsius: Boolean): String {
        return if (isCelsius) {
            "$temp°C"
        } else {
            val f = (temp * 1.8 + 32).roundToInt()
            "$f°F"
        }
    }

    private fun formatWind(speedKmh: Double, isKmh: Boolean): String {
        return if (isKmh) {
            "${speedKmh.toInt()} km/h"
        } else {
            val mph = (speedKmh * 0.621371).toInt()
            "$mph mph"
        }
    }

    private suspend fun generateAiBrief(
        prompt: String,
        fallbackMessage: String
    ): String {
        return try {
            val response = generativeModel.generateContent(
                """
            You are an expert weather assistant crafting a mobile notification.
            Instructions:
            - Write exactly 1 to 2 clear, engaging, conversational sentences.
            - Keep it under 140 characters so it fits on Android lock screens.
            - Output format must be JSON: {"message": "your text here"}
            
            Context: $prompt
            """.trimIndent()
            )

            val rawText = response.text?.trim() ?: return fallbackMessage


            try {
                val jsonObject = JSONObject(rawText)
                val firstKey = jsonObject.keys().asSequence().firstOrNull()
                if (firstKey != null) {
                    jsonObject.getString(firstKey)
                } else {
                    rawText
                }
            } catch (e: Exception) {
                e.printStackTrace()

                rawText
                    .replace(Regex("""^\{|\}$"""), "")
                    .substringAfter(":")
                    .trim()
                    .removeSurrounding("\"")
                    .takeIf { it.isNotBlank() } ?: rawText
            }
        } catch (e: Exception) {
            e.printStackTrace()
            fallbackMessage
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val prefs = dataStore.data.first()
            val isEnabled = prefs[PreferenceKeys.NOTIFICATIONS_ENABLED] ?: true
            if (!isEnabled) return Result.success()

            val isCelsius = prefs[PreferenceKeys.IS_CELSIUS] ?: true
            val isKmh = prefs[PreferenceKeys.IS_KMH] ?: true

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

            // --- MORNING BRIEF ---
            val morningEnabled = prefs[PreferenceKeys.MORNING_BRIEF_ENABLED] ?: true
            if (morningEnabled && hour in 7..8 && prefs[PreferenceKeys.LAST_MORNING_BRIEF_DATE] != today) {

                val maxRainChance = weatherData.hourlyForecast.take(14).maxOfOrNull { it.precipitationProbability } ?: 0
                val peakUv = weatherData.hourlyForecast.take(14).maxOfOrNull { it.uvIndex } ?: 0.0

                val currentFormatted = formatTemp(weatherData.temperature, isCelsius)
                val highFormatted = formatTemp(weatherData.highTemp, isCelsius)
                val lowFormatted = formatTemp(weatherData.lowTemp, isCelsius)

                val summaryText = "$currentFormatted • High: $highFormatted • Rain: $maxRainChance%"
                val fallback = "Current temperature is $currentFormatted with ${weatherData.condition}. Have a wonderful day!"

                val aiPrompt = """
                    City: $cityName
                    Current: $currentFormatted, ${weatherData.condition}
                    Today's Range: High $highFormatted / Low $lowFormatted
                    Peak Rain Chance: $maxRainChance%
                    Peak UV: $peakUv, AQI: ${weatherData.aqi ?: "N/A"}
                    Write a crisp morning brief focusing on today's high, any rain risk, and what to wear/bring.
                """.trimIndent()

                val message = generateAiBrief(aiPrompt, fallback)

                notificationManager.showMorningBrief(
                    title = "Morning Weather Brief ($cityName)",
                    summaryText = summaryText,
                    fullMessage = message
                )
                dataStore.edit { it[PreferenceKeys.LAST_MORNING_BRIEF_DATE] = today }
            }

            // --- EVENING BRIEF ---
            val eveningEnabled = prefs[PreferenceKeys.EVENING_BRIEF_ENABLED] ?: true
            if (eveningEnabled && hour in 19..20 && prefs[PreferenceKeys.LAST_EVENING_BRIEF_DATE] != today) {
                val tomorrow = weatherData.dailyForecast.getOrNull(1)

                val tomHighFormatted = tomorrow?.highTemp?.let { formatTemp(it, isCelsius) } ?: "--"
                val tomLowFormatted = tomorrow?.lowTemp?.let { formatTemp(it, isCelsius) } ?: "--"
                val currentFormatted = formatTemp(weatherData.temperature, isCelsius)

                val summaryText = "Tomorrow: High $tomHighFormatted / Low $tomLowFormatted"
                val fallback = "The night temperature is settling around $currentFormatted. Check out tomorrow's outlook in the app."

                val aiPrompt = """
                    City: $cityName
                    Tonight: $currentFormatted, ${weatherData.condition}
                    Tomorrow: High $tomHighFormatted / Low $tomLowFormatted, ${tomorrow?.condition?.name ?: "similar"}
                    Precipitation sum: ${tomorrow?.precipitationSum ?: 0.0} mm
                    Write an evening wind-down summary that previews tomorrow's weather so the user knows what to expect in the morning.
                """.trimIndent()

                val message = generateAiBrief(aiPrompt, fallback)

                notificationManager.showEveningBrief(
                    title = "Evening Weather Update",
                    summaryText = summaryText,
                    fullMessage = message
                )
                dataStore.edit { it[PreferenceKeys.LAST_EVENING_BRIEF_DATE] = today }
            }

            // --- AQI ALERTS ---
            val aqiEnabled = prefs[PreferenceKeys.AQI_ALERTS_ENABLED] ?: true
            val currentAqi = weatherData.aqi
            if (aqiEnabled && currentAqi != null && currentAqi > 100 && prefs[PreferenceKeys.LAST_AQI_ALERT_DATE] != today) {
                val fallback = getAqiAlertMessage(currentAqi)
                val aiPrompt = "City: $cityName. Air Quality Index (AQI) is $currentAqi. Provide a very brief, urgent health warning regarding outdoor activities."

                val message = generateAiBrief(aiPrompt, fallback)

                notificationManager.showAqiAlert(message)
                dataStore.edit { it[PreferenceKeys.LAST_AQI_ALERT_DATE] = today }
            }

            // --- RAIN ALERTS ---
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
                        val fallback = "Rain expected around ${rainHour.time} in $cityName — about $minutesUntilRain min away. You might want to grab an umbrella!"
                        val aiPrompt = "City: $cityName. Rain is starting in exactly $minutesUntilRain minutes (around ${rainHour.time}). Provide a quick, urgent, and helpful alert to grab an umbrella or stay dry."

                        val message = generateAiBrief(aiPrompt, fallback)

                        notificationManager.showRainAlert(message)
                        dataStore.edit { it[PreferenceKeys.LAST_RAIN_ALERT_KEY] = rainEventKey }
                    }
                }
            }

            // --- WIND ALERTS ---
            val windEnabled = prefs[PreferenceKeys.WIND_ALERTS_ENABLED] ?: true
            if (windEnabled &&
                weatherData.windSpeed > WIND_ALERT_THRESHOLD_KMH &&
                prefs[PreferenceKeys.LAST_WIND_ALERT_DATE] != today
            ) {
                val windFormatted = formatWind(weatherData.windSpeed, isKmh)
                val fallback = "High winds expected in $cityName — gusts up to $windFormatted. Secure loose outdoor items."
                val aiPrompt = "City: $cityName. High winds detected at $windFormatted. Provide a brief safety warning to secure loose outdoor items or be careful driving."

                val message = generateAiBrief(aiPrompt, fallback)

                notificationManager.showWindAlert(message)
                dataStore.edit { it[PreferenceKeys.LAST_WIND_ALERT_DATE] = today }
            }

            // --- TEMPERATURE ALERTS ---
            val tempAlertEnabled = prefs[PreferenceKeys.TEMPERATURE_ALERTS_ENABLED] ?: true
            if (tempAlertEnabled && prefs[PreferenceKeys.LAST_TEMPERATURE_ALERT_DATE] != today) {
                val temp = weatherData.temperature
                val tempFormatted = formatTemp(temp, isCelsius)
                when {
                    temp >= TEMP_ALERT_HIGH_C -> {
                        val fallback = "It's $tempFormatted in $cityName. Stay hydrated and avoid prolonged sun exposure."
                        val aiPrompt = "City: $cityName. Extreme heat warning. Current temperature is $tempFormatted. Provide a brief, urgent safety tip about hydration or avoiding sun exposure."
                        val message = generateAiBrief(aiPrompt, fallback)

                        notificationManager.showTemperatureAlert("Extreme Heat Warning", message)
                        dataStore.edit { it[PreferenceKeys.LAST_TEMPERATURE_ALERT_DATE] = today }
                    }
                    temp <= TEMP_ALERT_LOW_C -> {
                        val fallback = "It's $tempFormatted in $cityName. Dress warmly if you're heading out."
                        val aiPrompt = "City: $cityName. Cold weather advisory. Current temperature is $tempFormatted. Provide a brief, urgent tip about dressing warmly or staying indoors."
                        val message = generateAiBrief(aiPrompt, fallback)

                        notificationManager.showTemperatureAlert("Cold Weather Advisory", message)
                        dataStore.edit { it[PreferenceKeys.LAST_TEMPERATURE_ALERT_DATE] = today }
                    }
                }
            }

            // --- SMART RECOMMENDATIONS ---
            val recommendationsEnabled = prefs[PreferenceKeys.SMART_RECOMMENDATIONS_ENABLED] ?: true
            if (recommendationsEnabled) {
                val uvIndex = weatherData.hourlyForecast.firstOrNull()?.uvIndex ?: 0.0
                val aqi = weatherData.aqi
                val condition = weatherData.condition
                val temp = weatherData.temperature
                val windSpeed = weatherData.windSpeed
                
                val category = when {
                    uvIndex >= 6.0 -> "UV"
                    aqi != null && aqi > 100 -> "AQI"
                    temp >= 35 -> "HEAT"
                    temp <= 10 -> "COLD"
                    windSpeed > 30 -> "WIND"
                    else -> "GENERAL"
                }

                val recommendationKey = "$today:$category"
                if (prefs[PreferenceKeys.LAST_RECOMMENDATION_KEY] != recommendationKey) {
                    val tempFormatted = formatTemp(temp, isCelsius)
                    val windFormatted = formatWind(windSpeed, isKmh)
                    val fallback = "Current temperature is $tempFormatted in $cityName. Stay prepared for ${condition.name.lowercase()} conditions today."
                    val aiPrompt = "City: $cityName. Temp: $tempFormatted, Condition: $condition, UV: $uvIndex, AQI: $aqi, Wind: $windFormatted. Provide an actionable recommendation regarding outdoor activity, hydration, clothing, or skin protection."

                    val message = generateAiBrief(aiPrompt, fallback)
                    notificationManager.showSmartRecommendation(message)
                    dataStore.edit { it[PreferenceKeys.LAST_RECOMMENDATION_KEY] = recommendationKey }
                }
            }

            WeatherAppWidget().updateAll(context)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}