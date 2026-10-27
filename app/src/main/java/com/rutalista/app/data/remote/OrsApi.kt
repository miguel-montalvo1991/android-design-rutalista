package com.rutalista.app.data.remote

import com.rutalista.app.data.model.GeocodeResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Describe los endpoints de OpenRouteService que usa la app.
 * Retrofit genera el código de red a partir de esta interfaz.
 */
interface OrsApi {

    /**
     * Convierte un texto de dirección en coordenadas.
     * URL final: https://api.openrouteservice.org/geocode/search?api_key=...&text=...
     *
     * "suspend" significa que se llama desde una corrutina (no bloquea la pantalla).
     */
    @GET("geocode/search")
    suspend fun geocodeSearch(
        @Query("api_key") apiKey: String,          // la clave (viene de BuildConfig)
        @Query("text") texto: String,              // lo que escribió el usuario
        @Query("boundary.country") pais: String = "CO",   // solo resultados de Colombia
        @Query("focus.point.lon") lon: Double = -75.5636, // da prioridad a Medellín
        @Query("focus.point.lat") lat: Double = 6.2476,
        @Query("size") tamano: Int = 5             // máximo 5 resultados
    ): GeocodeResponse
}