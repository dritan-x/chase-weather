package dritan.xhabija.chase.chaseweather.ui.search

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchRepositoryTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun `test setting -last search query- persists in DataStore `() = runTest{
        val searchRepository = SearchRepository(context)
        searchRepository.setLastSearchedQuery("ABC")
        context.dataStore.data.map { prefs ->
            prefs[SearchRepository.LAST_SEARCH] ?: ""
        }.collectLatest {storedQuery->
            assertEquals("Did ABC get stored properly?", "ABC", storedQuery)
        }
    }
}