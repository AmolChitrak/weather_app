package com.jenil.weather.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild

@Composable
fun Modifier.glassCard(
    hazeState: HazeState,
    shape: Shape
): Modifier {
    val isDark = WeatherTheme.colors.isDark

    // Deep mode-aware container tint for crisp frosted glass contrast
    val containerColor = if (isDark) {
        Color.White.copy(alpha = 0.04f)
    } else {
        Color.White.copy(alpha = 0.55f)
    }

    // Solid Grey border for dark mode
    val borderBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF383D45), // True, subtle grey
                Color(0xFF383D45)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.8f),
                Color.White.copy(alpha = 0.2f)
            )
        )
    }

    return this
        .clip(shape)
        .hazeChild(state = hazeState)
        .background(containerColor, shape)
        .border(width = 1.dp, brush = borderBrush, shape = shape)
}

@Composable
fun Modifier.glassBackdrop(hazeState: HazeState): Modifier {
    return this
}