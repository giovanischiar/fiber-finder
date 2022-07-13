package io.schiar.fiberfinder.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.collections.Map as KotlinMap
import io.schiar.fiberfinder.model.Location
import io.schiar.fiberfinder.model.Map
import io.schiar.fiberfinder.model.Restaurant
import io.schiar.fiberfinder.model.repository.LocationRepository
import io.schiar.fiberfinder.model.repository.LocationRepositoryInterface
import io.schiar.fiberfinder.model.repository.RestaurantRepository
import io.schiar.fiberfinder.model.repository.RestaurantRepositoryInterface
import io.schiar.fiberfinder.view.viewdata.LocationViewData
import io.schiar.fiberfinder.view.viewdata.MarkerViewData
import io.schiar.fiberfinder.view.viewdata.RestaurantViewData
import io.schiar.fiberfinder.view.viewdata.addLocationsViewData
import kotlinx.coroutines.*

class RestaurantsViewModel(
    private val restaurantRepository: RestaurantRepositoryInterface = RestaurantRepository(),
    private val locationRepository: LocationRepositoryInterface = LocationRepository()
) : ViewModel(), ProgressReporter {
    private val map = Map()
    private val semaphore = java.util.concurrent.Semaphore(1)
    private var restaurants = mutableMapOf<String, Restaurant>()
    private val currentMappedRestaurants = mutableMapOf<String, RestaurantViewData>()

    val progress: MutableLiveData<Int> = MutableLiveData(0)
    val restaurant = MutableLiveData<RestaurantViewData>()
    val restaurantAmount = MutableLiveData(0)
    val markersToAdd = MutableLiveData<KotlinMap<LocationViewData, MarkerViewData>>()
    val markersToRem = MutableLiveData<KotlinMap<LocationViewData, MarkerViewData>>()

    val currentRestaurants = MutableLiveData<List<RestaurantViewData>>(listOf())


    fun changeIsShownRestaurantAt(index: Int) {
//        val updatedRestaurants = (restaurants.value ?: return)
//        val restaurant = updatedRestaurants.filter { it.locations.isNotEmpty() }[index]
//        val i = updatedRestaurants.indexOf(restaurant)
//        updatedRestaurants[i].isShown = !updatedRestaurants[i].isShown
//        restaurants.postValue(updatedRestaurants)
    }

    fun restaurantAt(index: Int) {
        //restaurant.postValue(restaurants.values.toList()[index])
    }

    fun fetchAllRestaurantLocations(latitude: Double, longitude: Double, radius: Int) {
        progress.postValue(0)
        viewModelScope.launch {
            locationRepository.fetchAll(
                restaurants.keys.toList(),
                Location(latitude, longitude),
                radius,
                this@RestaurantsViewModel,
                this
            )
            progress.postValue(restaurants.size)
            markersToRem.postValue(map.markersToRemove().toMapViewData())
            currentRestaurants.postValue(currentMappedRestaurants.values.toList())
        }
    }

    fun fetchRestaurantLocations(latitude: Double, longitude: Double, radius: Int) {
        restaurant.value ?: return
        val restaurantValue = restaurant.value as RestaurantViewData
        viewModelScope.launch {
            val locations = locationRepository.fetchCoroutine(
                restaurantValue.name,
                Location(latitude, longitude),
                radius,
                this@RestaurantsViewModel
            ).second
            restaurant.postValue(
                restaurantValue.addLocationsViewData(
                    locations.toListViewData(),
                    restaurant.value?.markerColor,
                    restaurant.value?.isShown
                )
            )
        }
    }

    fun fetch() {
        restaurantRepository.fetch {
            restaurants = it.associateBy { restaurant -> restaurant.name }.toMutableMap()
            restaurantAmount.postValue(restaurants.size)
        }
    }

    override fun reportProgress(result: Pair<String, List<Location>>) {
        semaphore.acquire()
        val i = (progress.value ?: return) + 1
        progress.postValue(i)
        semaphore.release()
        val (restaurantName, locations) = result
        map.addMarkers(restaurantName, (restaurants[restaurantName] ?: return).menu, locations)

        if (locations.isNotEmpty()) {
            markersToAdd.postValue(map.markersToAdd(restaurantName).toMapViewData())
            val color = map.currentRestaurantColor(restaurantName)
            currentMappedRestaurants[restaurantName] = RestaurantViewData(
                restaurantName,
                (restaurants[restaurantName] ?: return).menu,
                locations.toListViewData(),
                color,
                true
            )
            currentRestaurants.postValue(currentMappedRestaurants.values.toList())
        } else {
            currentMappedRestaurants.remove(restaurantName)
        }
    }
}