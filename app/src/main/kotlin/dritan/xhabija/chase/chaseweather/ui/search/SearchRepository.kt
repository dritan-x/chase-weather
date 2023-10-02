package dritan.xhabija.chase.chaseweather.ui.search

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dritan.xhabija.chase.chaseweather.ui.search.SearchRepository.Companion.PREFS_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map

/**
 * Search Repository persisting search query DataStore and having a "recent searches" list live in memory.
 */

private var recentTaskLock = false

class SearchRepository(private val context: Context) {
    // in memory cache
    private var lastSearch = ""

    /**
     * Keys for referencing data from DataStore
     */
    companion object {
        const val PREFS_NAME = "search_history"
        val LAST_SEARCH = stringPreferencesKey("last_search")
    }

    /*  In-memory cache for recent searches. It's not super important that this list get cleared on demand
        and can let it get cleared on next app restart (even though the clear function exists) */
    private val recentSearches = LinkedHashSet<String>()

    /**
     * Save last searched query, move previously searched query to "recents list" in-memory cache
     * In-memory cache doesn't make sense when we have to write to DataStore anyways and saving a read
     * operation for a single string does not need memory cache.
     */
    suspend fun setLastSearchedQuery(query: String) {
        if (query != lastSearch) {
            lastSearch = query
            context.dataStore.edit { prefs ->
                prefs[LAST_SEARCH] = query
            }
            recentSearches.add(query)
        }
    }

    /**
     * Get last searched query
     */
    fun getLastSearchFlow(): Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LAST_SEARCH] ?: ""
    }


    /**
     * Return recent list of searches
     */
    fun getRecentSearchesList() = recentSearches.toList()

    /**
     * Clear recent searches history
     */
    fun clearRecentSearches() = recentSearches.clear()
}

/**
 * Persisting search queries to disk using DataStore due to the nature of data being stored (not complex)
 * https://developer.android.com/topic/libraries/architecture/datastore
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFS_NAME)