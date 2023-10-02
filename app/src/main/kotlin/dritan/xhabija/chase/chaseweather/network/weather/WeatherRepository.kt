package dritan.xhabija.chase.chaseweather.network.weather

import dritan.xhabija.chase.chaseweather.location.SimpleLocation
import java.time.Duration


/**
 * Repository responsible for returning the cached api response if the data isn't stale,
 * else, makes new network request.
 */
class WeatherRepository(
    val api: WeatherApiService,
) {
    companion object {
        val MAX_AGE_WEATHER_REPLY = Duration.ofMinutes(5)
    }

    /*  Retain a cache of last fetched WeatherConditions
        Execute API call only if it's been at least 5 minutes
            --> this value should be adjusted according to the frequency of openweathermap.org's
             sources provide updates */
    var lastSearchedWeatherConditions: WeatherConditions? = null
        private set

    /**
     * Given latitude and longitude params, return a freshly cached object or make an api call to get a fresh object.
     */
    suspend fun getWeatherForSimpleLocation(location: SimpleLocation): WeatherConditions? {
        // check we have a valid cached object that also has a matching SimpleLocation
        lastSearchedWeatherConditions?.let { weatherConditions ->
            if (!weatherConditions.isStale()) { // if data is still fresh
                weatherConditions.simpleLocation?.let { prevDeviceLocation -> // check if lat & lon match
                    if (prevDeviceLocation.lat == location.lat && prevDeviceLocation.lon == location.lon) {
                        return weatherConditions // return cached
                    }
                }
            }
        }
        if (location.isEmpty()) {
            return null
        }
        val freshObject = api.getWeatherConditionsForLatLon(location.lat, location.lon)
        freshObject.simpleLocation = location // set this device's location object
        lastSearchedWeatherConditions =
            freshObject // update last searched object to be the fresh one
        return freshObject
    }

    /**
     * Given a search query, return a matching cached response that isn't stale or make an api call to get a fresh object.
     */
    suspend fun getWeatherForQuery(q: String): WeatherConditions {
        // check we have a valid (not stale) cached object that also has a matching query, return cache
        lastSearchedWeatherConditions?.let { weatherConditions ->
            if (!weatherConditions.isStale() && weatherConditions.searchQuery == q) {
                return weatherConditions
            }
        }
        // force to query for cities that are US-Based only
        val freshObject = api.getWeatherConditionsForQuery("$q,US")
        freshObject.searchQuery = q
        lastSearchedWeatherConditions = freshObject
        return freshObject
    }
}