package com.rutalista.app.data.remote

import com.rutalista.app.data.model.GeocodeResponse
import com.rutalista.app.data.model.RutaRequest
import com.rutalista.app.data.model.RutaResponse
import com.rutalista.app.data.model.OptimizacionRequest
import com.rutalista.app.data.model.OptimizacionResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface OrsApi {

    // Búsqueda de direcciones
    @GET("pelias/v1/search")
    suspend fun geocodeSearch(
        @Query("api_key") apiKey: String,
        @Query("text") texto: String,
        @Query("boundary.country") pais: String = "CO",
        @Query("focus.point.lon") lon: Double = -75.5636,
        @Query("focus.point.lat") lat: Double = 6.2476,
        @Query("size") tamano: Int = 5
    ): GeocodeResponse

    // Geocodificación inversa
    @GET("pelias/v1/reverse")
    suspend fun geocodeReverse(
        @Query("api_key") apiKey: String,
        @Query("point.lon") lon: Double,
        @Query("point.lat") lat: Double,
        @Query("size") tamano: Int = 1
    ): GeocodeResponse

    // Cálculo de ruta
    @POST("openrouteservice/v2/directions/driving-car/json")
    suspend fun calcularRuta(
        @Header("Authorization") apiKey: String,
        @Body solicitud: RutaRequest
    ): RutaResponse

    // Optimización de destinos
    @POST("vroom/v0")
    suspend fun optimizarRuta(
        @Header("Authorization") apiKey: String,
        @Body solicitud: OptimizacionRequest
    ): OptimizacionResponse
}