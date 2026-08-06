package com.jenil.weather.ui.weather

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jenil.weather.ui.core.Screen
import com.jenil.weather.ui.core.WeatherBottomBar
import com.jenil.weather.ui.map.WeatherMapScreen
import com.jenil.weather.ui.search.SearchScreen
import com.jenil.weather.ui.settings.SettingsScreen
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MainDashboardScreen(
    navController: NavController,
    weatherViewModel: WeatherViewModel
) {
    var selectedTabRoute by remember { mutableStateOf(Screen.Weather.route) }
    val bottomBarHazeState = remember { HazeState() }

    LaunchedEffect(Unit) {
        weatherViewModel.fetchWeatherForCurrentLocation()
    }


    var backPressedOnce by remember { mutableStateOf(false) }
    val context = LocalContext.current

    BackHandler(enabled = true) {
        when {
            selectedTabRoute != Screen.Weather.route -> {
                selectedTabRoute = Screen.Weather.route
            }
            backPressedOnce -> {
                (context as? Activity)?.finish()
            }
            else -> {
                backPressedOnce = true
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
        }
    }
    LaunchedEffect(backPressedOnce) {
        if (backPressedOnce) {
            delay(2000.milliseconds)
            backPressedOnce = false
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()

    ) {
        when (selectedTabRoute) {
            Screen.Weather.route -> {
                WeatherScreen(
                    navController = navController,
                    viewModel = weatherViewModel
                )
            }

            Screen.Locations.route -> {
                val searchHazeState = remember { HazeState() }
                SearchScreen(
                    navController = navController,
                    onLocationSelected = { lat, lon, cityName ->
                        weatherViewModel.loadWeatherData(lat, lon, cityName)
                        selectedTabRoute = Screen.Weather.route
                    },
                    hazeState = searchHazeState
                )
            }
            Screen.Map.route -> {
                WeatherMapScreen(
                    navController = navController,
                )
            }
            Screen.Settings.route -> {
                SettingsScreen(
                    navController = navController,
                )
            }
        }
        WeatherBottomBar(
            currentRoute = selectedTabRoute,
            onTabSelected = { screen ->
                selectedTabRoute = screen.route
            },
            hazeState = bottomBarHazeState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),

        )
    }
}