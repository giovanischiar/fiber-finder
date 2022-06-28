package io.schiar.fiberfinder.view.viewdata

import io.schiar.fiberfinder.viewmodel.MarkerColors

fun RestaurantViewData.addLocationsViewData(
    locations: List<LocationViewData>,
    markersColor: MarkerColors? = MarkerColors.HUE_RED,
    isShown: Boolean? = true
): RestaurantViewData {
    return RestaurantViewData(
        this.name,
        this.menu,
        locations,
        markersColor ?: MarkerColors.HUE_RED,
        isShown ?:true
    )
}
