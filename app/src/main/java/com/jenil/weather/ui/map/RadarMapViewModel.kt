package com.jenil.weather.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jenil.weather.domain.location.LocationTracker
import com.jenil.weather.domain.model.MapLayerType // <-- NEW IMPORT
import com.jenil.weather.domain.model.RadarFrame
import com.jenil.weather.domain.repository.RadarRepository
import com.jenil.weather.domain.repository.WindRepository // <-- NEW IMPORT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

data class RadarLatLng(val latitude: Double, val longitude: Double)

data class WeatherMapUiState(
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val userLocation: RadarLatLng = RadarLatLng(23.0225, 72.5714),
    val host: String = "",
    val frames: List<RadarFrame> = emptyList(),
    val currentFrameIndex: Int = 0,
    val formattedTime: String = "",
    val errorMessage: String? = null,
    val selectedLayer: MapLayerType = MapLayerType.RADAR,
    val windTileUrl: String? = null,
    val windGeoJson: String = ""
)

private const val REFRESH_INTERVAL_MINUTES = 10L

@HiltViewModel
class WeatherMapViewModel @Inject constructor(
    private val radarRepository: RadarRepository,
    private val locationTracker: LocationTracker,
    private val windRepository: WindRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherMapUiState())
    val uiState: StateFlow<WeatherMapUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private var windFetchJob: Job? = null

    init {
        // Pre-fetch the wind tile URL immediately so it's ready if the user switches
        _uiState.update {
            it.copy(windTileUrl = windRepository.getTileUrl(MapLayerType.WIND))
        }

        viewModelScope.launch {
            while (true) {
                loadData()
                delay(REFRESH_INTERVAL_MINUTES.minutes)
            }
        }
    }

    // --- NEW FUNCTION TO SWITCH LAYERS ---
    fun selectLayer(layerType: MapLayerType) {
        _uiState.update { it.copy(selectedLayer = layerType) }

        if (layerType != MapLayerType.RADAR) {
            stopPlayback()
        }

        if (layerType == MapLayerType.WIND && _uiState.value.windGeoJson.isEmpty()) {
            val currentLocation = _uiState.value.userLocation
            fetchWindData(currentLocation.latitude, currentLocation.longitude)
        }
    }

    fun fetchWindData(latitude: Double, longitude: Double,zoom: Double = 7.0) {
        windFetchJob?.cancel()

        windFetchJob = viewModelScope.launch {
            try {
                val geoJson = windRepository.fetchWindGridGeoJson(latitude, longitude, zoom)
                _uiState.update { it.copy(windGeoJson = geoJson, errorMessage = null) }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    e.printStackTrace()
                    if (_uiState.value.windGeoJson.isEmpty()) {
                        _uiState.update { it.copy(errorMessage = "Failed to load wind data") }
                    }
                }
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }


            val location = locationTracker.getCurrentLocation()
            val userLatLng = location?.let { RadarLatLng(it.latitude, it.longitude) }
                ?: _uiState.value.userLocation

            fetchWindData(userLatLng.latitude, userLatLng.longitude , 7.0)

            radarRepository.getRadarMetaData()
                .onSuccess { metaData ->
                    val lastIndex = (metaData.pastFrames.size - 1).coerceAtLeast(0)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userLocation = userLatLng,
                            host = metaData.host,
                            frames = metaData.pastFrames,
                            currentFrameIndex = lastIndex,
                            formattedTime = if (metaData.pastFrames.isNotEmpty()) {
                                formatTimestamp(metaData.pastFrames[lastIndex].time)
                            } else ""
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userLocation = userLatLng,
                            errorMessage = error.localizedMessage ?: "Failed to load radar data"
                        )
                    }
                }
        }
    }

    fun togglePlayback() {
        if (_uiState.value.isPlaying) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        _uiState.update { it.copy(isPlaying = true) }
        playbackJob = viewModelScope.launch {
            while (_uiState.value.isPlaying) {
                delay(700.milliseconds)
                val currentState = _uiState.value
                if (currentState.frames.isEmpty()) break

                val nextIndex = (currentState.currentFrameIndex + 1) % currentState.frames.size
                _uiState.update {
                    it.copy(
                        currentFrameIndex = nextIndex,
                        formattedTime = formatTimestamp(currentState.frames[nextIndex].time)
                    )
                }
            }
        }
    }

    private fun stopPlayback() {
        playbackJob?.cancel()
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun selectFrame(index: Int) {
        stopPlayback()
        val frames = _uiState.value.frames
        if (index in frames.indices) {
            _uiState.update {
                it.copy(
                    currentFrameIndex = index,
                    formattedTime = formatTimestamp(frames[index].time)
                )
            }
        }
    }

    private fun formatTimestamp(timeInSeconds: Long): String {
        val date = Date(timeInSeconds * 1000)
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
    }
}