package com.rutalista.app.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Crea una sola instancia de Retrofit para toda la app ("object" = único).
 *
 * NOTA: el proyecto tiene la librería OkHttp Logging Interceptor, pero NO la
 * usamos aquí a propósito: registraría en Logcat la URL completa, y esa URL
 * lleva la API key como parámetro.
 */
object RetrofitClient {

    // "lazy": se crea la primera vez que se usa, no al abrir la app.
    val api: OrsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/") // debe terminar en "/"
            .addConverterFactory(GsonConverterFactory.create()) // JSON -> clases
            .build()
            .create(OrsApi::class.java)
    }
}