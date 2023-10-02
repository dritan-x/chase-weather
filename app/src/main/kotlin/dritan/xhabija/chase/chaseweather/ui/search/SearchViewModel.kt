package dritan.xhabija.chase.chaseweather.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dritan.xhabija.chase.chaseweather.util.launchSafely
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest

class SearchViewModel(private val searchRepository: SearchRepository) : ViewModel() {

    private val _lastSearchedQuery = MutableStateFlow("")
    val lastSearchedQuery: StateFlow<String>
        get() = _lastSearchedQuery.asStateFlow()

    private val _recentSearches = MutableStateFlow(listOf(""))
    val recentSearches: StateFlow<List<String>>
        get() = _recentSearches.asStateFlow()


    fun getLastSearchedQueryFlow(): StateFlow<String> {
        viewModelScope.launchSafely {
            searchRepository.getLastSearchFlow().collect { lastSearchedText ->
                _lastSearchedQuery.emit(lastSearchedText)
            }
        }.onException { exception ->
            println("dx. ERROR get last searched query: $exception")
        }
        return lastSearchedQuery
    }

    /**
     * Set the last query that was executed. Also emit an updated recents list.
     */
    fun setLastSearchQuery(query: String) {
        viewModelScope.launchSafely {
            searchRepository.setLastSearchedQuery(query)
            getRecentSearchesFlow()
        }.onException { exception ->
            println("dx. ERROR setting last searched query '$query':: $exception")
        }
    }

    fun getRecentSearchesFlow(): StateFlow<List<String>> {
        val recentSearchHistory = searchRepository.getRecentSearchesList()
        viewModelScope.launchSafely {
            _recentSearches.emit(recentSearchHistory)
        }.onException { throwable ->
            println("dx. ERROR getting recent searches $throwable")
        }
        return recentSearches
    }
}