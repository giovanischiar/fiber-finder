package io.schiar.fiberfinder.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.schiar.fiberfinder.model.repository.RestaurantRepository
import io.schiar.fiberfinder.model.repository.RestaurantRepositoryInterface
import io.schiar.fiberfinder.view.viewdata.RestaurantViewData

class RestaurantsViewModel(
    private val restaurantRepository: RestaurantRepositoryInterface = RestaurantRepository()
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

    fun fetch() {
        restaurantRepository.fetch {
            restaurants.postValue(it.map { restaurant ->
                RestaurantViewData(restaurant.name, restaurant.menu)
            })
        }
    }
}