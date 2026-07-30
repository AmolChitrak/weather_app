package com.jenil.weather.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jenil.weather.data.local.PreferenceKeys
import com.jenil.weather.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

data class SettingsUiState(
    val isCelsius: Boolean = true,
    val isKmh: Boolean = true,
    val isDarkTheme: Boolean = false,
    val useCurrentLocation: Boolean = true,
    val isPrecipitationMm: Boolean = true,
    val pressureUnit: PressureUnit = PressureUnit.HPA,
    val fallbackLocationName: String = "Current Location",
    val isNotificationsEnabled: Boolean = true,
    val isMorningBriefEnabled: Boolean = true,
    val isEveningBriefEnabled: Boolean = true,
    val isRainAlertsEnabled: Boolean = true,
    val rainLeadTimeMinutes: Int = 30,
    val isAqiAlertsEnabled: Boolean = true,
    val isWindAlertsEnabled: Boolean = true,
    val isTemperatureAlertsEnabled: Boolean = true,
    val isSmartRecommendationsEnabled: Boolean = true
)

enum class PressureUnit(val label: String) {
    HPA("hPa"),
    INHG("inHg"),
    MBAR("mbar")
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val repository: WeatherRepository
) : ViewModel() {

    private fun Preferences.toUiState(): SettingsUiState {
        val rawPressure = this[PreferenceKeys.PRESSURE_UNIT]?.uppercase() ?: PressureUnit.HPA.name
        val safePressure = runCatching { PressureUnit.valueOf(rawPressure) }.getOrDefault(PressureUnit.HPA)

        return SettingsUiState(
            isCelsius = this[PreferenceKeys.IS_CELSIUS] ?: true,
            isKmh = this[PreferenceKeys.IS_KMH] ?: true,
            isDarkTheme = this[PreferenceKeys.IS_DARK_THEME] ?: false,
            isPrecipitationMm = this[PreferenceKeys.IS_PRECIPITATION_MM] ?: true,
            pressureUnit = safePressure,
            useCurrentLocation = this[PreferenceKeys.USE_CURRENT_LOCATION] ?: true,
            fallbackLocationName = this[PreferenceKeys.OFFLINE_NAME] ?: "Current Location",
            isNotificationsEnabled = this[PreferenceKeys.NOTIFICATIONS_ENABLED] ?: true,
            isMorningBriefEnabled = this[PreferenceKeys.MORNING_BRIEF_ENABLED] ?: true,
            isEveningBriefEnabled = this[PreferenceKeys.EVENING_BRIEF_ENABLED] ?: true,
            isRainAlertsEnabled = this[PreferenceKeys.RAIN_ALERTS_ENABLED] ?: true,
            rainLeadTimeMinutes = this[PreferenceKeys.RAIN_LEAD_TIME_MINUTES] ?: 30,
            isAqiAlertsEnabled = this[PreferenceKeys.AQI_ALERTS_ENABLED] ?: true,
            isWindAlertsEnabled = this[PreferenceKeys.WIND_ALERTS_ENABLED] ?: true,
            isTemperatureAlertsEnabled = this[PreferenceKeys.TEMPERATURE_ALERTS_ENABLED] ?: true,
            isSmartRecommendationsEnabled = this[PreferenceKeys.SMART_RECOMMENDATIONS_ENABLED] ?: true,
        )
    }

    val uiState: StateFlow<SettingsUiState> = dataStore.data
        .map { it.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking { dataStore.data.first().toUiState() }
        )

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { dataStore.edit { it[PreferenceKeys.NOTIFICATIONS_ENABLED] = enabled } }
    }

    fun updateSmartRecommendationsEnabled(enabled: Boolean) {
        viewModelScope.launch { dataStore.edit { it[PreferenceKeys.SMART_RECOMMENDATIONS_ENABLED] = enabled } }
    }
    fun updateMorningBriefEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[PreferenceKeys.MORNING_BRIEF_ENABLED] = enabled }
        }
    }

    fun updateEveningBriefEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[PreferenceKeys.EVENING_BRIEF_ENABLED] = enabled }
        }
    }

    fun updateRainAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[PreferenceKeys.RAIN_ALERTS_ENABLED] = enabled }
        }
    }

    fun updateRainLeadTime(minutes: Int) {
        viewModelScope.launch {
            dataStore.edit { it[PreferenceKeys.RAIN_LEAD_TIME_MINUTES] = minutes }
        }
    }

    fun updateAqiAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[PreferenceKeys.AQI_ALERTS_ENABLED] = enabled }
        }
    }

    fun updateWindAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[PreferenceKeys.WIND_ALERTS_ENABLED] = enabled }
        }
    }

    fun updateTemperatureAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[PreferenceKeys.TEMPERATURE_ALERTS_ENABLED] = enabled }
        }
    }

    fun updatePressureUnit(unit: PressureUnit) {
        viewModelScope.launch { dataStore.edit { it[PreferenceKeys.PRESSURE_UNIT] = unit.name } }
    }

    fun updatePrecipitationUnit(isMm: Boolean) {
        viewModelScope.launch { dataStore.edit { it[PreferenceKeys.IS_PRECIPITATION_MM] = isMm } }
    }

    fun updateTemperatureUnit(isCelsius: Boolean) {
        viewModelScope.launch { dataStore.edit { it[PreferenceKeys.IS_CELSIUS] = isCelsius } }
    }

    fun updateWindSpeedUnit(isKmh: Boolean) {
        viewModelScope.launch { dataStore.edit { it[PreferenceKeys.IS_KMH] = isKmh } }
    }

    fun updateTheme(isDarkTheme: Boolean) {
        viewModelScope.launch { dataStore.edit { it[PreferenceKeys.IS_DARK_THEME] = isDarkTheme } }
    }

    fun updateLocationPreference(useCurrentLocation: Boolean) {
        viewModelScope.launch { dataStore.edit { it[PreferenceKeys.USE_CURRENT_LOCATION] = useCurrentLocation } }
    }

    fun updateFallbackLocation(lat: Double, lon: Double, name: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[PreferenceKeys.OFFLINE_LAT] = lat
                prefs[PreferenceKeys.OFFLINE_LON] = lon
                prefs[PreferenceKeys.OFFLINE_NAME] = name
            }
        }
    }

    fun clearCachedData() {
        viewModelScope.launch {
            repository.clearCache()
        }
    }
}