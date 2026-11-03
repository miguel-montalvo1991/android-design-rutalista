package com.rutalista.app.data.model

data class RutaRequest(
    val coordinates: List<List<Double>>,
    val instructions: Boolean = true
)