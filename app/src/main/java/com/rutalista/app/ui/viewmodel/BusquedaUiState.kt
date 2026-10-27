package com.rutalista.app.ui.viewmodel

import com.rutalista.app.data.model.Direccion

/**
 * Los cuatro estados posibles de la pantalla de búsqueda.
 * La pantalla (de Diego) dibuja algo distinto según el estado.
 * "sealed" significa que no puede haber otros estados fuera de estos.
 */
sealed interface BusquedaUiState {
    /** Todavía no se ha buscado nada. */
    data object Inicial : BusquedaUiState

    /** Se está esperando la respuesta de internet. */
    data object Cargando : BusquedaUiState

    /** La búsqueda salió bien y trae una lista de resultados. */
    data class Exito(val resultados: List<Direccion>) : BusquedaUiState

    /** La búsqueda falló y trae un mensaje para mostrar al usuario. */
    data class Error(val mensaje: String) : BusquedaUiState
}