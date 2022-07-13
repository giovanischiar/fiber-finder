package io.schiar.fiberfinder.viewmodel

import androidx.lifecycle.MutableLiveData
import io.schiar.fiberfinder.model.Location
import io.schiar.fiberfinder.model.Marker
import io.schiar.fiberfinder.model.Restaurant
import io.schiar.fiberfinder.view.viewdata.LocationViewData
import io.schiar.fiberfinder.view.viewdata.MarkerViewData
import io.schiar.fiberfinder.view.viewdata.RestaurantViewData

fun Location.toViewData(): LocationViewData {
    return LocationViewData(this.latitude, this.longitude)
}

fun List<Location>.toListViewData(): List<LocationViewData> {
    return this.map { it.toViewData() }
}

fun Restaurant.toViewData(
    locations: List<Location> = this.locations,
    markerColor: MarkerColors = MarkerColors.HUE_RED
): RestaurantViewData {
    return RestaurantViewData(
        name,
        menu,
        locations.toListViewData(),
        markerColor
    )
}

fun Marker.toViewData(): MarkerViewData {
    return MarkerViewData(name, description, color.floatValue)
}

fun Map<Location, Marker>.toMapViewData(): Map<LocationViewData, MarkerViewData> {
    return mapKeys { it.key.toViewData() }.mapValues { it.value.toViewData() }
}

fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = apply { setValue(initialValue) }