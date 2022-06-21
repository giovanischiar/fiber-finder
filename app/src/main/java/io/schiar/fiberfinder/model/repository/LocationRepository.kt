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

    override fun fetch(query: String, callback: (List<Location>) -> Unit) {
        val latitude = "37.3845545"
        val longitude = "-122.0269173"
        val radius = 10000
        url = "https://maps.googleapis.com/maps/api/place/textsearch/json?location=${latitude}%2C${longitude}&query=${query}&radius=${radius}&key=${BuildConfig.API_KEY}"

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
            @Suppress("CAST_NEVER_SUCCEEDS") val locations = results?.subList(1, results.size)?.map {
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