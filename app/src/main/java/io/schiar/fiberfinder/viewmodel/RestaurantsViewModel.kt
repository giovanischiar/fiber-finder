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