package dritan.xhabija.chase.chaseweather.location

import java.lang.Exception
import java.time.Duration

/**
 * Location object containing lat & lon as returned by fused location client.
 */
data class SimpleLocation(
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val exception: Exception? = null,
    var cityName : String = "",
    val timestamp: Long = 0L // when object was created

) {
    companion object {
        private val MAX_AGE_LOCATION = Duration.ofMinutes(20)
    }

    fun isEmpty() = lat == 0.0 && lon == 0.0 && exception == null
    fun isNotEmpty() = !isEmpty()

    /**
     * Return whether this object is older than the specified duration.
     */
    fun isStale(duration: Duration = MAX_AGE_LOCATION): Boolean {
        val age = System.currentTimeMillis() - timestamp
        return age > duration.toMillis()
    }
}
