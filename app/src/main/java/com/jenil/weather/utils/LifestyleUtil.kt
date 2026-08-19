package com.jenil.weather.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Face
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.jenil.weather.domain.model.IndexCategory
import com.jenil.weather.domain.model.IndexLevel


fun IndexLevel.getBadgeColor(): Color = when (this) {
    IndexLevel.LOW -> Color(0xFF4CAF50)      // Green
    IndexLevel.MODERATE -> Color(0xFFFF9800) // Orange
    IndexLevel.HIGH -> Color(0xFFF44336)     // Red
    IndexLevel.EXTREME -> Color(0xFF9C27B0)  // Purple
}

// Icon mapping based on the category
fun IndexCategory.getIcon(): ImageVector = when (this) {
    IndexCategory.SKINCARE -> Icons.Rounded.Face
    IndexCategory.DRIVING -> Icons.Rounded.DirectionsCar
    IndexCategory.CLOTHING -> Icons.Rounded.Checkroom
    IndexCategory.OUTDOOR_ACTIVITY -> Icons.AutoMirrored.Rounded.DirectionsRun
}