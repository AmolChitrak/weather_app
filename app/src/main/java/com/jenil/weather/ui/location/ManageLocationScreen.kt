package com.jenil.weather.ui.location

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.jenil.weather.domain.model.LocationSearchResult
import com.jenil.weather.ui.core.Screen
import com.jenil.weather.ui.core.SettingsGroupTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageLocationsScreen(
    navController: NavController,
    onSearchClick: () -> Unit = {},
    viewModel: ManageLocationsViewModel = hiltViewModel()
) {
    val favorites by viewModel.favoriteLocations.collectAsStateWithLifecycle()
    val offlineName by viewModel.offlineLocationName.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Manage Locations",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onSearchClick() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Outlined.Search, contentDescription = "Add Location")
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // --- OFFLINE LOCATION STATUS ---
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsGroupTitle(title = "Offline Fallback")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Currently caching:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = offlineName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // --- FAVORITES LIST ---
                item { SettingsGroupTitle(title = "Saved Locations") }

                if (favorites.isEmpty()) {
                    item {
                        Text(
                            text = "No saved locations yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, start = 8.dp)
                        )
                    }
                } else {
                    items(favorites, key = { it.id }) { location ->
                        FavoriteLocationItem(
                            location = location,
                            onSetOffline = { viewModel.setOfflineLocation(it) },
                            onDelete = { viewModel.removeFavorite(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteLocationItem(
    location: LocationSearchResult,
    onSetOffline: (LocationSearchResult) -> Unit,
    onDelete: (LocationSearchResult) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = location.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (location.state.isNotEmpty() || location.country.isNotEmpty()) {
                Text(
                    text = "${location.state}, ${location.country}".trim(',', ' '),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Button to set as offline fallback
            IconButton(onClick = { onSetOffline(location) }) {
                Icon(
                    imageVector = Icons.Outlined.CloudDownload,
                    contentDescription = "Set Offline",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            // Button to delete
            IconButton(onClick = { onDelete(location) }) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}