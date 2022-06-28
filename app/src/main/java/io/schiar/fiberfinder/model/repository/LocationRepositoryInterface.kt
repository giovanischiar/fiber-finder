package io.schiar.fiberfinder.model.repository

import io.schiar.fiberfinder.model.Location
import io.schiar.fiberfinder.view.viewdata.RestaurantViewData
import kotlinx.coroutines.CoroutineScope

interface LocationRepositoryInterface {
    suspend fun fetchCoroutine(keyword: String, location: Location, radius: Int): Pair<String, List<Location>>
    fun fetch(keyword: String, location: Location, radius: Int, callback: (List<Location>) -> Unit)
    suspend fun fetchAll(
        keywords: List<String>,
        location: Location,
        radius: Int,
        parentScope: CoroutineScope
    ): List<Pair<String, List<Location>>>
}