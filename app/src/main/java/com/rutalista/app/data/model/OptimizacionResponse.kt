
package com.rutalista.app.data.model

data class OptimizacionResponse(
    val routes: List<RutaOptima> = emptyList(),
    val unassigned: List<TrabajoNoAsignado> = emptyList()
)

data class RutaOptima(
    val vehicle: Int = 0,
    val steps: List<PasoOptimo> = emptyList(),
    val distance: Double = 0.0,
    val duration: Double = 0.0
)

data class PasoOptimo(
    val type: String = "",
    val id: Int? = null,
    val location: List<Double> = emptyList(),
    val arrival: Double? = null,
    val duration: Double = 0.0,
    val distance: Double = 0.0
)

data class TrabajoNoAsignado(
    val id: Int = 0,
    val location: List<Double> = emptyList()
)
