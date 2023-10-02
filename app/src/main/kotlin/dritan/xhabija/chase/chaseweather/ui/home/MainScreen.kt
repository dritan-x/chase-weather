package dritan.xhabija.chase.chaseweather.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import dritan.xhabija.chase.chaseweather.findActivity
import dritan.xhabija.chase.chaseweather.network.weather.WeatherConditions
import dritan.xhabija.chase.chaseweather.network.weather.degToCompass
import dritan.xhabija.chase.chaseweather.network.weather.getWeatherImageUrl4x
import dritan.xhabija.chase.chaseweather.location.LocationHandler
import dritan.xhabija.chase.chaseweather.location.LocationViewModel
import dritan.xhabija.chase.chaseweather.location.SimpleLocation
import dritan.xhabija.chase.chaseweather.ui.search.SearchViewModel
import dritan.xhabija.chase.chaseweather.ui.theme.PurpleGrey40
import dritan.xhabija.chase.chaseweather.ui.theme.MainScreenSearchField
import dritan.xhabija.chase.chaseweather.ui.theme.PurpleGrey80
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

private var processingWeatherTask = false

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val locationViewModel = koinViewModel<LocationViewModel>()
    val weatherViewModel = koinViewModel<WeatherViewModel>()
    val searchViewModel = koinViewModel<SearchViewModel>()

    val context = LocalContext.current

    // get last known location
    val currLocation = locationViewModel.getCurrentLocationFlow().collectAsState().value

    // location weather conditions - api response for device's lat/lon
    var locationWeatherConditions =
        locationViewModel.deviceLocationWeatherConditions.collectAsState().value
    // get last searched query that has been persisted in DataStore
    val lastSearchedQuery = searchViewModel.getLastSearchedQueryFlow().collectAsState().value
    // get weather conditions returned by WeatherApi calls
    val weatherConditions = weatherViewModel.weatherConditions.collectAsState().value
    // request to view the current location's weather
    val viewWeatherForLocation = weatherViewModel.weatherForLocation.collectAsState().value

    // weather conditions to use for main screen
    var mainScreenWeatherConditions = weatherConditions

    if (!viewWeatherForLocation) { // flag set along with weatherConditions by default
        if ((weatherConditions.isEmpty() && !processingWeatherTask) || weatherConditions.cityName != lastSearchedQuery) {
            // if no query has been searched yet
            if (lastSearchedQuery.isEmpty()) {
                // see if we can use a cached WeatherConditions for current location
                if (locationWeatherConditions.isNotEmpty()) {
                    mainScreenWeatherConditions = locationWeatherConditions
                } else if (currLocation.isNotEmpty()) { // we don't have a cache of WeatherConditions, fetch a fresh reply of currentLocation using weatherViewModel
                    // updates weatherViewModel.weatherConditions
                    processingWeatherTask = true
                    // get WeatherConditions object delivered to `weatherConditions` above
                    weatherViewModel.getWeatherForSimpleLocationFlow(currLocation)
                } // else, location permissions were denied or unable to get coordinates from fused location client
            } else { // we have a persisted query - city name
                // get weather for last searched query, updates weatherViewModel.weatherConditions
                processingWeatherTask = true
                weatherViewModel.getWeatherForSearchQueryFlow(lastSearchedQuery)
            }
        } else {
            processingWeatherTask = false
            if (lastSearchedQuery.isNotEmpty() && weatherConditions.cityName.lowercase() == lastSearchedQuery.lowercase()) {
                weatherViewModel.getWeatherForSearchQueryFlow(lastSearchedQuery)
            }
        }
    }
    // searchbar text
    var selectedCity = "Search a US city"

    mainScreenWeatherConditions?.let {
        if (it.cityName.isNotEmpty()) {
            selectedCity = it.cityName
        }
    }

    // handle user pressing back button while on the Home screen, exit app, i.e. finish single activity
    BackHandler { context.findActivity()?.finish() }

    /* to prevent from getting location updates whenever this screen recomposes we set a staleness
    on the location object and get location updates once every 20 minutes (see currLocation.isStale())
     */
    if (currLocation.isStale()) {
        // obtains GPS coordinates from device and stores in LocationViewModel
        LocationHandler()
    }

    // MainScreen layout begin
    Column {
        OutlinedTextField(
            modifier = Modifier
                .background(MainScreenSearchField)
                .fillMaxWidth()
                .clickable {
                    onNavigateToSearch()
                }
                .border(
                    width = 3.dp,
                    color = Color.Black
                )
                .testTag("mainScreenSearchField"),
            value = selectedCity, onValueChange = {},
            textStyle = TextStyle(
                color = PurpleGrey40,
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp
            ),
            enabled = false,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search for a US city",
//                    modifier = Modifier .border(1.dp, Color.Red, shape = CircleShape),
                    tint = Color.Black
                )
            }
        )

        if (currLocation.isEmpty() || mainScreenWeatherConditions.isEmpty()) {
            Text("Get the weather for a US city by searching above!")
        } else {
            ForecastDetails(weatherConditions = mainScreenWeatherConditions)
        }
    }
}


/**
 * Show weather forecast details for the given [WeatherConditions]
 */
@Composable
fun ForecastDetails(weatherConditions: WeatherConditions) {

    val weather = weatherConditions.weather[0]
    val main = weatherConditions.main
    val wind = weatherConditions.wind
    val painter =
        rememberImagePainter(data = getWeatherImageUrl4x(weather.icon))

    val context = LocalContext.current
    Column(
        modifier = Modifier
            .background(PurpleGrey80)
            .padding(paddingValues = PaddingValues(10.dp, 20.dp, 10.dp, 20.dp))
            .fillMaxWidth()

    ) {
        Text("Current temperature ${main.temp}F, feels like ${main.feelsLike}")
        Text("Today's low ${main.tempMin}, high ${main.tempMax}")
        Text("Forecast of ${weather.description}")
        Image(
            painter = painter,
            contentDescription = "${weather.description}",
            contentScale = ContentScale.Inside
        )
        Text("Wind speed ${wind.speed}mph, from ${degToCompass(wind.degrees)}")
        Text("Humidity ${main.humidity}%")
        Text("Barometric pressure of ${main.pressure}hPa")
        Text(
            "GPS Location ${weatherConditions.coordindates.lat}, ${weatherConditions.coordindates.lon}",
            modifier = Modifier.clickable {
                launchLocationOnMaps(
                    weatherConditions.coordindates.lat,
                    weatherConditions.coordindates.lon,
                    context
                )
            },
            style = TextStyle(textDecoration = TextDecoration.Underline, color = Color.Blue)
        )
    }
}

fun launchLocationOnMaps(lat: Double, lon: Double, context: Context) {
    val uri = String.format(Locale.US, "geo:%f,%f", lat, lon)
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    context.startActivity(intent);
}