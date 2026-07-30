package com.jenil.weather.ui.weather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.jenil.weather.ui.core.CurrentWeatherHeader
import com.jenil.weather.ui.core.DailyForecastSection
import com.jenil.weather.ui.core.HourlyForecastSection
import com.jenil.weather.ui.core.RefreshIndicator
import com.jenil.weather.ui.core.WeatherExtraDetailsGrid
import com.jenil.weather.ui.core.WeatherMetricsGrid
import kotlin.math.roundToInt

private enum class ScreenState { Loading, Error, Content }

@Composable
fun WeatherScreen(
    navController: NavController,
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {

    val navBackStackEntry = navController.currentBackStackEntry
    val savedStateHandle = navBackStackEntry?.savedStateHandle

    val isCelsius by viewModel.isCelsius.collectAsStateWithLifecycle()
    val isKmh by viewModel.isKmh.collectAsStateWithLifecycle()
    val isPrecipitationMm by viewModel.isPrecipitationMm.collectAsStateWithLifecycle()
    val pressureUnit by viewModel.pressureUnit.collectAsStateWithLifecycle()

    val lat by savedStateHandle?.getStateFlow<Double?>("lat", null)?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(null) }
    val lon by savedStateHandle?.getStateFlow<Double?>("lon", null)?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(null) }
    val cityName by savedStateHandle?.getStateFlow<String?>("cityName", null)
        ?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(null) }

    LaunchedEffect(lat, lon, cityName) {
        if (lat != null && lon != null && cityName != null) {
            viewModel.loadWeatherData(lat!!, lon!!, cityName!!)

            savedStateHandle?.remove<Double>("lat")
            savedStateHandle?.remove<Double>("lon")
            savedStateHandle?.remove<String>("cityName")
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val data = uiState.data

    val screenState = when {
        uiState.isLoading && data == null -> ScreenState.Loading
        uiState.error != null && data == null -> ScreenState.Error
        else -> ScreenState.Content
    }
    val scrollState = rememberScrollState()
    val showStickyHeader = scrollState.value > 800

    val pullState = rememberPullToRefreshState()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent
        ) { innerPadding ->
            Crossfade(
                targetState = screenState,
                animationSpec = tween(400),
                label = "screen_state"
            ) { state ->
                when (state) {
                    ScreenState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    ScreenState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = uiState.error ?: "Something went wrong",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(12.dp))
                            TextButton(
                                onClick = { viewModel.refreshWeatherData() }
                            ) {
                                Text("Retry")
                            }
                        }
                    }

                    ScreenState.Content -> {
                        val current = data
                        if (current != null) {
                            PullToRefreshBox(
                                isRefreshing = uiState.isRefreshing,
                                onRefresh = { viewModel.refreshWeatherData() },
                                state = pullState,
                                indicator = {
                                    RefreshIndicator(
                                        isRefreshing = uiState.isLoading,
                                        state = pullState,
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .windowInsetsPadding(
                                                WindowInsets.statusBars.union(
                                                    WindowInsets.displayCutout.only(
                                                        WindowInsetsSides.Top
                                                    )
                                                )
                                            )
                                            .padding(top = 12.dp)
                                    )
                                },
                                modifier = Modifier.fillMaxSize()

                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                        .verticalScroll(scrollState)
                                ) {

                                    // --- OFFLINE BANNER ---
                                    AnimatedVisibility(
                                        visible = uiState.isOffline,
                                        enter = slideInVertically() + fadeIn(),
                                        exit = slideOutVertically() + fadeOut()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.errorContainer)
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.WifiOff,
                                                    contentDescription = "Offline",
                                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Offline Mode — Showing cached forecast for ${current.cityName}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                        }
                                    }
                                    // --- END OFFLINE BANNER ---

                                    CurrentWeatherHeader(
                                        weatherData = current,
                                        onSettingsClick = { navController.navigate("settings_route") },
                                        isCelsius = isCelsius,
                                    )
                                    WeatherMetricsGrid(
                                        weatherData = current,
                                        isKmh = isKmh
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HourlyForecastSection(
                                        hourlyData = current.hourlyForecast,
                                        isCelsius = isCelsius,
                                    )
                                    DailyForecastSection(
                                        dailyData = current.dailyForecast,
                                        isCelsius = isCelsius
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    WeatherExtraDetailsGrid(
                                        weatherData = current,
                                        isCelsius = isCelsius,
                                        isKmh = isKmh,
                                        isPrecipitationMm = isPrecipitationMm,
                                        pressureUnit = pressureUnit
                                    )
                                    Spacer(modifier = Modifier.height(120.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showStickyHeader && screenState == ScreenState.Content && data != null,
            enter = fadeIn(tween(250)) + slideInVertically(initialOffsetY = { -it / 2 }),
            exit = fadeOut(tween(180)) + slideOutVertically(targetOffsetY = { -it / 2 }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            data?.let { current ->

                val displayTemp =
                    if (isCelsius) current.temperature
                    else (current.temperature * 9f / 5f + 32f).roundToInt()
                val unit = if (isCelsius) "°C" else "°F"

                val settingsInteractionSource = remember { MutableInteractionSource() }
                val isSettingsPressed by settingsInteractionSource.collectIsPressedAsState()
                val settingsScale by animateFloatAsState(
                    targetValue = if (isSettingsPressed) 0.88f else 1f,
                    animationSpec = tween(150, easing = FastOutSlowInEasing),
                    label = "sticky_settings_scale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(
                            WindowInsets.statusBars.union(
                                WindowInsets.displayCutout.only(WindowInsetsSides.Top)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp), clip = false)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(28.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = current.cityName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Text(
                            text = "$displayTemp$unit",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}