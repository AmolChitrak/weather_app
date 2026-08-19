package com.jenil.weather.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.jenil.weather.domain.model.MapLayerType
import com.jenil.weather.ui.core.RadarLegend
import com.jenil.weather.ui.core.RadarPlaybackBar
import com.jenil.weather.ui.core.WindLegend
import com.jenil.weather.ui.core.WindScale
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.asNumber
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.interpolate
import org.maplibre.compose.expressions.dsl.linear
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.RasterLayer
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.TileSetOptions
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.sources.rememberRasterSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlin.time.Duration.Companion.milliseconds

private const val BASE_MAP_STYLE = "https://tiles.openfreemap.org/styles/liberty"
private const val RADAR_TILE_SIZE = 256
private const val RADAR_COLOR_SCHEME = 2
private const val MIN_ZOOM = 2f
private const val RADAR_MAX_ZOOM = 7f
private const val ZOOM_STEP = 1f
private const val MAP_ATTRIBUTION =
    "© OpenFreeMap © OpenMapTiles © OpenStreetMap contributors"

@OptIn(FlowPreview::class)
@Composable
fun WeatherMapScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: WeatherMapViewModel = hiltViewModel(),
    bottomNavClearance: Dp = 120.dp,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(
                latitude = uiState.userLocation.latitude,
                longitude = uiState.userLocation.longitude,
            ),
            zoom = 7.0,
        )
    )

    LaunchedEffect(uiState.userLocation) {
        cameraState.animateTo(
            finalPosition = cameraState.position.copy(
                target = Position(
                    latitude = uiState.userLocation.latitude,
                    longitude = uiState.userLocation.longitude,
                ),
                zoom = 7.0,
            ),
            duration = 500.milliseconds,
        )
    }

    fun zoomBy(delta: Float) {
        coroutineScope.launch {
            val newZoom = (cameraState.position.zoom + delta).coerceIn(MIN_ZOOM.toDouble(), RADAR_MAX_ZOOM.toDouble())
            cameraState.animateTo(
                finalPosition = cameraState.position.copy(zoom = newZoom),
                duration = 200.milliseconds,
            )
        }
    }

    fun recenter() {
        coroutineScope.launch {
            cameraState.animateTo(
                finalPosition = cameraState.position.copy(
                    target = Position(
                        latitude = uiState.userLocation.latitude,
                        longitude = uiState.userLocation.longitude,
                    ),
                    zoom = 7.0,
                ),
                duration = 500.milliseconds,
            )
        }
    }

    val currentFrame = uiState.frames.getOrNull(uiState.currentFrameIndex)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri(BASE_MAP_STYLE),
            cameraState = cameraState,
            zoomRange = MIN_ZOOM..RADAR_MAX_ZOOM,
            options = MapOptions(
                ornamentOptions = OrnamentOptions.AllDisabled,
                gestureOptions = GestureOptions(
                    isTiltEnabled = false,
                    isRotateEnabled = false,
                ),
            ),
        ) {
            val userLocationSource = rememberGeoJsonSource(
                data = GeoJsonData.Features(
                    FeatureCollection(
                        listOf(
                            Feature(
                                geometry = Point(
                                    Position(
                                        latitude = uiState.userLocation.latitude,
                                        longitude = uiState.userLocation.longitude,
                                    )
                                ),
                                properties = null
                            )
                        )
                    )
                )
            )

            when (uiState.selectedLayer) {
                MapLayerType.RADAR -> {
                    if (currentFrame != null && uiState.host.isNotEmpty()) {
                        val tileUrl = "${uiState.host}${currentFrame.path}/" +
                                "$RADAR_TILE_SIZE/{z}/{x}/{y}/$RADAR_COLOR_SCHEME/1_1.png"

                        val radarSource = rememberRasterSource(
                            tiles = listOf(tileUrl),
                            options = TileSetOptions(minZoom = 0, maxZoom = RADAR_MAX_ZOOM.toInt()),
                            tileSize = RADAR_TILE_SIZE
                        )

                        RasterLayer(
                            id = "radar-layer",
                            source = radarSource,
                            minZoom = MIN_ZOOM,
                            maxZoom = RADAR_MAX_ZOOM,
                        )
                    }
                }

                MapLayerType.WIND -> {
                    LaunchedEffect(uiState.selectedLayer) {
                        if (uiState.selectedLayer == MapLayerType.WIND) {
                            viewModel.fetchWindData(
                                cameraState.position.target.latitude,
                                cameraState.position.target.longitude,
                                cameraState.position.zoom
                            )

                            snapshotFlow { cameraState.position.target to cameraState.position.zoom }
                                .debounce(800.milliseconds)
                                .collect { (target, zoom) ->
                                    viewModel.fetchWindData(target.latitude, target.longitude, zoom)
                                }
                        }
                    }

                    if (uiState.windGeoJson.isNotEmpty()) {
                        val windSource = rememberGeoJsonSource(
                            data = GeoJsonData.JsonString(uiState.windGeoJson)
                        )

                        org.maplibre.compose.layers.LineLayer(
                            id = "wind-streamlines-layer",
                            source = windSource,
                            width = interpolate(
                                linear(),
                                feature["averageSpeedKmh"].asNumber(),
                                WindScale.widthStops[0].first to const(WindScale.widthStops[0].second.dp),
                                WindScale.widthStops[1].first to const(WindScale.widthStops[1].second.dp),
                                WindScale.widthStops[2].first to const(WindScale.widthStops[2].second.dp),
                            ),
                            color = interpolate(
                                linear(),
                                feature["averageSpeedKmh"].asNumber(),
                                WindScale.stops[0].speedKmh to const(WindScale.stops[0].color),
                                WindScale.stops[1].speedKmh to const(WindScale.stops[1].color),
                                WindScale.stops[2].speedKmh to const(WindScale.stops[2].color),
                                WindScale.stops[3].speedKmh to const(WindScale.stops[3].color),
                            ),
                            opacity = interpolate(
                                linear(),
                                feature["averageSpeedKmh"].asNumber(),
                                WindScale.opacityStops[0].first to const(WindScale.opacityStops[0].second),
                                WindScale.opacityStops[1].first to const(WindScale.opacityStops[1].second),
                            )
                        )
                    }
                }
            }

            if (hasLocationPermission) {
                CircleLayer(
                    id = "user-location-dot",
                    source = userLocationSource,
                    radius = const(7.dp),
                    color = const(Color(0xFF2A6DF4)),
                    strokeColor = const(Color.White),
                    strokeWidth = const(2.dp),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                MapLayerSelector(
                    selectedLayer = uiState.selectedLayer,
                    onLayerSelected = viewModel::selectLayer
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 64.dp, end = 12.dp)
        ) {
            when (uiState.selectedLayer) {
                MapLayerType.RADAR -> RadarLegend()
                MapLayerType.WIND -> WindLegend()
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallFloatingActionButton(onClick = { zoomBy(ZOOM_STEP) }) {
                Icon(Icons.Default.Add, contentDescription = "Zoom in")
            }
            SmallFloatingActionButton(onClick = { zoomBy(-ZOOM_STEP) }) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom out")
            }
            if (hasLocationPermission) {
                Spacer(Modifier.height(4.dp))
                SmallFloatingActionButton(onClick = { recenter() }) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Recenter on my location")
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Loading map...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !uiState.isLoading && uiState.errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    TextButton(onClick = { viewModel.loadData() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Retry", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        if (uiState.selectedLayer == MapLayerType.RADAR && uiState.frames.isNotEmpty()) {
            RadarPlaybackBar(
                isPlaying = uiState.isPlaying,
                currentFrameIndex = uiState.currentFrameIndex,
                frameCount = uiState.frames.size,
                formattedTime = uiState.formattedTime,
                onTogglePlayback = viewModel::togglePlayback,
                onScrub = viewModel::selectFrame,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomNavClearance)
                    .padding(horizontal = 16.dp)
            )
        }

        Text(
            text = MAP_ATTRIBUTION,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(start = 8.dp, bottom = 4.dp)
        )
    }
}

@Composable
private fun MapLayerSelector(
    selectedLayer: MapLayerType,
    onLayerSelected: (MapLayerType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(3.dp)) {
            MapLayerType.entries.forEach { layer ->
                val isSelected = layer == selectedLayer

                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                    else Color.Transparent,
                    animationSpec = tween(150),
                    label = "layerChipBackground"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(150),
                    label = "layerChipContent"
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(backgroundColor)
                        .clickable { onLayerSelected(layer) }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = layer.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = contentColor
                    )
                }
            }
        }
    }
}