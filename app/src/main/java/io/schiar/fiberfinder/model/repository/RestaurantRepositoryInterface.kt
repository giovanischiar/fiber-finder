package io.schiar.fiberfinder.model.repository

import io.schiar.fiberfinder.model.Restaurant

interface RestaurantRepositoryInterface {
    fun fetch(callback: (List<Restaurant>) -> Unit)
}