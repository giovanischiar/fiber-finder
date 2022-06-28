package io.schiar.fiberfinder.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.schiar.fiberfinder.model.Location
import io.schiar.fiberfinder.model.repository.LocationRepository
import io.schiar.fiberfinder.model.repository.LocationRepositoryInterface
import io.schiar.fiberfinder.model.repository.RestaurantRepository
import io.schiar.fiberfinder.model.repository.RestaurantRepositoryInterface
import io.schiar.fiberfinder.view.viewdata.RestaurantViewData
import io.schiar.fiberfinder.view.viewdata.addLocationsViewData
import kotlinx.coroutines.*

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
        //restaurantsValue?.toMutableList()?.toCollection(updatedRestaurants)
        //val reducedList = listOf(restaurantsValue?.get(37) ?: return)
        viewModelScope.launch {
            restaurants.postValue(
                locationRepository.fetchAll(
                    restaurantsValue?.map { it.name } ?: return@launch,
                    Location(latitude, longitude),
                    radius,
                    this
                ).map {
                    val res = restaurantsValue.filter {
                        restaurant -> restaurant.name === it.first
                    }[0]
                    val locations = it.second
                    res.addLocationsViewData(locations.toListViewData())
                }
            )
        }
    }

    fun fetchRestaurantLocations(latitude: Double, longitude: Double, radius: Int) {
        restaurant.value ?: return
        val restaurantValue = restaurant.value as RestaurantViewData
        viewModelScope.launch {
            val locations = locationRepository.fetchCoroutine(
                restaurantValue.name,
                Location(latitude, longitude),
                radius
            ).second
            restaurant.postValue(
                restaurantValue.addLocationsViewData(
                    locations.toListViewData()
                )
            )
        }
    }

    fun fetch() {
        restaurantRepository.fetch {
            restaurants.postValue(it.map { restaurant ->
                restaurant.toViewData()
            })
        }
    }
}