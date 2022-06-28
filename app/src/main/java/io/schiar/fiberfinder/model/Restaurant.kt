package io.schiar.fiberfinder.model

data class Restaurant(
    val name: String,
    val menu: String,
    val locations: List<Location>
)