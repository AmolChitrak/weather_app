package com.jenil.weather.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jenil.weather.data.repository.RecentSearchesRepository
import com.jenil.weather.domain.location.LocationTracker
import com.jenil.weather.domain.model.LocationSearchResult
import com.jenil.weather.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@HiltViewModel
class WeatherSearchViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val recentSearchesRepository: RecentSearchesRepository,
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<LocationSearchResult>>(emptyList())
    val searchResults: StateFlow<List<LocationSearchResult>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val recentSearches: StateFlow<List<LocationSearchResult>> = recentSearchesRepository.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Location States
    private val _isFetchingLocation = MutableStateFlow(false)
    val isFetchingLocation = _isFetchingLocation.asStateFlow()

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError = _locationError.asStateFlow()


    // 3. Recent Search Actions
    fun addRecentSearch(result: LocationSearchResult) {
        viewModelScope.launch {
            recentSearchesRepository.addRecent(result)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            recentSearchesRepository.clearAll()
        }
    }

    fun removeRecentSearch(id: Int) {
        viewModelScope.launch {
            recentSearchesRepository.removeOne(id.toString())
        }
    }

    // 4. Current Location Action
    fun fetchCurrentLocation(onSuccess: (lat: Double, lon: Double, name: String) -> Unit) {
        viewModelScope.launch {
            _isFetchingLocation.value = true
            _locationError.value = null

            val location = locationTracker.getCurrentLocation()

            _isFetchingLocation.value = false

            if (location != null) {
                onSuccess(location.latitude, location.longitude, location.cityName ?: "My Location")
            } else {
                _locationError.value = "Unable to retrieve location. Please check GPS and permissions."
            }
        }
    }


    val favoriteLocations: StateFlow<List<LocationSearchResult>> = weatherRepository
        .getFavoriteLocations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        _searchQuery
            .debounce(300.milliseconds)
            .filter { it.isNotBlank() }
            .distinctUntilChanged()
            .onEach { query ->
                performSearch(query)
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            weatherRepository.searchLocation(query)
                .onSuccess { results ->
                    _searchResults.value = results
                }
                .onFailure {
                    _searchResults.value = emptyList()
                }
            _isLoading.value = false
        }
    }

    fun toggleFavorite(location: LocationSearchResult, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyFavorite) {
                weatherRepository.removeFromFavorites(location)
            } else {
                weatherRepository.saveToFavorites(location)
            }
        }
    }
}