package com.jenil.weather.ui.weather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
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
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.jenil.weather.ui.core.CurrentWeatherHeader
import com.jenil.weather.ui.core.DailyForecastSection
import com.jenil.weather.ui.core.HomeTopBar
import com.jenil.weather.ui.core.HourlyForecastSection
import com.jenil.weather.ui.core.LifestyleSection
import com.jenil.weather.ui.core.RefreshIndicator
import com.jenil.weather.ui.core.WeatherExtraDetailsGrid
import com.jenil.weather.ui.core.WeatherMetricsGrid
import com.jenil.weather.ui.theme.WeatherTheme
import com.jenil.weather.ui.theme.glassBackdrop
import com.jenil.weather.ui.theme.glassCard
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
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

    val hazeState = rememberHazeState()
    val stickyShape = RoundedCornerShape(24.dp)
    val isDark = WeatherTheme.colors.isDark

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
    // Adjusted trigger point slightly for smoother sticky header appearance
    val showStickyHeader = scrollState.value > 580

    val pullState = rememberPullToRefreshState()

    // Mode-aware gradient background to preserve true dark background depth
    val atmosphericGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                MaterialTheme.colorScheme.surface.copy(alpha = 0.05f),
                MaterialTheme.colorScheme.background
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                MaterialTheme.colorScheme.background
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .glassBackdrop(hazeState) // Required for Haze glassmorphism
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
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                        }
                    }

                    ScreenState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = uiState.error ?: "Something went wrong",
                                style = WeatherTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(16.dp))
                            TextButton(
                                onClick = { viewModel.refreshWeatherData() }
                            ) {
                                Text(
                                    "Retry",
                                    style = WeatherTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
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
                                                    WindowInsets.displayCutout.only(WindowInsetsSides.Top)
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
                                        .hazeSource(state = hazeState)
                                        .verticalScroll(scrollState)
                                ) {
                                    Spacer(
                                        modifier = Modifier.height(
                                            WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp
                                        )
                                    )

                                    AnimatedVisibility(
                                        visible = uiState.isOffline,
                                        enter = slideInVertically() + fadeIn(),
                                        exit = slideOutVertically() + fadeOut()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f))
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
                                                    text = "Offline Mode — Cached forecast for ${current.cityName}",
                                                    style = WeatherTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                        }
                                    }
                                    HomeTopBar(
                                        cityName = current.cityName,
                                        modifier = Modifier.padding(horizontal = 20.dp),
                                        onLocationClick = {navController.navigate("routing_screen")}
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    CurrentWeatherHeader(
                                        weatherData = current,
                                        isCelsius = isCelsius,
                                        hazeState = hazeState
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    WeatherMetricsGrid(
                                        weatherData = current,
                                        isKmh = isKmh,
                                        hazeState = hazeState,
                                        isPrecipitationMm = isPrecipitationMm
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    LifestyleSection(
                                        indices = uiState.lifestyleIndexes,
                                        isInitialLoading = uiState.isAiLoading && uiState.lifestyleIndexes.isEmpty(),
                                        isRefreshing = uiState.isAiLoading && uiState.lifestyleIndexes.isNotEmpty(),
                                        hazeState = hazeState
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    HourlyForecastSection(
                                        hourlyData = current.hourlyForecast,
                                        isCelsius = isCelsius,
                                        hazeState = hazeState
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    DailyForecastSection(
                                        forecasts = current.dailyForecast,
                                        isCelsius = isCelsius,
                                        hazeState = hazeState
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    WeatherExtraDetailsGrid(
                                        weatherData = current,
                                        isCelsius = isCelsius,
                                        isKmh = isKmh,
                                        pressureUnit = pressureUnit,
                                        hazeState = hazeState
                                    )

                                    // Extra bottom padding so the last item clears navigation bars
                                    Spacer(modifier = Modifier.height(140.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showStickyHeader && screenState == ScreenState.Content && data != null,
            enter = fadeIn(tween(220)) + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut(tween(180)) + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            data?.let { current ->
                val displayTemp =
                    if (isCelsius) current.temperature
                    else (current.temperature * 9f / 5f + 32f).roundToInt()
                val unit = if (isCelsius) "°C" else "°F"

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(
                                WindowInsets.statusBars.union(
                                    WindowInsets.displayCutout.only(WindowInsetsSides.Top)
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .then(
                                if (!isDark) {
                                    Modifier.shadow(
                                        elevation = 8.dp,
                                        shape = stickyShape,
                                        ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                        spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    )
                                } else Modifier
                            )
                            .glassCard(hazeState, shape = stickyShape)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = current.cityName,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = WeatherTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WeatherTheme.colors.onSurface
                                )
                            }

                            Text(
                                text = "$displayTemp$unit",
                                style = WeatherTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = WeatherTheme.colors.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}