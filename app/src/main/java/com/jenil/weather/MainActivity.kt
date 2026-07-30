package com.jenil.weather

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jenil.weather.domain.model.WeatherCondition
import com.jenil.weather.ui.location.ManageLocationsScreen
import com.jenil.weather.ui.onboarding.OnboardingScreen
import com.jenil.weather.ui.search.SearchScreen
import com.jenil.weather.ui.settings.SettingsScreen
import com.jenil.weather.ui.settings.SettingsViewModel
import com.jenil.weather.ui.splash.SplashScreen
import com.jenil.weather.ui.theme.WeatherTheme
import com.jenil.weather.ui.weather.MainDashboardScreen
import com.jenil.weather.ui.weather.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.jenil.weather.utils.WeatherNotificationManager
import com.jenil.weather.worker.WeatherWorker
import java.util.concurrent.TimeUnit
import androidx.core.net.toUri

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

            // 2. Daily Forecasts (IMPORTANCE_DEFAULT)
            val dailyChannel = NotificationChannel(
                WeatherNotificationManager.CHANNEL_DAILY,
                "Daily Forecasts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Morning and evening weather briefs"
                setSound(soundUri, audioAttributes)
            }

            // 3. Rain Alerts (IMPORTANCE_HIGH)
            val rainChannel = NotificationChannel(
                WeatherNotificationManager.CHANNEL_RAIN,
                "Rain Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for approaching precipitation"
                setSound(soundUri, audioAttributes)
                enableVibration(true)
            }

            // 4. Weather Alerts (IMPORTANCE_HIGH)
            val weatherAlertsChannel = NotificationChannel(
                WeatherNotificationManager.CHANNEL_WEATHER_ALERTS,
                "Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Wind, heat, cold, and storm warnings"
                setSound(soundUri, audioAttributes)
                enableVibration(true)
            }

            // 5. Air Quality Alerts (IMPORTANCE_HIGH)
            val aqiChannel = NotificationChannel(
                WeatherNotificationManager.CHANNEL_AQI,
                "Air Quality Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Warnings for unhealthy air quality"
                setSound(soundUri, audioAttributes)
                enableVibration(true)
            }

            // Register all channels with the OS
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
        scheduleWeatherNotificationWorker() // ← was defined but never called before

        setContent {
            val weatherViewModel: WeatherViewModel = hiltViewModel()
            val settingsViewModel: SettingsViewModel = hiltViewModel()

            val uiState by weatherViewModel.uiState.collectAsStateWithLifecycle()
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            val isOnboardingComplete by weatherViewModel.isOnboardingComplete.collectAsStateWithLifecycle(initialValue = false)


            WeatherTheme(darkTheme = settingsState.isDarkTheme) {

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
                    composable("settings_route") {
                        SettingsScreen(
                            navController = navController,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable("onboarding_route") {
                        OnboardingScreen(navController = navController)
                    }
                    composable("manage_locations") {
                        ManageLocationsScreen(
                            navController = navController,
                            onSearchClick = { navController.navigate("search_route") }
                        )
                    }
                    composable("search_route") {
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
                            }
                        )
                    }
                }
            }
        }
    }
}