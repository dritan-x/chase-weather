package dritan.xhabija.chase.chaseweather.location

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dritan.xhabija.chase.chaseweather.network.weather.WeatherConditions
import dritan.xhabija.chase.chaseweather.util.launchSafely
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    // cache location returned from fused location client
    private val _deviceLocation = MutableStateFlow(SimpleLocation())
    val deviceLocation: StateFlow<SimpleLocation>
        get() = _deviceLocation.asStateFlow()

    private val _deviceLocationWeatherConditions =
        MutableStateFlow(WeatherConditions.emptyInstance())
    val deviceLocationWeatherConditions: StateFlow<WeatherConditions>
        get() = _deviceLocationWeatherConditions.asStateFlow()
    private val context: Context
        get() = getApplication<Application>().applicationContext

    fun getCurrentLocationFlow(): StateFlow<SimpleLocation> {
        if (deviceLocation.value.isEmpty()) {
            // try getting location from fused client
            getCurrentLocation(context = context) { lat, long, exception ->
                viewModelScope.launchSafely {
                    _deviceLocation.emit(SimpleLocation(lat, long, exception))
                }.onException { throwable ->
                    println("dx. ERROR emitting SimpleLocation with ($lat, $long, $exception) ::::>> $throwable")
                }
            }
        }
        return deviceLocation
    }

    /**
     * Set last known device location coordinates in memory
     */
    fun setLastKnownLocation(simpleLocation: SimpleLocation) {
        viewModelScope.launchSafely {
            _deviceLocation.emit(simpleLocation)
        }.onException { throwable ->
            println("dx. ERROR emitting SimpleLocation $simpleLocation ::::>> $throwable")
        }
    }

    /**
     * Set api response WeatherConditions for current device location
     */
    fun setDeviceLocationWeatherConditions(weatherConditions: WeatherConditions){
        viewModelScope.launchSafely {
            _deviceLocationWeatherConditions.emit(weatherConditions)
        }.onException {
            println("dx. ERROR emitting location WeatherConditions $it")
        }
    }
}