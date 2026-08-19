    package com.jenil.weather.data.repository

    import androidx.datastore.core.DataStore
    import androidx.datastore.preferences.core.Preferences
    import androidx.datastore.preferences.core.edit
    import com.google.ai.client.generativeai.GenerativeModel
    import com.jenil.weather.data.local.PreferenceKeys
    import com.jenil.weather.domain.model.IndexCategory
    import com.jenil.weather.domain.model.IndexLevel
    import com.jenil.weather.domain.model.LifeStyleIndex
    import com.jenil.weather.domain.model.WeatherData
    import com.jenil.weather.domain.repository.LifeStyleIndexRepository
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.flow.first
    import kotlinx.coroutines.withContext
    import kotlinx.serialization.json.Json
    import javax.inject.Inject

    class LifeStyleIndexRepositoryImpl @Inject constructor(
        private val generativeModel: GenerativeModel,
        private val dataStore: DataStore<Preferences>
    ) : LifeStyleIndexRepository {

        companion object {
            private const val PROMPT_VERSION = 3
        }
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        override suspend fun getLifeStyleIndices(weatherData: WeatherData): List<LifeStyleIndex> = withContext(
            Dispatchers.IO) {

            val prefs = dataStore.data.first()
            val lastFetched = prefs[PreferenceKeys.AI_LAST_FETCHED] ?: 0L
            val cachedJson = prefs[PreferenceKeys.AI_CACHED_JSON]
            val cachedCity = prefs[PreferenceKeys.AI_CACHED_CITY]
            val cachedPromptVersion = prefs[PreferenceKeys.AI_PROMPT_VERSION] ?: -1
            val currentTime = System.currentTimeMillis()

            // 1. Check the cached data expiry , availability etc
            if (currentTime - lastFetched < 3_600_000L &&
                cachedCity == weatherData.cityName &&
                cachedPromptVersion == PROMPT_VERSION &&
                !cachedJson.isNullOrEmpty()) {
                try {
                    return@withContext json.decodeFromString<List<LifeStyleIndex>>(cachedJson)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 2. Fetch fresh data

            val todayDaily = weatherData.dailyForecast.firstOrNull()
            val uvIndex = todayDaily?.uvIndexMax ?: 0.0

            val prompt = """
                You are a meteorological lifestyle expert writing SHORT advice for small mobile cards.
                Weather in ${weatherData.cityName}:
                - Temperature: ${weatherData.temperature}°C (Feels like ${weatherData.apparentTemperature}°C)
                - Condition: ${weatherData.condition.name}
                - Humidity: ${weatherData.humidity}%
                - Wind Speed: ${weatherData.windSpeed} km/h
                - Visibility: ${weatherData.visibility} km
                - Max UV Index Today: $uvIndex

                Generate contextual daily advice for four categories: SKINCARE, DRIVING, CLOTHING, and OUTDOOR_ACTIVITY.

                STRICT LENGTH RULES (the text is displayed on a small card and gets cut off if too long):
                - "title": max 3 words. No punctuation.
                - "recommendation": ONE sentence, max 15 words. State the risk AND the action together — no filler, no scene-setting, no "despite the..." preambles.

                Good recommendation (15 words): "Low humidity dries skin fast — apply a rich moisturizer with SPF before heading out."
                Bad recommendation (too long, don't do this): "Despite the overcast skies, the high UV index poses a significant threat of skin damage as rays penetrate through thick clouds, so make sure to apply sunscreen generously."

                Return the result STRICTLY as a JSON array matching this exact schema, with no extra text or markdown formatting:
                [
                  { "category": "SKINCARE", "title": "Short catchy title", "level": "LOW", "recommendation": "One short sentence, max 16 words." },
                  { "category": "DRIVING", "title": "Short catchy title", "level": "MODERATE", "recommendation": "One short sentence, max 16 words." },
                  { "category": "CLOTHING", "title": "Short catchy title", "level": "LOW", "recommendation": "One short sentence, max 16 words." },
                  { "category": "OUTDOOR_ACTIVITY", "title": "Short catchy title", "level": "LOW", "recommendation": "One short sentence, max 16 words." }
                ]
                Note: "category" must be one of SKINCARE, DRIVING, CLOTHING, OUTDOOR_ACTIVITY. "level" must be one of LOW, MODERATE, HIGH, EXTREME.
            """.trimIndent()

            try {
                val response = generativeModel.generateContent(prompt)
                val jsonString = response.text?.trim() ?: "[]"

                val cleanedJson = jsonString.removeSurrounding("```json", "```").trim()

                val freshIndices = json.decodeFromString<List<LifeStyleIndex>>(cleanedJson)

                dataStore.edit { preferences ->
                    preferences[PreferenceKeys.AI_LAST_FETCHED] = currentTime
                    preferences[PreferenceKeys.AI_CACHED_JSON] = json.encodeToString(freshIndices)
                    preferences[PreferenceKeys.AI_CACHED_CITY] = weatherData.cityName
                    preferences[PreferenceKeys.AI_PROMPT_VERSION] = PROMPT_VERSION
                }

                return@withContext freshIndices
            } catch (e: Exception) {
                e.printStackTrace()
                if (!cachedJson.isNullOrEmpty()) {
                    try {
                        return@withContext json.decodeFromString<List<LifeStyleIndex>>(cachedJson)
                    } catch (parseError: Exception) {
                        parseError.printStackTrace()
                    }
                }

                listOf(
                    LifeStyleIndex(
                        category = IndexCategory.SKINCARE,
                        title = "Skincare: Standard",
                        level = IndexLevel.LOW,
                        recommendation = "AI insights unavailable. Maintain your regular daily skincare routine."
                    ),
                    LifeStyleIndex(
                        category = IndexCategory.DRIVING,
                        title = "Driving: Normal",
                        level = IndexLevel.LOW,
                        recommendation = "AI insights unavailable. Exercise standard caution on the road."
                    ),
                    LifeStyleIndex(
                        category = IndexCategory.CLOTHING,
                        title = "Clothing: Standard",
                        level = IndexLevel.LOW,
                        recommendation = "AI insights unavailable. Dress according to your comfort."
                    ),
                    LifeStyleIndex(
                        category = IndexCategory.OUTDOOR_ACTIVITY,
                        title = "Activity: Standard",
                        level = IndexLevel.LOW,
                        recommendation = "AI insights unavailable. Plan your activities as per your schedule."
                    )
                )
            }
        }
    }
