package com.rutalista.app.ui.viewmodel

import com.rutalista.app.data.model.RutaDetalle
import com.rutalista.app.core.PuntoRuta
import com.rutalista.app.data.model.Direccion

sealed interface RutaUiState {

    data object Inicial : RutaUiState

    data object Cargando : RutaUiState

    data class Exito(
        val ruta: RutaDetalle,
        val origen: Direccion,
        val destinos: List<Direccion>,
        val puntosRuta: List<PuntoRuta>   // trazado ya decodificado, listo para dibujar
    ) : RutaUiState

    data class Error(
        val mensaje: String
    ) : RutaUiState


}