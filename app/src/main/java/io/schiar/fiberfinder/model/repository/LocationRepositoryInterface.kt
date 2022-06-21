package io.schiar.fiberfinder.model.repository

import io.schiar.fiberfinder.model.Location

interface LocationRepositoryInterface {
    fun fetch(query: String, callback: (List<Location>) -> Unit)
}