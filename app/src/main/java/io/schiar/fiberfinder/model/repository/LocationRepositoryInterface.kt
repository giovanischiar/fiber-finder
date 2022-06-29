package io.schiar.fiberfinder.model.repository

import io.schiar.fiberfinder.model.Location
import io.schiar.fiberfinder.view.viewdata.RestaurantViewData
import io.schiar.fiberfinder.viewmodel.ProgressReporter
import kotlinx.coroutines.CoroutineScope

interface LocationRepositoryInterface {
    suspend fun fetchCoroutine(keyword: String, location: Location, radius: Int, progressReporter: ProgressReporter): Pair<String, List<Location>>
    fun fetch(keyword: String, location: Location, radius: Int, callback: (List<Location>) -> Unit)
    suspend fun fetchAll(
        keywords: List<String>,
        location: Location,
        radius: Int,
        progressReporter: ProgressReporter,
        parentScope: CoroutineScope
    ): List<Pair<String, List<Location>>>
}