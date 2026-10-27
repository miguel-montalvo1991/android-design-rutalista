package com.rutalista.app.data.model

/**
 * Estructura del JSON que responde ORS en GET /geocode/search.
 * Los nombres deben coincidir con los del JSON para que Gson los llene solo.
 *
 * Ejemplo simplificado de la respuesta:
 * { "features": [ { "properties": { "label": "..." },
 *                   "geometry": { "coordinates": [-75.56, 6.24] } } ] }
 */
data class GeocodeResponse(val features: List<GeocodeFeature>)

/** Cada resultado encontrado. */
data class GeocodeFeature(
    val properties: GeocodeProperties,
    val geometry: GeocodeGeometry
)

/** Datos de texto del resultado. */
data class GeocodeProperties(val label: String)

/**
 * IMPORTANTE: en "coordinates" el orden es [longitud, latitud],
 * al revés de lo que uno esperaría.
 */
data class GeocodeGeometry(val coordinates: List<Double>)