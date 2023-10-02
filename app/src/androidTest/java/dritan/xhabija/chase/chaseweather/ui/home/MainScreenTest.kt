package dritan.xhabija.chase.chaseweather.ui.home

import android.os.SystemClock.sleep
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import dritan.xhabija.chase.chaseweather.MainActivity
import org.junit.Rule
import org.junit.Test

/**
 * Tests that MainScreen's Search Field is visible and that when tapping on it takes you to the Search screen.
 */
class MainScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule(MainActivity::class.java)

    @Test
    fun `test that search field is visible`(){
        val searchField = composeTestRule.onNode(hasTestTag("MainScreenSearchField"), useUnmergedTree = true)
        searchField.assertIsDisplayed()
    }

    @Test
    fun `test that tapping on search field shows search screen`(){
        val searchField = composeTestRule.onNode(hasTestTag("MainScreenSearchField"), useUnmergedTree = true)
        searchField.assertIsDisplayed()
        searchField.performClick()
        sleep(2000)
        val searchScreenGoButton = composeTestRule.onNode(hasTestTag("GoButton"))
        searchScreenGoButton.assertIsDisplayed()
    }
}