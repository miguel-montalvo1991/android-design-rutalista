package com.rutalista.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rutalista.app.data.model.Direccion
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel: guarda el estado que las pantallas muestran y recibe sus acciones.
 * Sobrevive a rotaciones de pantalla.
 *
 * VERSIÓN DE PRUEBA: inventa resultados. En L4 la cambiamos por la API real
 * SIN modificar los nombres públicos, para que Diego no tenga que tocar nada.
 */
class RutaViewModel : ViewModel() {

    // Estado de la búsqueda. "_busqueda" es privado y se puede modificar aquí dentro.
    private val _busqueda = MutableStateFlow<BusquedaUiState>(BusquedaUiState.Inicial)

    // "busqueda" es público y de solo lectura: las pantallas solo pueden mirarlo.
    val busqueda: StateFlow<BusquedaUiState> = _busqueda.asStateFlow()

    // Lista de direcciones que el domiciliario ya agregó a su jornada.
    private val _pedidos = MutableStateFlow<List<Direccion>>(emptyList())
    val pedidos: StateFlow<List<Direccion>> = _pedidos.asStateFlow()

    /** Busca una dirección. Por ahora devuelve resultados falsos. */
    fun buscar(texto: String) {
        if (texto.isBlank()) return // no buscar si el campo está vacío

        // launch inicia una corrutina: trabajo en segundo plano sin congelar la pantalla.
        viewModelScope.launch {
            _busqueda.value = BusquedaUiState.Cargando
            delay(600) // simula lo que tarda internet
            _busqueda.value = BusquedaUiState.Exito(
                listOf(
                    Direccion("$texto, Medellín (falso 1)", 6.2476, -75.5636),
                    Direccion("$texto, Envigado (falso 2)", 6.1759, -75.5917)
                )
            )
        }
    }

    /** Agrega una dirección a la lista de pedidos. */
    fun agregar(direccion: Direccion) {
        _pedidos.value = _pedidos.value + direccion
    }

    /** Quita una dirección de la lista de pedidos. */
    fun quitar(direccion: Direccion) {
        _pedidos.value = _pedidos.value - direccion
    }
}