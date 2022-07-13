package io.schiar.fiberfinder.view.viewdata

data class LocationViewData(
    val latitude: Double,
    val longitude: Double
) {
    override fun toString(): String {
        return "($latitude, $longitude)"
    }
}
