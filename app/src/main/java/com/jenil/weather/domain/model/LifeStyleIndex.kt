package com.jenil.weather.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class IndexLevel {
    LOW,
    MODERATE,
    HIGH,
    EXTREME
}

@Serializable
enum class IndexCategory {
    SKINCARE,
    DRIVING,
    CLOTHING,
    OUTDOOR_ACTIVITY
}

@Serializable
data class LifeStyleIndex (
    val category: IndexCategory,
    val title: String,
    val level: IndexLevel,
    val recommendation: String
)
