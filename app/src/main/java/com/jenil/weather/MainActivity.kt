package com.jenil.weather

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.jenil.weather.ui.location.ManageLocationsScreen
import com.jenil.weather.ui.onboarding.OnboardingScreen
import com.jenil.weather.ui.search.SearchScreen
import com.jenil.weather.ui.settings.SettingsViewModel
import com.jenil.weather.ui.splash.SplashScreen
import com.jenil.weather.ui.weather.MainDashboardScreen
import com.jenil.weather.ui.weather.WeatherViewModel
import com.jenil.weather.utils.WeatherNotificationManager
import com.jenil.weather.data.worker.WeatherWorker
import com.jenil.weather.ui.theme.WeatherAppTheme
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.haze.HazeState
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val soundUri =
                "${ContentResolver.SCHEME_ANDROID_RESOURCE}://$packageName/${R.raw.weather_notification}".toUri()

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val dailyChannel = NotificationChannel(
                WeatherNotificationManager.CHANNEL_DAILY,
                "Daily Forecasts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Morning and evening weather briefs"
                setSound(soundUri, audioAttributes)
            }

            val rainChannel = NotificationChannel(
                WeatherNotificationManager.CHANNEL_RAIN,
                "Rain Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for approaching precipitation"
                setSound(soundUri, audioAttributes)
                enableVibration(true)
            }

            val weatherAlertsChannel = NotificationChannel(
                WeatherNotificationManager.CHANNEL_WEATHER_ALERTS,
                "Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Wind, heat, cold, and storm warnings"
                setSound(soundUri, audioAttributes)
                enableVibration(true)
            }

            val aqiChannel = NotificationChannel(
                WeatherNotificationManager.CHANNEL_AQI,
                "Air Quality Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Warnings for unhealthy air quality"
                setSound(soundUri, audioAttributes)
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(
                listOf(dailyChannel, rainChannel, weatherAlertsChannel, aqiChannel)
            )
        }
    }

    private fun scheduleWeatherNotificationWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val weatherRequest = PeriodicWorkRequestBuilder<WeatherWorker>(30, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "WeatherNotificationJob",
            ExistingPeriodicWorkPolicy.KEEP,
            weatherRequest
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        createNotificationChannels()
        scheduleWeatherNotificationWorker()

        setContent {
            val weatherViewModel: WeatherViewModel = hiltViewModel()
            val settingsViewModel: SettingsViewModel = hiltViewModel()

            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            val isOnboardingComplete by weatherViewModel.isOnboardingComplete.collectAsStateWithLifecycle(initialValue = false)


            WeatherAppTheme(darkTheme = settingsState.isDarkTheme) {

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "splash_route"
                ) {
                    composable("splash_route", exitTransition = {
                        fadeOut(
                            animationSpec = tween(600, easing = FastOutSlowInEasing)
                        ) + scaleOut(
                            targetScale = 1.08f,
                            animationSpec = tween(600, easing = FastOutSlowInEasing)
                        )
                    }
                    ) {
                        SplashScreen(
                            viewModel = weatherViewModel,
                            onReady = {
                                val destination = if (isOnboardingComplete) "weather_route" else "onboarding_route"

                                navController.navigate(destination) {
                                    popUpTo("splash_route") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("weather_route") {
                        MainDashboardScreen(
                            navController = navController,
                            weatherViewModel = weatherViewModel
                        )
                    }

                    composable("onboarding_route") {
                        OnboardingScreen(navController = navController)
                    }
                    composable("manage_locations") {
                        val manageLocationsHazeState = remember { HazeState() }
                        ManageLocationsScreen(
                            navController = navController,
                            onSearchClick = { navController.navigate("search_route") },
                            onBackClick = { navController.popBackStack() },
                            hazeState = manageLocationsHazeState
                        )
                    }
                    composable("search_route") {
                        val searchHazeState = remember { HazeState() }
                        SearchScreen(
                            navController = navController,
                            onLocationSelected = { lat, lon, cityName ->
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.apply {
                                        set("lat", lat)
                                        set("lon", lon)
                                        set("cityName", cityName)
                                    }
                                navController.popBackStack()
                            },
                            hazeState = searchHazeState
                        )
                    }
                }
            }
        }
    }
}