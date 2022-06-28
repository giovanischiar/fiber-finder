package io.schiar.fiberfinder.model.repository

import android.os.Handler
import android.os.Looper
import io.schiar.fiberfinder.BuildConfig
import io.schiar.fiberfinder.model.Restaurant
import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread

class RestaurantRepository : RestaurantRepositoryInterface {
    private val url = "https://sheets.googleapis.com/v4/spreadsheets/${BuildConfig.SPREADSHEET_ID}/values/'restaurants'!A:D1?&key=${BuildConfig.API_KEY}"

    override fun fetch(callback: (List<Restaurant>) -> Unit) {
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
            val values = map["values"] as List<List<String>>?
            val foods = values?.subList(1, values.size)?.map {
                Restaurant(it[0], it[1], listOf())
            }

            Handler(Looper.getMainLooper()).post {
                callback(foods ?: listOf())
            }
        }
    }
}