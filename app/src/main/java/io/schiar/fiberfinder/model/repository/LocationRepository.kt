package io.schiar.fiberfinder.model.repository

import android.os.Handler
import android.os.Looper
import io.schiar.fiberfinder.BuildConfig
import io.schiar.fiberfinder.model.Location
import io.schiar.fiberfinder.model.Restaurant
import io.schiar.fiberfinder.view.viewdata.LocationViewData
import io.schiar.fiberfinder.view.viewdata.RestaurantViewData
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sqrt

class LocationRepository : LocationRepositoryInterface {

    private val locations = mapOf(
        "Vitality Bowls" to listOf(
            Location(37.37743372385918,-122.0315667873983),
            Location(37.37929707485121,-121.99277028347674),
            Location(37.32532179847934,-122.01323166837733)
        ),

        "Sajj Mediterran" to listOf(
            Location(37.377413597664585, -122.03133186682433)
        ),

        "Chipotle" to listOf(
            Location(37.36775580420934, -122.03615380793916)
        ),

        "Pokéworks" to listOf(
            Location(37.393536814801905, -122.07897383689634)
        ),

        "Eureka!" to listOf(
            Location(37.39385677632327, -122.0786722654486)
        ),

        "Bonchon" to listOf(
            Location(37.39318232927479, -122.07963669514258),
            Location(37.362270157053636, -122.02736134621435)
        ),

        "Denny’s" to listOf(
            Location(37.39615975606972, -122.02777692189157),
            Location(37.35269624768945, -121.95685609904403)
        ),

        "In n Out" to listOf(
            Location(37.36094939507492, -122.02491898940269),
            Location(37.380405201492735, -122.074029708719),
            Location(37.38805263609297, -121.9820575000368),
            Location(37.42085632488169, -122.09333948899261)
        ),

        "Subway" to listOf(
            Location(37.37111204581005, -122.04704861613482),
            Location(37.3899954370106, -122.04226898836177),
            Location(37.39725676635801, -122.06117332946619),
            Location(37.38384390234908, -122.08057212946616),
            Location(37.39984843746578, -122.11021995891397)
        )
    )

    override fun fetch(keyword: String, location: Location, radius: Int, callback: (List<Location>) -> Unit) {
        Thread.sleep((900L..3000L).random())
        callback((locations[keyword] ?: listOf()).filter {
            val x1 = location.latitude
            val y1 = location.longitude
            val x2 = it.latitude
            val y2 = it.longitude

            val dX = (x2 - x1) * Math.PI / 180
            val dY = (y2 - y1) * Math.PI / 180

            val a = 0.5 - cos(dX) / 2 + cos(x2 * Math.PI / 180) * cos(x1 * Math.PI / 180) * (1 - cos(dY)) / 2

            val distance = 6371000 * 2 * asin(sqrt(a))

            distance <= radius
        })
    }

    override suspend fun fetchCoroutine(
        keyword: String, location: Location, radius: Int
    ) = suspendCoroutine { continuation ->
        this.fetch(keyword, location, radius) { locations ->
            continuation.resume(keyword to locations)
        }
    }

    override suspend fun fetchAll(
        keywords: List<String>,
        location: Location,
        radius: Int,
        parentScope: CoroutineScope
    ): List<Pair<String, List<Location>>> {
        return keywords.map { keyword ->
            parentScope.async(Dispatchers.IO) {
                fetchCoroutine(keyword, location, radius)
            }
        }.awaitAll()
    }

    fun fetchPlacesAPI(keyword: String, location: Location, radius: Int, callback: (List<Location>) -> Unit) {
        val (latitude, longitude) = location
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=${latitude}%2C${longitude}" +
                "&keyword=${keyword}" +
                "&radius=${radius}" +
                "&key=${BuildConfig.API_KEY}"

        thread {
            val json = try {
                URL(url).readText()
            } catch (e: Exception) {
                println(e)
                return@thread
            }
            val jsonObj = JSONObject(json)
            val map = jsonObj.toMap()
            @Suppress("UNCHECKED_CAST")
            val results = map["results"] as List<LinkedHashMap<String, String>>?
            @Suppress("CAST_NEVER_SUCCEEDS") val locations = results?.map {
                val geometry = it["geometry"] as LinkedHashMap<String, LinkedHashMap<String, Double>>
                val location = geometry["location"] ?: return@thread
                val lat = location["lat"] ?: return@thread
                val lng = location["lng"] ?: return@thread
                Location(lat, lng)
            }
            Handler(Looper.getMainLooper()).post {
                callback(locations ?: listOf())
            }
        }
    }
}