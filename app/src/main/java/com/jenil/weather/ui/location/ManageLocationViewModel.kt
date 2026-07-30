package com.jenil.weather.ui.location

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jenil.weather.data.local.PreferenceKeys.OFFLINE_LAT
import com.jenil.weather.data.local.PreferenceKeys.OFFLINE_LON
import com.jenil.weather.data.local.PreferenceKeys.OFFLINE_NAME
import com.jenil.weather.domain.model.LocationSearchResult
import com.jenil.weather.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageLocationsViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    // Observe favorites from Room database
    val favoriteLocations: StateFlow<List<LocationSearchResult>> = repository.getFavoriteLocations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Observe the currently selected offline location name
    val offlineLocationName: StateFlow<String> = dataStore.data.map { prefs ->
        prefs[OFFLINE_NAME] ?: "None"
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Loading..."
    )

    fun removeFavorite(location: LocationSearchResult) {
        viewModelScope.launch {
            repository.removeFromFavorites(location)
        }
    }

    fun setOfflineLocation(location: LocationSearchResult) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[OFFLINE_LAT] = location.latitude
                prefs[OFFLINE_LON] = location.longitude
                prefs[OFFLINE_NAME] = location.name
            }
            // Trigger a background fetch to immediately cache this new location
            repository.getWeatherData(location.latitude, location.longitude, location.name)
                .onSuccess { weatherData ->
                    repository.cacheOfflineWeather(weatherData)
                }
        }
    }
}