package com.rutalista.app.ui.viewmodel
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rutalista.app.data.model.Direccion
import com.rutalista.app.data.model.OptimizacionResponse
import com.rutalista.app.data.repository.RutaRepository
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.rutalista.app.core.PolylineDecoder


class RutaViewModel(
    private val repository: RutaRepository = RutaRepository()
) : ViewModel() {

    private val _origen = MutableStateFlow<Direccion?>(null)
    val origen: StateFlow<Direccion?> = _origen.asStateFlow()

    private val _busqueda = MutableStateFlow<BusquedaUiState>(BusquedaUiState.Inicial)
    val busqueda: StateFlow<BusquedaUiState> = _busqueda.asStateFlow()

    private val _pedidos = MutableStateFlow<List<Direccion>>(emptyList())
    val pedidos: StateFlow<List<Direccion>> = _pedidos.asStateFlow()

    private val _ruta = MutableStateFlow<RutaUiState>(RutaUiState.Inicial)
    val ruta: StateFlow<RutaUiState> = _ruta.asStateFlow()

    private val _optimizacion = MutableStateFlow<OptimizacionUiState>(OptimizacionUiState.Inicial)
    val optimizacion: StateFlow<OptimizacionUiState> = _optimizacion.asStateFlow()

    private val _ubicacionGps = MutableStateFlow<UbicacionGpsUiState>(UbicacionGpsUiState.Inicial)
    val ubicacionGps: StateFlow<UbicacionGpsUiState> = _ubicacionGps.asStateFlow()

    private val _geocodificacionMapa = MutableStateFlow<GeocodeMapaUiState>(GeocodeMapaUiState.Inicial)
    val geocodificacionMapa: StateFlow<GeocodeMapaUiState> = _geocodificacionMapa.asStateFlow()

    // COMMIT 4a: borra la ruta y la optimización anteriores.
    // Se llama cada vez que cambian el origen o los destinos, porque
    // el resultado viejo ya no corresponde a los datos actuales.
    private fun reiniciarResultados() {
        _ruta.value = RutaUiState.Inicial
        _optimizacion.value = OptimizacionUiState.Inicial
    }

    // ESTABLECER Y GESTIONAR ORIGEN
    fun establecerOrigen(direccion: Direccion?) {
        _origen.value = direccion
        reiniciarResultados()
    }

    // BUSCAR DIRECCIONES MANUALMENTE
    fun buscar(texto: String) {
        if (texto.isBlank()) {
            _busqueda.value = BusquedaUiState.Inicial
            return
        }

        viewModelScope.launch {
            _busqueda.value = BusquedaUiState.Cargando

            try {
                repository.buscarDireccion(texto).fold(
                    onSuccess = {
                        _busqueda.value = BusquedaUiState.Exito(it)
                    },
                    onFailure = {
                        _busqueda.value = BusquedaUiState.Error(
                            it.message ?: "Error al buscar direcciones."
                        )
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _busqueda.value = BusquedaUiState.Error(
                    e.message ?: "Error inesperado."
                )
            }
        }
    }

    // Limpiar búsqueda
    fun limpiarBusqueda() {
        _busqueda.value = BusquedaUiState.Inicial
    }

    // AGREGAR DESTINO (devuelve false si ya estaba en la lista)
    fun agregar(direccion: Direccion): Boolean {
        val yaExiste = _pedidos.value.any {
            kotlin.math.abs(it.lat - direccion.lat) < 0.00001 &&
                    kotlin.math.abs(it.lon - direccion.lon) < 0.00001
        }
        if (yaExiste) return false
        _pedidos.value = _pedidos.value + direccion
        reiniciarResultados() // COMMIT 4a: la lista cambió
        return true
    }

    // ELIMINAR DESTINO
    fun quitar(direccion: Direccion) {
        _pedidos.value = _pedidos.value - direccion
        reiniciarResultados() // COMMIT 4a: la lista cambió
    }

    // REEMPLAZAR DESTINO
    fun reemplazarDestino(index: Int, nuevaDireccion: Direccion) {
        val listaActual = _pedidos.value.toMutableList()
        if (index in listaActual.indices) {
            listaActual[index] = nuevaDireccion
            _pedidos.value = listaActual
            reiniciarResultados() // COMMIT 4a: la lista cambió
        }
    }

    // OBTENER UBICACIÓN ACTUAL POR GPS
    fun obtenerUbicacionGps(
        context: Context,
        esOrigen: Boolean = false,
        indexDestino: Int? = null
    ) {
        viewModelScope.launch {
            _ubicacionGps.value = UbicacionGpsUiState.Cargando
            repository.obtenerUbicacionActualGps(context).fold(
                onSuccess = { direccion ->
                    _ubicacionGps.value = UbicacionGpsUiState.Exito(direccion)
                    if (esOrigen) {
                        establecerOrigen(direccion)
                    } else if (indexDestino != null) {
                        reemplazarDestino(indexDestino, direccion)
                    } else {
                        agregar(direccion)
                    }
                },
                onFailure = { error ->
                    _ubicacionGps.value = UbicacionGpsUiState.Error(
                        error.message ?: "No se pudo obtener la ubicación GPS."
                    )
                }
            )
        }
    }

    // GEOCODIFICAR PUNTO EN MAPA
    fun geocodificarPuntoMapa(lat: Double, lon: Double, onResultado: (Direccion) -> Unit) {
        viewModelScope.launch {
            _geocodificacionMapa.value = GeocodeMapaUiState.Cargando
            repository.obtenerDireccionDesdeCoordenadas(lat, lon).fold(
                onSuccess = { direccion ->
                    _geocodificacionMapa.value = GeocodeMapaUiState.Exito(direccion)
                    onResultado(direccion)
                },
                onFailure = {
                    val fallback = Direccion(
                        texto = String.format(Locale.US, "Mapa (%.5f, %.5f)", lat, lon),
                        lat = lat,
                        lon = lon
                    )
                    _geocodificacionMapa.value = GeocodeMapaUiState.Exito(fallback)
                    onResultado(fallback)
                }
            )
        }
    }

    // CALCULAR RUTA
    fun calcularRuta(origen: Direccion? = _origen.value) {
        if (origen == null) {
            _ruta.value = RutaUiState.Error("Selecciona un origen primero.")
            return
        }
        val destinos = _pedidos.value

        viewModelScope.launch {
            _ruta.value = RutaUiState.Cargando

            try {
                repository.calcularRuta(origen, destinos).fold(
                    onSuccess = { rutaDetalle ->
                        // El texto "geometry" de ORS se decodifica aquí, en el ViewModel,
                        // para que la vista solo reciba puntos listos y no tenga lógica.
                        // Si geometry viene nulo se guarda una lista vacía (el mapa avisará).
                        _ruta.value = RutaUiState.Exito(
                            ruta = rutaDetalle,
                            origen = origen,
                            destinos = destinos,
                            puntosRuta = PolylineDecoder.decodificar(rutaDetalle.geometry.orEmpty())
                        )
                    },
                    onFailure = {
                        _ruta.value = RutaUiState.Error(
                            it.message ?: "Error al calcular la ruta."
                        )
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _ruta.value = RutaUiState.Error(
                    e.message ?: "Error inesperado."
                )
            }
        }
    }

    // OPTIMIZAR DESTINOS
    fun optimizarRuta(origen: Direccion? = _origen.value) {
        if (origen == null) {
            _optimizacion.value = OptimizacionUiState.Error("Selecciona un origen primero.")
            return
        }
        val destinos = _pedidos.value

        viewModelScope.launch {
            _optimizacion.value = OptimizacionUiState.Cargando

            try {
                repository.optimizarRuta(origen, destinos).fold(
                    onSuccess = {
                        _optimizacion.value = OptimizacionUiState.Exito(it)
                    },
                    onFailure = {
                        _optimizacion.value = OptimizacionUiState.Error(
                            it.message ?: "Error al optimizar la ruta."
                        )
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _optimizacion.value = OptimizacionUiState.Error(
                    e.message ?: "Error inesperado."
                )
            }
        }
    }
}

// ESTADOS DE OPTIMIZACIÓN Y UBICACIÓN
sealed interface OptimizacionUiState {
    data object Inicial : OptimizacionUiState
    data object Cargando : OptimizacionUiState
    data class Exito(val resultado: OptimizacionResponse) : OptimizacionUiState
    data class Error(val mensaje: String) : OptimizacionUiState
}

sealed interface UbicacionGpsUiState {
    data object Inicial : UbicacionGpsUiState
    data object Cargando : UbicacionGpsUiState
    data class Exito(val direccion: Direccion) : UbicacionGpsUiState
    data class Error(val mensaje: String) : UbicacionGpsUiState
}

sealed interface GeocodeMapaUiState {
    data object Inicial : GeocodeMapaUiState
    data object Cargando : GeocodeMapaUiState
    data class Exito(val direccion: Direccion) : GeocodeMapaUiState
    data class Error(val mensaje: String) : GeocodeMapaUiState
}
