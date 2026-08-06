package com.jenil.weather.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


data class WeatherExtendedColors(
    val isDark: Boolean,
    val onBackground: Color,
    val onSurface: Color,
    val surfaceCard: Color,
    val surfaceElevated: Color,
    val surfaceElevatedHigh: Color,
    val onSurfaceMuted: Color,
    val onSurfaceFaint: Color,
    val divider: Color,
    val cardBorder: Color,
    val navContainerBg: Color,
    val navSelectedBg: Color,
    val navSelectedIcon: Color,
    val navUnselectedIcon: Color,
    val accentSun: Color,
    val accentSky: Color,
    val accentRain: Color,
    val badgeRed: Color,
    val badgeGreen: Color,
    val gradientPressureTop: Color,
    val gradientPressureMid: Color,
    val gradientPressureBottom: Color,
    val graphLine: Color,
    val graphHighlightDot: Color
)

private val DarkExtendedColors = WeatherExtendedColors(
    isDark = true,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    surfaceCard = DarkSurfaceCard,
    surfaceElevated = DarkSurfaceElevated,
    surfaceElevatedHigh = DarkSurfaceElevatedHigh,
    onSurfaceMuted = DarkOnSurfaceMuted,
    onSurfaceFaint = DarkOnSurfaceFaint,
    divider = DarkDivider,
    cardBorder = DarkCardBorder,
    navContainerBg = DarkNavContainerBg,
    navSelectedBg = DarkNavSelectedBg,
    navSelectedIcon = DarkNavSelectedIcon,
    navUnselectedIcon = DarkNavUnselectedIcon,
    accentSun = AccentSun,
    accentSky = AccentSky,
    accentRain = AccentRain,
    badgeRed = AccentBadgeRed,
    badgeGreen = AccentBadgeGreen,
    gradientPressureTop = GradientPressureTop,
    gradientPressureMid = GradientPressureMid,
    gradientPressureBottom = GradientPressureBottom,
    graphLine = GraphLineColor,
    graphHighlightDot = GraphHighlightDot
)

private val LightExtendedColors = WeatherExtendedColors(
    isDark = false,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    surfaceCard = LightSurfaceCard,
    surfaceElevated = LightSurfaceElevated,
    surfaceElevatedHigh = LightSurfaceElevatedHigh,
    onSurfaceMuted = LightOnSurfaceMuted,
    onSurfaceFaint = LightOnSurfaceFaint,
    divider = LightDivider,
    cardBorder = LightCardBorder,
    navContainerBg = LightNavContainerBg,
    navSelectedBg = LightNavSelectedBg,
    navSelectedIcon = LightNavSelectedIcon,
    navUnselectedIcon = LightNavUnselectedIcon,
    accentSun = AccentSun,
    accentSky = AccentSky,
    accentRain = AccentRain,
    badgeRed = AccentBadgeRed,
    badgeGreen = AccentBadgeGreen,
    gradientPressureTop = GradientPressureTop,
    gradientPressureMid = GradientPressureMid,
    gradientPressureBottom = GradientPressureBottom,
    graphLine = GraphLineColorLight,
    graphHighlightDot = GraphHighlightDot
)
 val DarkColors = darkColorScheme(
    primary = DarkOnSurface,
    onPrimary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurfaceCard,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkOnSurfaceMuted,
    outline = DarkDivider,
    error = AccentBadgeRed
)
 val LightColors = lightColorScheme(
    primary = LightOnSurface,
    onPrimary = LightBackground,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurfaceCard,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightOnSurfaceMuted,
    outline = LightDivider,
    error = AccentBadgeRed
)

val LocalWeatherColors = staticCompositionLocalOf { DarkExtendedColors }

@Composable
fun WeatherAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                    !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalWeatherColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WeatherTypography,
            shapes = WeatherShapes,
            content = content
        )
    }
}

object WeatherTheme {
    val colors: WeatherExtendedColors
        @Composable
        get() = LocalWeatherColors.current

    val typography = WeatherTypography
    val extraType = WeatherExtraType
    val shapes = WeatherShapes
    val extraShapes = WeatherExtraShapes
}