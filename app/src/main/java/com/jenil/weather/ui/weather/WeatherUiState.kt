package com.jenil.weather.ui.weather


import com.jenil.weather.domain.model.WeatherData

data class WeatherUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val data: WeatherData? = null,
    val error: String? = null,
    val isOffline: Boolean = false // ADDED OFFLINE FLAG
)