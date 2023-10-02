package dritan.xhabija.chase.chaseweather.network.weather

import dritan.xhabija.chase.chaseweather.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("weather")
    suspend fun getWeatherConditionsForLatLon(@Query("lat") lat:Double, @Query("lon") lon:Double): WeatherConditions

    @GET("weather")
    suspend fun getWeatherConditionsForQuery(@Query("q") q:String): WeatherConditions
}

//
/**
 * Return the URL for fetching a weather icon. Optionally the size of the icon can be specified such as
 * https://openweathermap.org/img/wn/10d@2x.png --> iconId = 10d, size = @2x
 */
fun getWeatherImageUrl(iconId:String, size:String="") = BuildConfig.WEATHER_IMG_URL + "$iconId$size.png"

fun getWeatherImageUrl2x(iconId: String) = getWeatherImageUrl(iconId, "@2x")
fun getWeatherImageUrl3x(iconId: String) = getWeatherImageUrl(iconId, "@3x")
fun getWeatherImageUrl4x(iconId: String) = getWeatherImageUrl(iconId, "@4x")

/**
 * Given a degree orientation, return the direction of that degree
 */
// converted to Kotlin from https://stackoverflow.com/a/25867068
fun degToCompass(degrees:Double): String {
    val index = Math.floor((degrees / 22.5) + 0.5)
    var arr = listOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
    return arr[(index % 16).toInt()]
}