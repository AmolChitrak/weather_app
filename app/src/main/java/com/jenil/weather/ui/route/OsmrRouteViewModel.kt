package com.jenil.weather.ui.route

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jenil.weather.domain.location.LocationTracker
import com.jenil.weather.domain.model.HourlyForecast
import com.jenil.weather.domain.model.LocationSearchResult
import com.jenil.weather.domain.model.RouteWaypoint
import com.jenil.weather.domain.repository.RouteRepository
import com.jenil.weather.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class RouteWeatherViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
    private val weatherRepository: WeatherRepository,
    private val locationTracker: LocationTracker,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val CURRENT_LOCATION_ID = -1
    }

    private val _uiState = MutableStateFlow(RouteWeatherUiState())
    val uiState: StateFlow<RouteWeatherUiState> = _uiState.asStateFlow()

    init {
        fetchInitialCameraLocation()
    }

    private fun fetchInitialCameraLocation() {
        viewModelScope.launch {
            val location = withTimeoutOrNull(10.seconds) {
                locationTracker.getCurrentLocation()
            }

            if (location != null) {
                _uiState.update {
                    it.copy(
                        currentDeviceLat = location.latitude,
                        currentDeviceLon = location.longitude,
                        isFetchingInitialLocation = false
                    )
                }
            } else {
                _uiState.update { it.copy(isFetchingInitialLocation = false) }
            }
        }
    }

    fun clearOrigin() {
        _uiState.update { it.copy(origin = null, routeGeoJson = null, waypointsWeather = emptyList()) }
    }

    fun clearDestination() {
        _uiState.update { it.copy(destination = null, routeGeoJson = null, waypointsWeather = emptyList()) }
    }

    fun setOrigin(location: LocationSearchResult) {
        _uiState.update { it.copy(origin = location) }
        checkAndCalculateRoute()
    }

    fun setDestination(location: LocationSearchResult) {
        _uiState.update { it.copy(destination = location) }
        checkAndCalculateRoute()
    }

    fun swapLocations() {
        _uiState.update {
            it.copy(origin = it.destination, destination = it.origin)
        }
        checkAndCalculateRoute()
    }

    fun useCurrentLocationAsOrigin() {
        viewModelScope.launch {
            resolveCurrentLocation()?.let { setOrigin(it) }
        }
    }

    fun useCurrentLocationAsDestination() {
        viewModelScope.launch {
            resolveCurrentLocation()?.let { setDestination(it) }
        }
    }

    private suspend fun resolveCurrentLocation(): LocationSearchResult? {
        val cachedLat = _uiState.value.currentDeviceLat
        val cachedLon = _uiState.value.currentDeviceLon

        if (cachedLat != null && cachedLon != null) {
            return LocationSearchResult(
                id = CURRENT_LOCATION_ID,
                name = "Current Location",
                latitude = cachedLat,
                longitude = cachedLon,
                state = "",
                country = ""
            )
        }

        val fresh = locationTracker.getCurrentLocation()
        if (fresh == null) {
            _uiState.update { it.copy(error = "Unable to get your current location. Check location permissions and try again.") }
            return null
        }

        _uiState.update { it.copy(currentDeviceLat = fresh.latitude, currentDeviceLon = fresh.longitude) }
        return LocationSearchResult(
            id = CURRENT_LOCATION_ID,
            name = "Current Location",
            latitude = fresh.latitude,
            longitude = fresh.longitude,
            state = "",
            country = ""
        )
    }

    private fun checkAndCalculateRoute() {
        val origin = _uiState.value.origin
        val destination = _uiState.value.destination

        if (origin != null && destination != null) {
            calculateRoute(origin, destination)
        }
    }

    private fun calculateRoute(origin: LocationSearchResult, destination: LocationSearchResult) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isCalculatingRoute = true, isFetchingWeather = false, error = null)
            }

            routeRepository.getRoute(
                startLat = origin.latitude,
                startLon = origin.longitude,
                endLat = destination.latitude,
                endLon = destination.longitude
            ).onSuccess { route ->
                _uiState.update {
                    it.copy(
                        routeGeoJson = route.geoJsonLineString,
                        routeCoordinates = route.coordinates,
                        isCalculatingRoute = false,
                        isFetchingWeather = true
                    )
                }

                fetchWeatherForWaypoints(route.waypoints)

            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isCalculatingRoute = false,
                        error = "Could not calculate route: ${exception.localizedMessage}"
                    )
                }
            }
        }
    }

    private suspend fun fetchWeatherForWaypoints(waypoints: List<RouteWaypoint>) {
        var previousResolvedName: String? = _uiState.value.origin?.name

        val waypointWeathers = waypoints.mapIndexed { index, waypoint ->
            viewModelScope.async {
                val originName = _uiState.value.origin?.name ?: "Start"
                val destName = _uiState.value.destination?.name ?: "Destination"

                val locationName = when (index) {
                    0 -> originName
                    waypoints.lastIndex -> destName
                    else -> {
                        // Pass origin and previous names to avoid duplicate labels
                        val resolvedCity = getCityNameFromCoordinates(
                            lat = waypoint.latitude,
                            lon = waypoint.longitude,
                            excludeNames = setOfNotNull(originName, destName, previousResolvedName)
                        )

                        previousResolvedName = resolvedCity

                        val eta = formatEta(waypoint.etaOffsetSeconds)
                        if (resolvedCity != null) {
                            "$resolvedCity (ETA: $eta)"
                        } else {
                            "En Route (ETA: $eta)"
                        }
                    }
                }

                val weatherResult = weatherRepository.getWeatherData(
                    lat = waypoint.latitude,
                    lon = waypoint.longitude,
                    cityName = locationName
                )

                val matchedForecast = if (weatherResult.isSuccess) {
                    val weatherData = weatherResult.getOrNull()
                    weatherData?.let { findForecastForEta(it.hourlyForecast, waypoint.etaOffsetSeconds) }
                } else {
                    null
                }

                RouteWaypointWeather(
                    waypoint = waypoint,
                    weather = matchedForecast,
                    locationName = locationName
                )
            }
        }.awaitAll()

        _uiState.update {
            it.copy(
                waypointsWeather = waypointWeathers,
                isFetchingWeather = false
            )
        }
    }

    private suspend fun getCityNameFromCoordinates(
        lat: Double,
        lon: Double,
        excludeNames: Set<String> = emptySet()
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 3) ?: return@withContext null
                val address = addresses.firstOrNull() ?: return@withContext null

                // Candidate names ordered from most specific to broader
                val candidates = listOfNotNull(
                    address.subLocality,
                    address.locality,
                    address.subAdminArea,
                    address.featureName
                )

                candidates.firstOrNull { name ->
                    excludeNames.none { it.equals(name, ignoreCase = true) }
                } ?: address.locality ?: address.subLocality ?: address.subAdminArea
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun findForecastForEta(
        hourlyForecasts: List<HourlyForecast>,
        etaOffsetSeconds: Long
    ): HourlyForecast? {
        if (hourlyForecasts.isEmpty()) return null

        val offsetHours = (etaOffsetSeconds / 3600.0).toInt()
        val safeIndex = offsetHours.coerceIn(0, hourlyForecasts.lastIndex)

        return hourlyForecasts[safeIndex]
    }

    private fun formatEta(etaSeconds: Long): String {
        val hours = etaSeconds / 3600
        val minutes = (etaSeconds % 3600) / 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}