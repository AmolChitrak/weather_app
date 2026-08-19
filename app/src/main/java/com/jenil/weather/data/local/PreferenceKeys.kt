package com.jenil.weather.data.local


import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey


object PreferenceKeys {

    // WEATHER UNIT KEY
    val IS_CELSIUS = booleanPreferencesKey("is_celsius")
    val IS_KMH = booleanPreferencesKey("is_kmh")
    val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    val USE_CURRENT_LOCATION = booleanPreferencesKey("use_current_location")
    val IS_PRECIPITATION_MM = booleanPreferencesKey("is_precip_mm")
    val PRESSURE_UNIT = stringPreferencesKey("pressure_unit")

    // ONBOARDING KEY
    val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")

    // AI CACHING KEYS
    val AI_LAST_FETCHED = longPreferencesKey("ai_last_fetched")
    val AI_CACHED_JSON = stringPreferencesKey("ai_cached_json")
    val AI_CACHED_CITY = stringPreferencesKey("ai_cached_city")
    val AI_PROMPT_VERSION = intPreferencesKey("ai_prompt_version")

    // LOCATION CACHING KEYS
    val OFFLINE_LAT = doublePreferencesKey("offline_lat")
    val OFFLINE_LON = doublePreferencesKey("offline_lon")
    val OFFLINE_NAME = stringPreferencesKey("offline_name")

    // NOTIFICATION KEYS
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val MORNING_BRIEF_ENABLED = booleanPreferencesKey("morning_brief_enabled")
    val EVENING_BRIEF_ENABLED = booleanPreferencesKey("evening_brief_enabled")
    val RAIN_ALERTS_ENABLED = booleanPreferencesKey("rain_alerts_enabled")
    val RAIN_LEAD_TIME_MINUTES = intPreferencesKey("rain_lead_time_minutes")
    val AQI_ALERTS_ENABLED = booleanPreferencesKey("aqi_alerts_enabled")
    val WIND_ALERTS_ENABLED = booleanPreferencesKey("wind_alerts_enabled")
    val TEMPERATURE_ALERTS_ENABLED = booleanPreferencesKey("temperature_alerts_enabled")
    val LAST_AQI_ALERT_DATE = stringPreferencesKey("last_aqi_alert_date")
    val LAST_RAIN_ALERT_KEY = stringPreferencesKey("last_rain_alert_key")
    val LAST_MORNING_BRIEF_DATE = stringPreferencesKey("last_morning_brief_date")
    val LAST_EVENING_BRIEF_DATE = stringPreferencesKey("last_evening_brief_date")
    val LAST_WIND_ALERT_DATE = stringPreferencesKey("last_wind_alert_date")
    val LAST_TEMPERATURE_ALERT_DATE = stringPreferencesKey("last_temperature_alert_date")
    val SMART_RECOMMENDATIONS_ENABLED = booleanPreferencesKey("smart_recommendations_enabled")
    val LAST_RECOMMENDATION_KEY = stringPreferencesKey("last_recommendation_key")
}
