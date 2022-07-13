package io.schiar.fiberfinder.model

import kotlin.collections.Map as KotlinMap
import io.schiar.fiberfinder.viewmodel.MarkerColors

class Map {
    private val markers = mutableMapOf<String, MutableMap<Location, Marker>>()
    private val colorsPerMarker = mutableMapOf<String, MarkerColors>()
    private var markersToRemove = mutableMapOf<Location, Marker>()
    private val markerColors = MarkerColors.values()
    private var colorIndexCount = 0

    private fun invalidateMarkers(name: String) {
        val markersFromID = markers[name] ?: mutableMapOf()
        for (entry in markersFromID) {
            markersToRemove[entry.key] = entry.value
        }
    }

    @Synchronized
    private fun setColor(): MarkerColors {
        return markerColors[colorIndexCount++ % (markerColors.size-1)]
    }

    fun addMarkers(name: String, description: String, locations: List<Location>) {
        invalidateMarkers(name)
        if (locations.isEmpty()) {
            colorsPerMarker.remove(name)
            return
        }

        val color = if (colorsPerMarker.containsKey(name)) {
            colorsPerMarker[name] ?: MarkerColors.HUE_RED
        } else {
            colorsPerMarker[name] = setColor()
            colorsPerMarker[name] ?: MarkerColors.HUE_RED
        }

        locations.forEach {
            val inside =  markers[name] ?: mutableMapOf()
            if (!inside.containsKey(it)) {
                inside[it] = Marker(name, description, color)
                markers[name] = inside
            }
            markersToRemove.remove(it)
        }
    }

    fun markersToAdd(name: String): KotlinMap<Location, Marker> {
        return markers[name]?.filter { !markersToRemove.containsKey(it.key) } ?: mapOf()
    }

    fun markersToRemove(): KotlinMap<Location, Marker> {
        return markersToRemove
    }

    fun currentRestaurantColor(name: String): MarkerColors {
        return colorsPerMarker[name] ?: MarkerColors.HUE_RED
    }
}