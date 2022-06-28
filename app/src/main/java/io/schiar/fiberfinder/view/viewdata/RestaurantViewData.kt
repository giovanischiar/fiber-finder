package io.schiar.fiberfinder.view.viewdata

import io.schiar.fiberfinder.viewmodel.MarkerColors

data class RestaurantViewData(
    val name: String,
    val menu: String,
    val locations: List<LocationViewData>,
    val markerColor: MarkerColors,
    var isShown: Boolean = true
)