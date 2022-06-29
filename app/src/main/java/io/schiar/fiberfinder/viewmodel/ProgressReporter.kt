package io.schiar.fiberfinder.viewmodel

interface ProgressReporter {
    fun reportProgress(value: Int)
}