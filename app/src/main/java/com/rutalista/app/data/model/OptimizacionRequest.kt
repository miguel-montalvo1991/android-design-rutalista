
package com.rutalista.app.data.model

data class OptimizacionRequest(
    val jobs: List<TrabajoRuta>,
    val vehicles: List<VehiculoRuta>
)

data class TrabajoRuta(
    val id: Int,
    val location: List<Double>
)

data class VehiculoRuta(
    val id: Int,
    val profile: String = "driving-car",
    val start: List<Double>,
    val end: List<Double>? = null
)
