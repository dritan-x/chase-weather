package dritan.xhabija.chase.chaseweather.navigation

import androidx.compose.runtime.Composable

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import dritan.xhabija.chase.chaseweather.ui.home.MainScreen

/**
 * Defines a screen of the app that can be navigated to.
 */
sealed class Screen(
    val route: String,
) {
    object Home : Screen(route = "home_route")
    object Search : Screen(route = "search_route")
    object Settings : Screen(route = "settings_route")
    object Location : Screen(route = "location_route")
}

/**
 * Helper extension function of NavController for navigating to a Screen directly without having to
 * manually specify the route of the Screen.
 */
fun NavController.navigateTo(screen: Screen) = navigate(screen.route)