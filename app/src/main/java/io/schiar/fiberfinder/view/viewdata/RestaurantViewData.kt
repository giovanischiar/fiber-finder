package io.schiar.fiberfinder.view.viewdata

data class RestaurantViewData(
    val name: String,
    val menu: String,
    val locations: List<LocationViewData>
)
