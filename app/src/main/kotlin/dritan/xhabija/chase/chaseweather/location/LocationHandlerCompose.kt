package dritan.xhabija.chase.chaseweather.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import dritan.xhabija.chase.chaseweather.ui.home.WeatherViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * LocationHandler is responsible for acquiring the device's GPS coordinates and storing them in the
 * [LocationViewModel]. If permissions have previously been granted - it proceeds to extract
 * lat and long and provides a fresh location that can be requested from anywhere. Prompts the user
 * for coarse location accuracy. See manifest.xml for more on the permissions.
 */

// semaphore
private var locationTaskInProgress = false

@Composable
fun LocationHandler() {
    //// location-related
    val locationViewModel = koinViewModel<LocationViewModel>()
    // get currently cached SimpleLocation
    val currentLocation = locationViewModel.deviceLocation.collectAsState().value
    // we don't need to do any location-related operations if the previously cached location is still fresh
    if (!currentLocation.isStale()){
        return // early termination
    }

    // get currently cached WeatherConditions for the currentLocation
    val locationWeatherConditions =
        locationViewModel.deviceLocationWeatherConditions.collectAsState().value

    //// weather-related
    val weatherViewModel = koinViewModel<WeatherViewModel>()
    // get weather conditions returned by WeatherApi calls
    val weatherConditions = weatherViewModel.weatherConditions.collectAsState().value
    // if we received api reply
    if (weatherConditions.isNotEmpty()){
        weatherConditions.simpleLocation = currentLocation // attach device location to response object
        locationViewModel.setDeviceLocationWeatherConditions(weatherConditions)
        // update SimpleLocation with city name
        if (currentLocation.isNotEmpty()){
            currentLocation.cityName = weatherConditions.cityName
        }
        locationViewModel.setLastKnownLocation(currentLocation)
        weatherViewModel.clearDeviceLocation()
    }

    val context = LocalContext.current

    // Create a permission launcher
    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted: Boolean ->
                if (isGranted) { // location permissions were granted
                    // query for device GPS coordinates and update viewmodel, sets locationTaskInProgress = false
                    obtainAndCacheDeviceLocation(
                        context = context,
                        locationViewModel = locationViewModel,
                        weatherViewModel = weatherViewModel
                    )

                } else { // location permissions were denied
                    locationTaskInProgress = false
                }
            })


    // location permissions continue to remain granted
    if (grantedCoarseLocationPermission(context)) {
        if (!locationTaskInProgress) {
            locationTaskInProgress = true
            // get device location and cache in LocationViewModel, sets locationTaskInProgress = false
            obtainAndCacheDeviceLocation(context, locationViewModel, weatherViewModel)
        }
    } else { // we need location permissions
        if (!locationTaskInProgress) {
            locationTaskInProgress = true
            // Request location permission
            LaunchedEffect(key1 = "location permissions", block = {
                launch {
                    // locationTaskInProgress gets set to false in obtainAndCacheDeviceLocation()
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
            })
        }
    }
}

private fun obtainAndCacheDeviceLocation(
    context: Context,
    locationViewModel: LocationViewModel,
    weatherViewModel: WeatherViewModel,
) {
    // get device GPS coordinates
    getCurrentLocation(context) { lat, long, exception ->
        val currLocation =
            SimpleLocation(
                lat = lat,
                lon = long,
                exception = exception,
                timestamp = System.currentTimeMillis()
            )
        // cache these coordinates in memory
        locationViewModel.setLastKnownLocation(currLocation)

        // todo: evaluate which exceptions in the entire app should show a message to user and
        //  define a systematic exception handling paradigm
        exception?.let {
            Toast.makeText(context, "Location Permission Error: $exception", Toast.LENGTH_LONG)
                .show()
        }
        locationTaskInProgress = false
        weatherViewModel.getWeatherForSimpleLocationFlow(currLocation)
    }
}

/**
 * Check we have COARSE location access from user.
 */
fun grantedCoarseLocationPermission(context: Context) =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

/**
 * Get current location from fused location client
 */
fun getCurrentLocation(context: Context, callback: (Double, Double, Exception?) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val long = location.longitude
                callback(lat, long, null)
            } else {
                // no exception was encountered == app is running on emulator or we can't read data
//                callback(0.0, 0.0, null)
                callback(37.418700, -122.210735, null)
            }
        }
        .addOnFailureListener { exception ->
            callback(-1.0, -1.0, exception)
        }
}