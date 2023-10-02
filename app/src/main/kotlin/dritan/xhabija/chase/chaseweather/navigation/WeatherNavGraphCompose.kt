package dritan.xhabija.chase.chaseweather.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dritan.xhabija.chase.chaseweather.ui.home.MainScreen
import dritan.xhabija.chase.chaseweather.ui.search.SearchScreen

/**
 * Creates a dynamic nav graph according to the screens that need to be shown
 */
@Composable
fun WeatherNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            MainScreen(
                onNavigateToSearch = { navController.navigateTo(Screen.Search) },
                onNavigateToSettings = {}
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToHome = { navController.navigateTo(Screen.Home) },
                onNavigateToSettings = {}
            )
        }

        composable(Screen.Settings.route) {
        }
    }
}