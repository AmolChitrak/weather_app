package com.jenil.weather.ui.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.jenil.weather.ui.core.SettingsClickableRow
import com.jenil.weather.ui.core.SettingsGroupCard
import com.jenil.weather.ui.core.SettingsGroupTitle
import com.jenil.weather.ui.core.SettingsRowDivider
import com.jenil.weather.ui.core.SettingsToggleRow
import com.jenil.weather.ui.core.UnitSegmentedRow
import com.jenil.weather.ui.theme.WeatherTheme
import com.jenil.weather.ui.theme.glassBackdrop
import dev.chrisbanes.haze.rememberHazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showClearCacheDialog by remember { mutableStateOf(false) }
    val hazeState = rememberHazeState()
    val isDark = WeatherTheme.colors.isDark

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

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            icon = {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    "Clear cached data?",
                    style = WeatherTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Removes offline forecast data. Your saved locations and settings stay untouched.",
                    style = WeatherTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearCachedData()
                    showClearCacheDialog = false
                }) {
                    Text("Clear", style = WeatherTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel", style = WeatherTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .glassBackdrop(hazeState)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "Settings",
                    style = WeatherTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
            ) {
                item { SettingsGroupTitle(title = "Units") }
                item {
                    SettingsGroupCard(hazeState) {
                        UnitSegmentedRow(
                            icon = Icons.Outlined.Thermostat,
                            title = "Temperature",
                            options = listOf("°C", "°F"),
                            selectedIndex = if (uiState.isCelsius) 0 else 1,
                            onSelect = { viewModel.updateTemperatureUnit(it == 0) }
                        )
                        SettingsRowDivider()
                        UnitSegmentedRow(
                            icon = Icons.Outlined.Air,
                            title = "Wind speed",
                            options = listOf("km/h", "mph"),
                            selectedIndex = if (uiState.isKmh) 0 else 1,
                            onSelect = { viewModel.updateWindSpeedUnit(it == 0) }
                        )
                        SettingsRowDivider()
                        UnitSegmentedRow(
                            icon = Icons.Outlined.WaterDrop,
                            title = "Show precipitation",
                            options = listOf("mm", "in"),
                            selectedIndex = if (uiState.isPrecipitationMm) 0 else 1,
                            onSelect = { viewModel.updatePrecipitationUnit(it == 0) }
                        )
                        SettingsRowDivider()
                        UnitSegmentedRow(
                            icon = Icons.Outlined.Compress,
                            title = "Pressure",
                            options = PressureUnit.entries.map { it.label },
                            selectedIndex = uiState.pressureUnit.ordinal,
                            onSelect = { viewModel.updatePressureUnit(PressureUnit.entries[it]) }
                        )
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }

                item { SettingsGroupTitle(title = "Notifications") }
                item {
                    SettingsGroupCard(hazeState) {
                        SettingsToggleRow(
                            icon = Icons.Outlined.Notifications,
                            title = "Enable notifications",
                            subtitle = if (uiState.isNotificationsEnabled) "On" else "Off",
                            isChecked = uiState.isNotificationsEnabled,
                            onCheckedChange = { viewModel.updateNotificationsEnabled(it) }
                        )
                    }
                }

                item { Spacer(Modifier.height(12.dp)) }

                item { SettingsGroupTitle(title = "Daily briefs") }
                item {
                    SettingsGroupCard(hazeState) {
                        SettingsToggleRow(
                            icon = Icons.Outlined.WbSunny,
                            title = "Morning brief",
                            subtitle = "Around 8:00 AM",
                            isChecked = uiState.isMorningBriefEnabled,
                            onCheckedChange = { viewModel.updateMorningBriefEnabled(it) },
                            enabled = uiState.isNotificationsEnabled
                        )
                        SettingsRowDivider()
                        SettingsToggleRow(
                            icon = Icons.Outlined.NightsStay,
                            title = "Evening brief",
                            subtitle = "Around 8:00 PM",
                            isChecked = uiState.isEveningBriefEnabled,
                            onCheckedChange = { viewModel.updateEveningBriefEnabled(it) },
                            enabled = uiState.isNotificationsEnabled
                        )
                    }
                }

                item { Spacer(Modifier.height(12.dp)) }

                item { SettingsGroupTitle(title = "Weather alerts") }
                item {
                    SettingsGroupCard(hazeState) {
                        SettingsToggleRow(
                            icon = Icons.Outlined.WaterDrop,
                            title = "Rain",
                            subtitle = "Before it arrives",
                            isChecked = uiState.isRainAlertsEnabled,
                            onCheckedChange = { viewModel.updateRainAlertsEnabled(it) },
                            enabled = uiState.isNotificationsEnabled
                        )
                        AnimatedVisibility(
                            visible = uiState.isRainAlertsEnabled && uiState.isNotificationsEnabled,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                SettingsRowDivider()
                                UnitSegmentedRow(
                                    icon = Icons.Outlined.Timer,
                                    title = "Lead time",
                                    options = listOf("15 min", "30 min", "45 min"),
                                    selectedIndex = when (uiState.rainLeadTimeMinutes) {
                                        15 -> 0
                                        45 -> 2
                                        else -> 1
                                    },
                                    onSelect = { index ->
                                        val minutes = when (index) {
                                            0 -> 15; 2 -> 45; else -> 30
                                        }
                                        viewModel.updateRainLeadTime(minutes)
                                    }
                                )
                            }
                        }
                        SettingsRowDivider()
                        SettingsToggleRow(
                            icon = Icons.Outlined.Eco,
                            title = "Air quality",
                            subtitle = "When AQI is unhealthy",
                            isChecked = uiState.isAqiAlertsEnabled,
                            onCheckedChange = { viewModel.updateAqiAlertsEnabled(it) },
                            enabled = uiState.isNotificationsEnabled
                        )
                        SettingsRowDivider()
                        SettingsToggleRow(
                            icon = Icons.Outlined.Air,
                            title = "High wind",
                            subtitle = "During strong gusts",
                            isChecked = uiState.isWindAlertsEnabled,
                            onCheckedChange = { viewModel.updateWindAlertsEnabled(it) },
                            enabled = uiState.isNotificationsEnabled
                        )
                        SettingsRowDivider()
                        SettingsToggleRow(
                            icon = Icons.Outlined.Thermostat,
                            title = "Extreme temperature",
                            subtitle = "Heat or cold extremes",
                            isChecked = uiState.isTemperatureAlertsEnabled,
                            onCheckedChange = { viewModel.updateTemperatureAlertsEnabled(it) },
                            enabled = uiState.isNotificationsEnabled
                        )
                    }
                }

                item { Spacer(Modifier.height(12.dp)) }

                item { SettingsGroupTitle(title = "Smart tips") }
                item {
                    SettingsGroupCard(hazeState) {
                        SettingsToggleRow(
                            icon = Icons.Outlined.TipsAndUpdates,
                            title = "Recommendations",
                            subtitle = "UV, clothing & activity tips",
                            isChecked = uiState.isSmartRecommendationsEnabled,
                            onCheckedChange = { viewModel.updateSmartRecommendationsEnabled(it) },
                            enabled = uiState.isNotificationsEnabled
                        )
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }

                item { SettingsGroupTitle(title = "Location") }
                item {
                    SettingsGroupCard(hazeState) {
                        SettingsToggleRow(
                            icon = Icons.Outlined.MyLocation,
                            title = "Use current location",
                            subtitle = "Update forecasts automatically as you move",
                            isChecked = uiState.useCurrentLocation,
                            onCheckedChange = { viewModel.updateLocationPreference(it) }
                        )
                        SettingsRowDivider()
                        SettingsClickableRow(
                            icon = Icons.Outlined.Star,
                            title = "Manage saved locations",
                            subtitle = "Offline: ${uiState.fallbackLocationName}",
                            onClick = { navController.navigate("manage_locations") }
                        )
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }

                item { SettingsGroupTitle(title = "Appearance") }
                item {
                    SettingsGroupCard(hazeState) {
                        SettingsToggleRow(
                            icon = if (uiState.isDarkTheme) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                            title = "Dark theme",
                            subtitle = if (uiState.isDarkTheme) "On" else "Off",
                            isChecked = uiState.isDarkTheme,
                            onCheckedChange = { viewModel.updateTheme(it) }
                        )
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }

                item { SettingsGroupTitle(title = "About") }
                item {
                    SettingsGroupCard(hazeState) {
                        SettingsClickableRow(
                            icon = Icons.Outlined.Info,
                            title = "Version",
                            subtitle = "1.0.0",
                            onClick = {
                                Toast.makeText(context, "Stable release", Toast.LENGTH_SHORT).show()
                            }
                        )
                        SettingsRowDivider()
                        SettingsClickableRow(
                            icon = Icons.Outlined.DeleteOutline,
                            title = "Clear cached data",
                            subtitle = "Free up storage",
                            titleColor = MaterialTheme.colorScheme.error,
                            onClick = { showClearCacheDialog = true }
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Weatherly",
                            style = WeatherTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Skies, simplified.",
                            style = WeatherTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}