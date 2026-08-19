package com.jenil.weather.ui.route

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jenil.weather.ui.core.RouteWeatherItemCard
import com.jenil.weather.ui.core.SearchFieldType
import com.jenil.weather.ui.core.SearchResultsList
import com.jenil.weather.ui.search.WeatherSearchViewModel
import com.jenil.weather.ui.theme.WeatherTheme
import com.jenil.weather.ui.theme.glassCard
import com.jenil.weather.utils.zoomForSpan
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationPuckColors
import org.maplibre.compose.location.LocationPuckSizes
import org.maplibre.compose.location.mostAccurateBearing
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberDefaultOrientationProvider
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteWeatherScreen(
    onBackClick: () -> Unit,
    routeViewModel: RouteWeatherViewModel = hiltViewModel(),
    searchViewModel: WeatherSearchViewModel = hiltViewModel()
) {
    val uiState by routeViewModel.uiState.collectAsState()
    val searchResults by searchViewModel.searchResults.collectAsState()
    val searchQuery by searchViewModel.searchQuery.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isMapReady by remember { mutableStateOf(false) }

    val hazeState = rememberHazeState()
    var activeSearchField by remember { mutableStateOf<SearchFieldType?>(null) }

    val locationProvider = rememberDefaultLocationProvider()
    val orientationProvider = rememberDefaultOrientationProvider()
    val userLocationState = rememberUserLocationState(locationProvider, orientationProvider)

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(target = Position(72.5714, 23.0225), zoom = 5.0)
    )

    // Split waypoints into start, midpoints, and destination
    val startWaypoint = uiState.waypointsWeather.firstOrNull()
    val endWaypoint = if (uiState.waypointsWeather.size > 1) uiState.waypointsWeather.lastOrNull() else null
    val intermediateWaypoints = remember(uiState.waypointsWeather) {
        if (uiState.waypointsWeather.size > 2) {
            uiState.waypointsWeather.subList(1, uiState.waypointsWeather.size - 1)
        } else {
            emptyList()
        }
    }

    LaunchedEffect(uiState.routeCoordinates, isMapReady) {
        val points = uiState.routeCoordinates
        if (isMapReady && points.isNotEmpty()) {
            val lons = points.map { it.first }
            val lats = points.map { it.second }
            val minLon = lons.min(); val maxLon = lons.max()
            val minLat = lats.min(); val maxLat = lats.max()

            val centerLon = (minLon + maxLon) / 2
            val centerLat = (minLat + maxLat) / 2

            val lonSpan = (maxLon - minLon) * kotlin.math.cos(Math.toRadians(centerLat))
            val latSpan = maxLat - minLat
            val span = maxOf(lonSpan, latSpan)

            cameraState.animateTo(
                finalPosition = cameraState.position.copy(
                    target = Position(centerLon, centerLat),
                    zoom = zoomForSpan(span),
                    padding = PaddingValues(top = 180.dp, bottom = 220.dp, start = 32.dp, end = 32.dp)
                ),
                duration = 1000.milliseconds
            )
        }
    }

    LaunchedEffect(uiState.currentDeviceLat, uiState.currentDeviceLon, isMapReady) {
        val lat = uiState.currentDeviceLat
        val lon = uiState.currentDeviceLon
        if (isMapReady && lat != null && lon != null && uiState.routeCoordinates.isEmpty()) {
            cameraState.animateTo(
                finalPosition = cameraState.position.copy(
                    target = Position(lon, lat),
                    zoom = 11.0
                ),
                duration = 800.milliseconds
            )
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // 1. Interactive Map Layer
            MaplibreMap(
                modifier = Modifier.fillMaxSize().hazeSource(hazeState),
                baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
                cameraState = cameraState,
                options = MapOptions(
                    ornamentOptions = OrnamentOptions(
                        isCompassEnabled = false,
                        isScaleBarEnabled = false,
                        padding = PaddingValues(bottom = 8.dp, start = 8.dp)
                    )
                ),
                onMapLoadFinished = { isMapReady = true },
                onMapLoadFailed = { Log.e("RouteWeatherScreen", "Map load failed: $it") }
            ) {
                // User Location Puck (When not routing)
                if (uiState.routeGeoJson.isNullOrEmpty()) {
                    LocationPuck(
                        idPrefix = "user",
                        location = userLocationState.location,
                        bearing = userLocationState.mostAccurateBearing(),
                        cameraState = cameraState,
                        colors = LocationPuckColors(
                            dotStrokeColor = Color.White,
                            dotFillColorCurrentLocation = WeatherTheme.colors.mapBlue,
                            dotFillColorOldLocation = Color.Gray,
                            accuracyStrokeColor = WeatherTheme.colors.mapBlue,
                            accuracyFillColor = WeatherTheme.colors.mapBlue.copy(alpha = 0.15f),
                            bearingColor = WeatherTheme.colors.mapAmber
                        ),
                        sizes = LocationPuckSizes(
                            dotRadius = 7.dp,
                            dotStrokeWidth = 3.dp,
                            shadowBlur = 2.0f
                        ),
                        accuracyThreshold = 75f,
                        showBearing = false
                    )
                }

                // Route Highway Polyline
                if (!uiState.routeGeoJson.isNullOrEmpty()) {
                    val routeSource = rememberGeoJsonSource(data = GeoJsonData.JsonString(uiState.routeGeoJson!!))
                    LineLayer(
                        id = "route-line",
                        source = routeSource,
                        color = const(WeatherTheme.colors.mapBlue),
                        width = const(5.5.dp)
                    )
                }

                // A. Intermediate Waypoints (Subtle Checkpoints)
                if (intermediateWaypoints.isNotEmpty()) {
                    val midFeatures = intermediateWaypoints.map { wp ->
                        Feature(geometry = Point(Position(wp.waypoint.longitude, wp.waypoint.latitude)), properties = null)
                    }
                    val midSource = rememberGeoJsonSource(data = GeoJsonData.Features(FeatureCollection(midFeatures)))
                    CircleLayer(
                        id = "route-mid-waypoints",
                        source = midSource,
                        radius = const(5.dp),
                        color = const(Color.White),
                        strokeColor = const(WeatherTheme.colors.mapBlue),
                        strokeWidth = const(2.dp)
                    )
                }

                // B. Start Puck (Origin: Blue center with crisp white ring)
                if (startWaypoint != null) {
                    val startSource = rememberGeoJsonSource(
                        data = GeoJsonData.Features(
                            FeatureCollection(
                                listOf(
                                    Feature(
                                        geometry = Point(Position(startWaypoint.waypoint.longitude, startWaypoint.waypoint.latitude)),
                                        properties = null
                                    )
                                )
                            )
                        )
                    )
                    CircleLayer(
                        id = "route-start-puck",
                        source = startSource,
                        radius = const(7.5.dp),
                        color = const(WeatherTheme.colors.mapBlue),
                        strokeColor = const(Color.White),
                        strokeWidth = const(3.dp)
                    )
                }

                // C. Destination Puck (End: Red center with bold white ring)
                if (endWaypoint != null) {
                    val endSource = rememberGeoJsonSource(
                        data = GeoJsonData.Features(
                            FeatureCollection(
                                listOf(
                                    Feature(
                                        geometry = Point(Position(endWaypoint.waypoint.longitude, endWaypoint.waypoint.latitude)),
                                        properties = null
                                    )
                                )
                            )
                        )
                    )
                    CircleLayer(
                        id = "route-end-puck",
                        source = endSource,
                        radius = const(8.5.dp),
                        color = const(WeatherTheme.colors.mapRed),
                        strokeColor = const(Color.White),
                        strokeWidth = const(3.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .height(220.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.55f),
                                Color.Black.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
                    .zIndex(0.5f)
            )

            // Loading Overlay
            val isBusy = uiState.isCalculatingRoute || uiState.isFetchingWeather || uiState.isFetchingInitialLocation
            AnimatedVisibility(
                visible = isBusy,
                modifier = Modifier.fillMaxSize().zIndex(2f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            // 3. Top UI Layer
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp)
                    .zIndex(1f)
                    .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow))
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .heightIn(min = 40.dp)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Commute Weather",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                // Unified Search Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Origin",
                                tint = WeatherTheme.colors.mapBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .width(2.dp)
                                    .height(36.dp)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), CircleShape)
                            )
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Destination",
                                tint = WeatherTheme.colors.mapRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            val originText = if (activeSearchField == SearchFieldType.ORIGIN) searchQuery else uiState.origin?.name ?: ""
                            val destinationText = if (activeSearchField == SearchFieldType.DESTINATION) searchQuery else uiState.destination?.name ?: ""
                            TextField(
                                value = originText,
                                onValueChange = {
                                    activeSearchField = SearchFieldType.ORIGIN
                                    searchViewModel.onSearchQueryChanged(it)
                                },
                                placeholder = { Text("Start location", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (originText.isNotEmpty()) {
                                            IconButton(onClick = {
                                                activeSearchField = SearchFieldType.ORIGIN
                                                searchViewModel.onSearchQueryChanged("")
                                                routeViewModel.clearOrigin()
                                            }) {
                                                Icon(Icons.Default.Close, contentDescription = "Clear start location", modifier = Modifier.size(18.dp))
                                            }
                                        }
                                        IconButton(onClick = {
                                            activeSearchField = null
                                            searchViewModel.onSearchQueryChanged("")
                                            routeViewModel.useCurrentLocationAsOrigin()
                                        }) {
                                            Icon(
                                                Icons.Outlined.MyLocation,
                                                contentDescription = "Use current location as start",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            TextField(
                                value = destinationText,
                                onValueChange = {
                                    activeSearchField = SearchFieldType.DESTINATION
                                    searchViewModel.onSearchQueryChanged(it)
                                },
                                placeholder = { Text("Destination", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (destinationText.isNotEmpty()) {
                                            IconButton(onClick = {
                                                activeSearchField = SearchFieldType.DESTINATION
                                                searchViewModel.onSearchQueryChanged("")
                                                routeViewModel.clearDestination()
                                            }) {
                                                Icon(Icons.Default.Close, contentDescription = "Clear destination location", modifier = Modifier.size(18.dp))
                                            }
                                        }
                                        IconButton(onClick = {
                                            activeSearchField = null
                                            searchViewModel.onSearchQueryChanged("")
                                            routeViewModel.useCurrentLocationAsDestination()
                                        }) {
                                            Icon(
                                                Icons.Outlined.MyLocation,
                                                contentDescription = "Use current location as destination",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        IconButton(
                            onClick = { routeViewModel.swapLocations() },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(Icons.Default.SwapVert, contentDescription = "Swap", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                AnimatedVisibility(visible = activeSearchField != null && searchResults.isNotEmpty()) {
                    SearchResultsList(searchResults, hazeState) { result ->
                        if (activeSearchField == SearchFieldType.ORIGIN) {
                            routeViewModel.setOrigin(result)
                        } else {
                            routeViewModel.setDestination(result)
                        }
                        searchViewModel.onSearchQueryChanged("")
                        activeSearchField = null
                    }
                }

                AnimatedVisibility(
                    visible = uiState.origin == null && uiState.destination == null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = "Pick a start and end point to see weather along your route.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 12.dp, start = 8.dp)
                    )
                }
            }

            // 4. Bottom Layer
            AnimatedVisibility(
                visible = uiState.waypointsWeather.isNotEmpty(),
                modifier = Modifier.align(Alignment.BottomCenter).zIndex(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(hazeState, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                        .navigationBarsPadding()
                        .padding(vertical = 20.dp)
                ) {
                    Text(
                        text = "Weather Along Route",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.waypointsWeather) { wp ->
                            RouteWeatherItemCard(wp)
                        }
                    }
                }
            }
        }
    }
}