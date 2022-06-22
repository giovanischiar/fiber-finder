package io.schiar.fiberfinder.model.repository

import io.schiar.fiberfinder.model.Location

interface LocationRepositoryInterface {
    fun fetch(keyword: String, location: Location, radius: Int, callback: (List<Location>) -> Unit)
}