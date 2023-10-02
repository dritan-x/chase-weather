package dritan.xhabija.chase.chaseweather

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import dritan.xhabija.chase.chaseweather.navigation.WeatherNavGraph
import dritan.xhabija.chase.chaseweather.ui.theme.ChaseWeatherTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChaseWeatherTheme {
                WeatherNavGraph(navController = rememberNavController())
            }
        }
    }
}

/**
 * Extension function for getting single activity during the Home compose screens (aka MainScreen)
 * so user can exit the app if they're pressing back button on Home screen.
 */
fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}