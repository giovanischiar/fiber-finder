package io.schiar.fiberfinder.viewmodel

import io.schiar.fiberfinder.model.Location

interface ProgressReporter {
    fun reportProgress(result: Pair<String, List<Location>>)
}