package io.schiar.fiberfinder.model

import io.schiar.fiberfinder.viewmodel.MarkerColors

data class Marker(
    val name: String,
    val description: String,
    val color: MarkerColors,
)