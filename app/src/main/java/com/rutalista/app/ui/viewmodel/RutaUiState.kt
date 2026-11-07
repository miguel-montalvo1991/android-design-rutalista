package com.rutalista.app.ui.viewmodel

import com.rutalista.app.data.model.RutaDetalle

sealed interface RutaUiState {

    data object Inicial : RutaUiState

    data object Cargando : RutaUiState

    data class Exito(
        val ruta: RutaDetalle
    ) : RutaUiState

    data class Error(
        val mensaje: String
    ) : RutaUiState
}