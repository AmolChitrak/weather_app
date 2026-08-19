package com.jenil.weather.ui.weather

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jenil.weather.data.local.PreferenceKeys
import com.jenil.weather.domain.location.LocationTracker
import com.jenil.weather.domain.repository.LifeStyleIndexRepository
import com.jenil.weather.domain.repository.WeatherRepository
import com.jenil.weather.ui.settings.PressureUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val locationTracker: LocationTracker,
    private val dataStore: DataStore<Preferences>,
    private val lifeStyleIndexRepository: LifeStyleIndexRepository
) : ViewModel() {

    val isOnboardingComplete = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.HAS_COMPLETED_ONBOARDING] ?: false
    }

    val isPrecipitationMm: StateFlow<Boolean> = dataStore.data
        .map { it[PreferenceKeys.IS_PRECIPITATION_MM] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val pressureUnit: StateFlow<PressureUnit> = dataStore.data
        .map { prefs ->
            val raw = prefs[PreferenceKeys.PRESSURE_UNIT]?.uppercase() ?: "HPA"
            runCatching { PressureUnit.valueOf(raw) }.getOrDefault(PressureUnit.HPA)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PressureUnit.HPA)

    val isCelsius: StateFlow<Boolean> = dataStore.data
        .map { it[PreferenceKeys.IS_CELSIUS] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isKmh: StateFlow<Boolean> = dataStore.data
        .map { it[PreferenceKeys.IS_KMH] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private var currentLat: Double = 23.0225
    private var currentLon: Double = 72.5714
    private var currentCity: String = "Ahmedabad"


    private val _uiState = MutableStateFlow(WeatherUiState(isLoading = true))
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun fetchWeatherForCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val prefs = dataStore.data.first()
            val useLocation = prefs[PreferenceKeys.USE_CURRENT_LOCATION] ?: true

            if (useLocation) {
                val location = locationTracker.getCurrentLocation()
                val displayCity = location?.cityName ?: "Current Location"

                if (location != null) {
                    loadWeatherData(location.latitude, location.longitude, displayCity)
                } else {
                    loadWeatherData(23.0225, 72.5714, "Ahmedabad")
                }
            } else {
                loadWeatherData(23.0225, 72.5714, "Ahmedabad")
            }
        }
    }

    fun loadWeatherData(lat: Double, lon: Double, cityName: String) {
        currentLat = lat
        currentLon = lon
        currentCity = cityName

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isAiLoading = true, error = null, isOffline = false) }

            repository.getWeatherData(lat = lat, lon = lon, cityName = cityName)
                .onSuccess { weatherData ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            data = weatherData,
                            error = null,
                            isOffline = false
                        )
                    }

                    val indexes = lifeStyleIndexRepository.getLifeStyleIndices(weatherData)
                    _uiState.update { 
                        it.copy(
                            lifestyleIndexes = indexes,
                            isAiLoading = false 
                        )
                    }
                    
                    updateOfflineCacheInBackground()
                }
                .onFailure { exception ->
                    loadOfflineFallback(exception.localizedMessage ?: "Unknown Error")
                }
        }
    }
    private fun updateOfflineCacheInBackground() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = dataStore.data.first()

                val offlineLat = prefs[PreferenceKeys.OFFLINE_LAT] ?: currentLat
                val offlineLon = prefs[PreferenceKeys.OFFLINE_LON] ?: currentLon
                val offlineName = prefs[PreferenceKeys.OFFLINE_NAME] ?: currentCity

                repository.getWeatherData(offlineLat, offlineLon, offlineName)
                    .onSuccess { fallbackData ->
                        repository.cacheOfflineWeather(fallbackData)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun loadOfflineFallback(networkError: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cachedData = repository.getCachedWeather()

            if (cachedData != null) {
                // 1. Show cached weather immediately
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        data = cachedData,
                        isOffline = true,
                        isAiLoading = true,
                        error = null,
                    )
                }
                
                // 2. Load/Verify AI indexes (might be cached in DataStore already)
                val indexes = lifeStyleIndexRepository.getLifeStyleIndices(cachedData)
                _uiState.update {
                    it.copy(
                        lifestyleIndexes = indexes,
                        isAiLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "You are offline and no cached data is available. ($networkError)"
                    )
                }
            }
        }
    }

    fun refreshWeatherData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadWeatherData(currentLat, currentLon, currentCity)
        }
    }
}