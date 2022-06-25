package io.schiar.fiberfinder.model.repository

import android.os.Handler
import android.os.Looper
import io.schiar.fiberfinder.BuildConfig
import io.schiar.fiberfinder.model.Location
import org.json.JSONObject
import java.lang.Math.pow
import java.net.URL
import kotlin.concurrent.thread
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt

class LocationRepository : LocationRepositoryInterface {
    private var url = ""
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
        )
    )

    override fun fetch(keyword: String, location: Location, radius: Int, callback: (List<Location>) -> Unit) {
        thread {
            Thread.sleep((900L..3000L).random())
            Handler(Looper.getMainLooper()).post {
                callback((locations[keyword] ?: listOf()).filter {
                    val x1 = location.latitude
                    val y1 = location.longitude
                    val x2 = it.latitude
                    val y2 = it.longitude

                    val dX = (x2 - x1) * Math.PI / 180;
                    val dY = (y2 - y1) * Math.PI / 180;

                    val a = 0.5 - cos(dX) / 2 + cos(x2 * Math.PI / 180) * cos(x1 * Math.PI / 180) * (1 - cos(dY)) / 2

                    val distance = 6371000 * 2 * asin(sqrt(a))

                    if (keyword == "Vitality Bowls") {
                        println("yaay")
                    }
                    distance <= radius
                })
            }
        }
//            val latitude = location.latitude.toString()
//        val longitude = location.longitude.toString()
//        url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
//                "?location=${latitude}%2C${longitude}" +
//                "&keyword=${keyword}" +
//                "&radius=${radius}" +
//                "&key=${BuildConfig.API_KEY}"
//
//        thread {
//            val json = try {
//                URL(url).readText()
//            } catch (e: Exception) {
//                println(e)
//                return@thread
//            }
//            val jsonObj = JSONObject(json)
//            val map = jsonObj.toMap()
//            @Suppress("UNCHECKED_CAST")
//            val results = map["results"] as List<LinkedHashMap<String, String>>?
//            @Suppress("CAST_NEVER_SUCCEEDS") val locations = results?.map {
//                val geometry = it["geometry"] as LinkedHashMap<String, LinkedHashMap<String, Double>>
//                val location = geometry["location"] ?: return@thread
//                val lat = location["lat"] ?: return@thread
//                val lng = location["lng"] ?: return@thread
//                Location(lat, lng)
//            }
//
//            Handler(Looper.getMainLooper()).post {
//                callback(locations ?: listOf())
//            }
//        }
    }
}