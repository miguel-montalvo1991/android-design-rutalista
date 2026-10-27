package com.rutalista.app.data.repository

import com.rutalista.app.BuildConfig
import com.rutalista.app.data.model.Direccion
import com.rutalista.app.data.remote.OrsApi
import com.rutalista.app.data.remote.RetrofitClient
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException

/**
 * Repository: único punto por donde los datos entran a la app.
 * Al ViewModel no le importa si vienen de internet o de otro lado.
 */
class RutaRepository(private val api: OrsApi = RetrofitClient.api) {

    /**
     * Busca una dirección y devuelve una lista de [Direccion].
     * Devuelve [Result]: o trae la lista (success) o un error con mensaje (failure).
     */
    suspend fun buscarDireccion(texto: String): Result<List<Direccion>> {
        return try {
            // 1. Llamada a la API usando la clave guardada en BuildConfig.
            val respuesta = api.geocodeSearch(
                apiKey = BuildConfig.ORS_API_KEY,
                texto = texto
            )

            // 2. Convertimos cada resultado de la API en nuestro modelo Direccion.
            val direcciones = respuesta.features.map { f ->
                Direccion(
                    texto = f.properties.label,
                    lat = f.geometry.coordinates[1], // el índice 1 es la latitud
                    lon = f.geometry.coordinates[0]  // el índice 0 es la longitud
                )
            }
            Result.success(direcciones)

        } catch (e: HttpException) {
            // El servidor respondió con un error: traducimos el código a un mensaje claro.
            val mensaje = when (e.code()) {
                401, 403 -> "La clave de API no es válida"
                429 -> "Se superó el límite de consultas, intenta más tarde"
                else -> "Error del servidor (${e.code()})"
            }
            Result.failure(Exception(mensaje))

        } catch (e: IOException) {
            // No hubo conexión con el servidor.
            Result.failure(Exception("Sin conexión a internet"))

        } catch (e: CancellationException) {
            // Si la corrutina se cancela (por ejemplo, el usuario sale de la pantalla),
            // hay que dejar pasar la cancelación y no tratarla como un error.
            throw e

        } catch (e: Exception) {
            // Cualquier otro problema inesperado (por ejemplo, un JSON raro).
            Result.failure(Exception("Error inesperado al buscar la dirección"))
        }
    }
}