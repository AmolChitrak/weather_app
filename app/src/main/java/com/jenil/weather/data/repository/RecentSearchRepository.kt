package com.jenil.weather.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.jenil.weather.domain.model.LocationSearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val RECENTS_KEY = stringSetPreferencesKey("recent_searches")
private const val MAX_RECENTS = 3

@Singleton
class RecentSearchesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val recentSearches: Flow<List<LocationSearchResult>> = dataStore.data.map { prefs ->
        prefs[RECENTS_KEY]
            ?.mapNotNull { entry ->
                runCatching {
                    val (indexStr, json) = entry.split("::", limit = 2)
                    indexStr.toInt() to Json.decodeFromString<LocationSearchResult>(json)
                }.getOrNull()
            }
            ?.sortedByDescending { it.first } // most recent first
            ?.map { it.second }
            ?: emptyList()
    }

    suspend fun addRecent(result: LocationSearchResult) {
        dataStore.edit { prefs ->
            val current = prefs[RECENTS_KEY].orEmpty()
                .mapNotNull { entry ->
                    runCatching {
                        val (indexStr, json) = entry.split("::", limit = 2)
                        indexStr.toInt() to Json.decodeFromString<LocationSearchResult>(json)
                    }.getOrNull()
                }
                .filter { it.second.id != result.id } // de-dupe, will re-add at top

            val nextIndex = (current.maxOfOrNull { it.first } ?: 0) + 1
            val updated = current + (nextIndex to result)
            val trimmed = updated.sortedByDescending { it.first }.take(MAX_RECENTS)

            prefs[RECENTS_KEY] = trimmed.map { (idx, loc) ->
                "$idx::${Json.encodeToString(loc)}"
            }.toSet()
        }
    }

    suspend fun clearAll() {
        dataStore.edit { it.remove(RECENTS_KEY) }
    }

    suspend fun removeOne(id: String) {
        dataStore.edit { prefs ->
            val current = prefs[RECENTS_KEY].orEmpty()
                .mapNotNull { entry ->
                    runCatching {
                        val (indexStr, json) = entry.split("::", limit = 2)
                        indexStr.toInt() to Json.decodeFromString<LocationSearchResult>(json)
                    }.getOrNull()
                }

            // Filter out the matching ID structurally rather than via string matching
            val filtered = current.filterNot { it.second.id.toString() == id }

            prefs[RECENTS_KEY] = filtered.map { (idx, loc) ->
                "$idx::${Json.encodeToString(loc)}"
            }.toSet()
        }
    }
}