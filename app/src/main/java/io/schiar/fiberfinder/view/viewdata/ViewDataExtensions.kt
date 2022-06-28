package io.schiar.fiberfinder.view.viewdata

fun RestaurantViewData.addLocationsViewData(locations: List<LocationViewData>): RestaurantViewData {
    return RestaurantViewData(
        this.name,
        this.menu,
        locations
    )
}
