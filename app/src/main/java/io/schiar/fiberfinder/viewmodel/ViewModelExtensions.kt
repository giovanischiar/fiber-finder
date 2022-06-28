package io.schiar.fiberfinder.viewmodel

import io.schiar.fiberfinder.model.Location
import io.schiar.fiberfinder.model.Restaurant
import io.schiar.fiberfinder.view.viewdata.LocationViewData
import io.schiar.fiberfinder.view.viewdata.RestaurantViewData

fun Location.toViewData(): LocationViewData {
    return LocationViewData(this.latitude, this.longitude)
}

fun List<Location>.toListViewData(): List<LocationViewData> {
    return this.map { it.toViewData() }
}

fun Restaurant.toViewData(locations: List<Location> = this.locations, markerColor: MarkerColors = MarkerColors.HUE_RED): RestaurantViewData {
    return RestaurantViewData(
        this.name,
        this.menu,
        locations.toListViewData(),
        markerColor
    )
}