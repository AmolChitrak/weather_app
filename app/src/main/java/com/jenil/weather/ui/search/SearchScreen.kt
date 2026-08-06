package com.jenil.weather.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.jenil.weather.domain.model.LocationSearchResult
import com.jenil.weather.ui.core.CurrentLocationCard
import com.jenil.weather.ui.core.EmptyState
import com.jenil.weather.ui.core.LocationListItem
import com.jenil.weather.ui.core.RecentSearchItem
import com.jenil.weather.ui.core.SwipeToRemoveFavorite
import com.jenil.weather.ui.theme.WeatherTheme
import com.jenil.weather.ui.theme.glassBackdrop
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    onLocationSelected: (lat: Double, lon: Double, cityName: String) -> Unit,
    hazeState: HazeState,
    viewModel: WeatherSearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val favoriteLocations by viewModel.favoriteLocations.collectAsState()
    val isFetchingLocation by viewModel.isFetchingLocation.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val locationError by viewModel.locationError.collectAsState()
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

    LaunchedEffect(locationError) {
        locationError?.let { errorMsg ->
            snackbarHostState.showSnackbar(
                message = errorMsg,
                duration = SnackbarDuration.Short
            )
        }
    }

    fun navigateWithResult(result: LocationSearchResult) {
        viewModel.addRecentSearch(result)
        onLocationSelected(result.latitude, result.longitude, result.name)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .glassBackdrop(hazeState)
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    TextField(
                        value = searchQuery,
                        singleLine = true,
                        onValueChange = viewModel::onSearchQueryChanged,
                        placeholder = {
                            Text(
                                text = "Search city, e.g. Ahmedabad",
                                style = WeatherTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = searchQuery.isNotEmpty(),
                                enter = fadeIn(tween(150)),
                                exit = fadeOut(tween(150))
                            ) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    )
                }
            }
        ) { innerPadding ->
            val screenState = when {
                isLoading -> "loading"
                searchResults.isNotEmpty() -> "results"
                searchQuery.isNotBlank() -> "no_results"
                else -> "initial"
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(
                    targetState = screenState,
                    animationSpec = tween(220),
                    label = "search_screen_state"
                ) { state ->
                    when (state) {
                        "loading" -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        strokeWidth = 3.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        text = "Searching cities…",
                                        style = WeatherTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        "results" -> {
                            Column(Modifier.fillMaxSize()) {
                                Text(
                                    text = "${searchResults.size} result${if (searchResults.size == 1) "" else "s"}",
                                    style = WeatherTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
                                )
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(bottom = 100.dp)
                                ) {
                                    items(searchResults, key = { it.id }) { result ->
                                        val isFavorite = favoriteLocations.any { it.id == result.id }
                                        LocationListItem(
                                            result = result,
                                            isFavorite = isFavorite,
                                            hazeState = hazeState,
                                            highlightQuery = searchQuery,
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                                            onClick = { navigateWithResult(result) },
                                            onFavoriteToggle = { viewModel.toggleFavorite(result, isFavorite) }
                                        )
                                    }
                                }
                            }
                        }

                        "no_results" -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp, vertical = 32.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                EmptyState(
                                    icon = Icons.Default.SearchOff,
                                    title = "No cities found",
                                    subtitle = "Try a different spelling or a nearby city",
                                    hazeState = hazeState
                                )
                            }
                        }

                        "initial" -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 100.dp)
                            ) {
                                item {
                                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                                        CurrentLocationCard(
                                            isLoading = isFetchingLocation,
                                            hazeState = hazeState,
                                            onClick = {
                                                viewModel.fetchCurrentLocation { lat, lon, name ->
                                                    onLocationSelected(lat, lon, name)
                                                }
                                            }
                                        )
                                    }
                                }

                                if (recentSearches.isNotEmpty()) {
                                    item {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 24.dp, top = 20.dp, end = 20.dp, bottom = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "RECENT SEARCHES",
                                                style = WeatherTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            TextButton(onClick = { viewModel.clearRecentSearches() }) {
                                                Text(
                                                    text = "Clear all",
                                                    style = WeatherTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                    items(recentSearches, key = { "recent_${it.id}" }) { result ->
                                        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                                            RecentSearchItem(
                                                result = result,
                                                hazeState = hazeState,
                                                onClick = { navigateWithResult(result) },
                                                onRemove = { viewModel.removeRecentSearch(result.id) },
                                                onUseQuery = { name -> viewModel.onSearchQueryChanged(name) }
                                            )
                                        }
                                    }
                                }

                                if (favoriteLocations.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "SAVED LOCATIONS",
                                            style = WeatherTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.5.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp)
                                        )
                                    }
                                    items(favoriteLocations, key = { "fav_${it.id}" }) { result ->
                                        SwipeToRemoveFavorite(
                                            onRemove = {
                                                viewModel.toggleFavorite(result, true)
                                                coroutineScope.launch {
                                                    val action = snackbarHostState.showSnackbar(
                                                        message = "${result.name} removed",
                                                        actionLabel = "Undo",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                    if (action == SnackbarResult.ActionPerformed) {
                                                        viewModel.toggleFavorite(result, false)
                                                    }
                                                }
                                            }
                                        ) {
                                            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                                                LocationListItem(
                                                    result = result,
                                                    isFavorite = true,
                                                    hazeState = hazeState,
                                                    onClick = { navigateWithResult(result) },
                                                    onFavoriteToggle = { viewModel.toggleFavorite(result, true) }
                                                )
                                            }
                                        }
                                    }
                                }

                                if (recentSearches.isEmpty() && favoriteLocations.isEmpty()) {
                                    item {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 64.dp, start = 20.dp, end = 20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(72.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Search,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(32.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            }
                                            Spacer(Modifier.height(16.dp))
                                            Text(
                                                text = "Search for a city",
                                                style = WeatherTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}