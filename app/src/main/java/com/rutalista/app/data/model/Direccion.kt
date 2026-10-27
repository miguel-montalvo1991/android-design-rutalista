package com.rutalista.app.data.model

/**
 * Una dirección ya verificada que la app puede mostrar y guardar.
 * Es el "modelo" de MVVM: solo guarda datos, no tiene lógica.
 *
 * @param texto Nombre completo de la dirección (ej: "Carrera 43A, Medellín").
 * @param lat   Latitud (qué tan al norte o sur está).
 * @param lon   Longitud (qué tan al este u oeste está).
 */
data class Direccion(
    val texto: String,
    val lat: Double,
    val lon: Double
)