package dritan.xhabija.chase.chaseweather.network.weather

import com.google.gson.annotations.SerializedName
import dritan.xhabija.chase.chaseweather.location.SimpleLocation
import dritan.xhabija.chase.chaseweather.network.weather.WeatherRepository.Companion.MAX_AGE_WEATHER_REPLY
import java.time.Duration

data class WeatherConditions(
    @SerializedName("coord")
    val coordindates: ApiCoord,
    @SerializedName("weather")
    val weather: List<ApiWeather>,
    @SerializedName("main")
    val main: ApiMain,
    @SerializedName("visibility")
    val visibility: Int,
    @SerializedName("wind")
    val wind: ApiWind,
    @SerializedName("clouds")
    val clouds: ApiClouds,
    @SerializedName("dt")
    val timeOfCalculation: Long,
    @SerializedName("sys")
    val sys: ApiSys,
    @SerializedName("timezone")
    val timezone: Int, // shift in seconds from UTC
    @SerializedName("id")
    val cityId: Int,
    @SerializedName("name")
    val cityName: String,
    // search query used for backend to return this object reply
    var searchQuery: String = "",
    // attach a SimpleLocation object if the query was lat/lon
    var simpleLocation: SimpleLocation? = null
) {
    companion object {
        fun emptyInstance() = WeatherConditions(
            coordindates = ApiCoord(0.0, 0.0),
            weather = listOf(ApiWeather(0, "", "", "")),
            main = ApiMain(0.0, 0.0, 0.0, 0.0, 0, 0),
            visibility = 0,
            wind = ApiWind(0.0, 0.0),
            clouds = ApiClouds(all = 0.0),
            timeOfCalculation = 0,
            sys = ApiSys("", 0L, 0L),
            timezone = 0,
            cityId = 0,
            cityName = ""
        )
    }

    /**
     * Return whether this object is "empty" -- without coordinates the weather is meaningless and
     * therefore the object is considered empty.
     */
    fun isEmpty() = coordindates.isEmpty()

    fun isNotEmpty() = !isEmpty()

    /**
     * Return whether this object is older than the specified duration.
     */
    fun isStale(duration: Duration = MAX_AGE_WEATHER_REPLY): Boolean {
        val age = System.currentTimeMillis() - timeOfCalculation
        return age > duration.toMillis()
    }
}

data class ApiCoord(
    val lat: Double,
    val lon: Double
) {
    fun isEmpty() = lat == 0.0 && lon == 0.0
}

data class ApiWeather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
) {
    fun isEmpty() = id == 0 && main == "" && description == "" && icon == ""
}

data class ApiMain(
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    @SerializedName("temp_min")
    val tempMin: Double,
    @SerializedName("temp_max")
    val tempMax: Double,
    val pressure: Int,
    val humidity: Int
) {
    fun isEmpty() =
        temp == 0.0 && feelsLike == 0.0 && tempMin == 0.0 && tempMax == 0.0 && pressure == 0 && humidity == 0
}

data class ApiWind(
    val speed: Double,
    @SerializedName("deg")
    val degrees: Double
)

data class ApiClouds(
    val all: Double
)

data class ApiSys(
    val country: String,
    val sunrise: Long,
    val sunset: Long
)