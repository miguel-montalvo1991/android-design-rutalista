
package com.rutalista.app.data.repository

import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.rutalista.app.BuildConfig
import com.rutalista.app.data.model.Direccion
import com.rutalista.app.data.model.OptimizacionRequest
import com.rutalista.app.data.model.OptimizacionResponse
import com.rutalista.app.data.model.RutaDetalle
import com.rutalista.app.data.model.RutaRequest
import com.rutalista.app.data.model.TrabajoRuta
import com.rutalista.app.data.model.VehiculoRuta
import com.rutalista.app.data.remote.OrsApi
import com.rutalista.app.data.remote.RetrofitClient
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.HttpException

class RutaRepository(
    private val api: OrsApi = RetrofitClient.api
) {

    // Buscar direcciones
    suspend fun buscarDireccion(texto: String): Result<List<Direccion>> {
        return try {
            val respuesta = api.geocodeSearch(
                apiKey = BuildConfig.ORS_API_KEY,
                texto = texto
            )

            val direcciones = respuesta.features.map { f ->
                Direccion(
                    texto = f.properties.label,
                    lat = f.geometry.coordinates[1],
                    lon = f.geometry.coordinates[0]
                )
            }

            Result.success(direcciones)

        } catch (e: HttpException) {
            val mensaje = when (e.code()) {
                401, 403 -> "La clave de API no es válida."
                429 -> "Se superó el límite de consultas."
                else -> "Error del servidor (${e.code()})."
            }
            Result.failure(Exception(mensaje))

        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet."))

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            Result.failure(Exception("Error al buscar la dirección."))
        }
    }

    // Geocodificación inversa (convertir coordenadas lat/lon en Direccion)
    suspend fun obtenerDireccionDesdeCoordenadas(lat: Double, lon: Double): Result<Direccion> {
        val latFormateada = String.format(java.util.Locale.US, "%.5f", lat)
        val lonFormateada = String.format(java.util.Locale.US, "%.5f", lon)
        val nombreRespaldo = "Ubicación en mapa ($latFormateada, $lonFormateada)"

        return try {
            val respuesta = api.geocodeReverse(
                apiKey = BuildConfig.ORS_API_KEY,
                lon = lon,
                lat = lat,
                tamano = 1
            )

            val label = respuesta.features.firstOrNull()?.properties?.label
            val textoFinal = if (!label.isNullOrBlank()) label else nombreRespaldo

            Result.success(Direccion(texto = textoFinal, lat = lat, lon = lon))

        } catch (e: Exception) {
            // Si falla la geocodificación por red o API key, conservamos las coordenadas
            Result.success(Direccion(texto = nombreRespaldo, lat = lat, lon = lon))
        }
    }

    // Obtener ubicación GPS actual del dispositivo
    suspend fun obtenerUbicacionActualGps(context: Context): Result<Direccion> {
        return try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val location = suspendCancellableCoroutine<Location?> { cont ->
                fusedClient.lastLocation
                    .addOnSuccessListener { loc: Location? ->
                        if (loc != null) {
                            if (cont.isActive) cont.resume(loc)
                        } else {
                            fusedClient.getCurrentLocation(
                                Priority.PRIORITY_HIGH_ACCURACY,
                                null
                            ).addOnSuccessListener { curLoc: Location? ->
                                if (cont.isActive) cont.resume(curLoc)
                            }.addOnFailureListener {
                                if (cont.isActive) cont.resume(null)
                            }
                        }
                    }
                    .addOnFailureListener {
                        if (cont.isActive) cont.resume(null)
                    }
            }

            if (location != null) {
                val latFormateada = String.format(java.util.Locale.US, "%.5f", location.latitude)
                val lonFormateada = String.format(java.util.Locale.US, "%.5f", location.longitude)
                val nombreGps = "Ubicación GPS ($latFormateada, $lonFormateada)"

                val resultadoGeocodificacion = obtenerDireccionDesdeCoordenadas(location.latitude, location.longitude)
                val direccionFinal = resultadoGeocodificacion.getOrNull()

                if (direccionFinal != null && !direccionFinal.texto.startsWith("Ubicación en mapa")) {
                    Result.success(direccionFinal)
                } else {
                    Result.success(Direccion(texto = nombreGps, lat = location.latitude, lon = location.longitude))
                }
            } else {
                Result.failure(Exception("No se pudo obtener la señal GPS del dispositivo."))
            }

        } catch (e: SecurityException) {
            Result.failure(Exception("Permiso de ubicación denegado."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Error al obtener la ubicación GPS."))
        }
    }

    // Calcular ruta respetando el orden de los destinos
    suspend fun calcularRuta(
        origen: Direccion,
        destinos: List<Direccion>
    ): Result<RutaDetalle> {

        if (destinos.isEmpty()) {
            return Result.failure(
                Exception("Agrega al menos un destino.")
            )
        }

        return try {
            val coordenadas = listOf(origen) + destinos

            val solicitud = RutaRequest(
                coordinates = coordenadas.map {
                    listOf(it.lon, it.lat)
                }
            )

            val respuesta = api.calcularRuta(
                apiKey = BuildConfig.ORS_API_KEY,
                solicitud = solicitud
            )

            val ruta = respuesta.routes.firstOrNull()
                ?: throw Exception("No se encontró una ruta.")

            Result.success(ruta)

        } catch (e: HttpException) {
            Result.failure(
                Exception("Error al calcular la ruta (${e.code()}).")
            )

        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet."))

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            Result.failure(
                Exception(e.message ?: "Error al calcular la ruta.")
            )
        }
    }

    // Optimizar automáticamente el orden de los destinos
    suspend fun optimizarRuta(
        origen: Direccion,
        destinos: List<Direccion>
    ): Result<OptimizacionResponse> {

        if (destinos.isEmpty()) {
            return Result.failure(
                Exception("Agrega al menos un destino para optimizar.")
            )
        }

        return try {
            val trabajos = destinos.mapIndexed { index, direccion ->
                TrabajoRuta(
                    id = index + 1,
                    location = listOf(direccion.lon, direccion.lat)
                )
            }

            val vehiculo = VehiculoRuta(
                id = 1,
                profile = "driving-car",
                start = listOf(origen.lon, origen.lat)
            )

            val solicitud = OptimizacionRequest(
                jobs = trabajos,
                vehicles = listOf(vehiculo)
            )

            val respuesta = api.optimizarRuta(
                apiKey = BuildConfig.ORS_API_KEY,
                solicitud = solicitud
            )

            if (respuesta.routes.isEmpty()) {
                throw Exception("No se encontró un recorrido optimizado.")
            }

            Result.success(respuesta)

        } catch (e: HttpException) {
            Result.failure(
                Exception("Error al optimizar la ruta (${e.code()}).")
            )

        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet."))

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            Result.failure(
                Exception(e.message ?: "Error al optimizar la ruta.")
            )
        }
    }
}
