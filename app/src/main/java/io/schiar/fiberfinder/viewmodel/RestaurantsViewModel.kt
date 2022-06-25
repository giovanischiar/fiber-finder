package io.schiar.fiberfinder.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.schiar.fiberfinder.model.Location
import io.schiar.fiberfinder.model.Restaurant
import io.schiar.fiberfinder.model.repository.LocationRepository
import io.schiar.fiberfinder.model.repository.LocationRepositoryInterface
import io.schiar.fiberfinder.model.repository.RestaurantRepository
import io.schiar.fiberfinder.model.repository.RestaurantRepositoryInterface
import io.schiar.fiberfinder.view.viewdata.LocationViewData
import io.schiar.fiberfinder.view.viewdata.RestaurantViewData

class RestaurantsViewModel(
    private val restaurantRepository: RestaurantRepositoryInterface = RestaurantRepository(),
    private val locationRepository: LocationRepositoryInterface = LocationRepository()
) : ViewModel() {
    val restaurants: MutableLiveData<List<RestaurantViewData>> by lazy {
        MutableLiveData<List<RestaurantViewData>>()
    }

    val restaurant: MutableLiveData<RestaurantViewData> by lazy {
        MutableLiveData<RestaurantViewData>()
    }

    fun restaurantAt(index: Int) {
        restaurant.postValue(restaurants.value?.get(index) ?: return)
    }

    fun fetchAllRestaurantLocations(latitude: Double, longitude: Double, radius: Int) {
        restaurants.value ?: return
        val restaurantsValue = restaurants.value
        val updatedRestaurants = mutableListOf<RestaurantViewData>()
        restaurantsValue?.toMutableList()?.toCollection(updatedRestaurants)
        //val reducedList = listOf(restaurantsValue?.get(37) ?: return)
        restaurantsValue?.forEach {
            locationRepository.fetch(it.name, Location(latitude, longitude), radius) { locations ->
                val index = updatedRestaurants.indexOf(it)
                val restaurantViewData = RestaurantViewData(
                    it.name,
                    it.menu,
                    locations.map { location -> LocationViewData(location.latitude, location.longitude) }
                )
                updatedRestaurants[index] = restaurantViewData
                restaurants.postValue(updatedRestaurants.toList())
            }
        }
    }

    fun fetchRestaurantLocations(latitude: Double, longitude: Double, radius: Int) {
        restaurant.value ?: return
        val restaurantValue = restaurant.value as RestaurantViewData
        locationRepository.fetch(restaurantValue.name, Location(latitude, longitude), radius) {
            restaurant.postValue(
                RestaurantViewData(
                    restaurantValue.name,
                    restaurantValue.menu,
                    it.map { location -> LocationViewData(location.latitude, location.longitude) })
            )
        }
    }

    fun fetch() {
        restaurantRepository.fetch {
            restaurants.postValue(it.map { restaurant ->
                RestaurantViewData(restaurant.name, restaurant.menu, listOf())
            })
        }
    }
}