package com.rutalista.app.data.model
import com.google.gson.annotations.SerializedName

data class RutaResponse(
    val routes: List<RutaDetalle> = emptyList()
)

data class RutaDetalle(
    val summary: ResumenRuta? = null,
    val segments: List<SegmentoRuta> = emptyList(),
    val geometry: String? = null
)

data class ResumenRuta(
    val distance: Double = 0.0,
    val duration: Double = 0.0
)

data class SegmentoRuta(
    val distance: Double = 0.0,
    val duration: Double = 0.0,
    val steps: List<PasoRuta> = emptyList()
)

data class PasoRuta(
    val distance: Double = 0.0,
    val duration: Double = 0.0,
    val instruction: String = "",
    @SerializedName("name")
    val nombreVia: String = ""
)