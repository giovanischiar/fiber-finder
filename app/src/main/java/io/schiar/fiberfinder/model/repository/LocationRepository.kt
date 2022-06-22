package io.schiar.fiberfinder.model.repository

import android.os.Handler
import android.os.Looper
import io.schiar.fiberfinder.BuildConfig
import io.schiar.fiberfinder.model.Location
import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread

class LocationRepository : LocationRepositoryInterface {
    private var url = ""

    override fun fetch(keyword: String, location: Location, radius: Int, callback: (List<Location>) -> Unit) {
        val latitude = location.latitude.toString()
        val longitude = location.longitude.toString()
        url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
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