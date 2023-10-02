package dritan.xhabija.chase.chaseweather.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dritan.xhabija.chase.chaseweather.network.weather.WeatherConditions
import dritan.xhabija.chase.chaseweather.network.weather.WeatherRepository
import dritan.xhabija.chase.chaseweather.location.SimpleLocation
import dritan.xhabija.chase.chaseweather.util.launchSafely
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// semaphores
private var fetchingWeatherApiLatLon = false
private var fetchingWeatherApiQuery = false

/**
 * ViewModel that is shared amongst any other screens that need to reference home.
 * i.e. any composable screen can reference this viewmodel for accessing a common property.
 */
class WeatherViewModel(private val weatherRepository: WeatherRepository) : ViewModel() {

    private val _weatherConditions = MutableStateFlow(WeatherConditions.emptyInstance())
    val weatherConditions: StateFlow<WeatherConditions>
        get() = _weatherConditions.asStateFlow()

    private val _weatherForLocation = MutableStateFlow(false)
    val weatherForLocation: StateFlow<Boolean>
        get() = _weatherForLocation.asStateFlow()


    private var searchQueryLock = false

    fun getWeatherForSimpleLocationFlow(
        // location lat/long coming from fused location client
        location: SimpleLocation,
        // user tapped on current location and wants to view the weather for that location
        viewWeatherForLocationRequest: Boolean = false
    ): StateFlow<WeatherConditions> {
        if (!fetchingWeatherApiLatLon) {
            fetchingWeatherApiLatLon = true
            viewModelScope.launchSafely {
                val weatherApiReply = weatherRepository.getWeatherForSimpleLocation(location)
                weatherApiReply?.let {// null entries = lot/lon passed is 0/0
                    _weatherConditions.emit(weatherApiReply)
                    _weatherForLocation.emit(viewWeatherForLocationRequest)
                }
                fetchingWeatherApiLatLon = false
            }.onException { throwable ->
                fetchingWeatherApiLatLon = false
            }

        }
        return weatherConditions
    }

    fun getWeatherForSearchQueryFlow(query: String): StateFlow<WeatherConditions> {
        if (!searchQueryLock) {
            searchQueryLock = true
            viewModelScope.launchSafely {
                val weatherApiReply = weatherRepository.getWeatherForQuery(query)
                searchQueryLock = false
                _weatherConditions.emit(weatherApiReply)
            }.onException { throwable ->
                println("dx. ERROR Running WeatherViewModel coroutine getWeatherForSearchQuery: $throwable")
                searchQueryLock = false
            }
        }
        return weatherConditions
    }

    fun getLastSearchedWeatherConditions() = weatherRepository.lastSearchedWeatherConditions
    fun clearDeviceLocation() {
        viewModelScope.launchSafely {
            _weatherConditions.emit(WeatherConditions.emptyInstance())
        }.onException {
            println("dx. ERROR weatherViewModel is unable to emit the 'clear' of weatherConditions flow")
        }
    }
}