package com.rutalista.app.core

import kotlin.math.pow

/**
 * Un punto de la ruta ya decodificado.
 * Se usa una clase propia (y no GeoPoint de osmdroid) para que "core"
 * no dependa de la librería del mapa; la conversión se hará en la capa de UI.
 */
data class PuntoRuta(
    val latitud: Double,
    val longitud: Double
)

/**
 * Convierte el texto comprimido que devuelve OpenRouteService en "geometry"
 * (formato "encoded polyline") en una lista de puntos.
 */
object PolylineDecoder {

    /**
     * @param codificado texto de RutaDetalle.geometry
     * @param precision 5 para ORS con el endpoint /json (es el valor por defecto)
     * @return lista de puntos en orden de recorrido; vacía si el texto está vacío o dañado
     */
    fun decodificar(codificado: String, precision: Int = 5): List<PuntoRuta> {
        // Factor para pasar de entero a decimal (con precisión 5 = 100000.0)
        val factor = 10.0.pow(precision)
        val puntos = ArrayList<PuntoRuta>()

        var indice = 0   // posición actual dentro del texto
        var lat = 0      // latitud acumulada (en enteros)
        var lon = 0      // longitud acumulada (en enteros)

        // Lee UN número codificado y avanza el índice.
        // Devuelve null si el texto se acaba a la mitad (texto dañado).
        fun leerValor(): Int? {
            var resultado = 0
            var desplazamiento = 0
            var byte: Int
            do {
                if (indice >= codificado.length) return null
                byte = codificado[indice++].code - 63   // cada carácter guarda 5 bits
                resultado = resultado or ((byte and 0x1f) shl desplazamiento)
                desplazamiento += 5
            } while (byte >= 0x20)                      // 0x20 = "sigue otro carácter"
            // El bit menos significativo indica el signo
            return if ((resultado and 1) != 0) (resultado shr 1).inv() else (resultado shr 1)
        }

        while (indice < codificado.length) {
            // Cada punto se guarda como diferencia respecto al anterior
            val deltaLat = leerValor() ?: return emptyList()
            val deltaLon = leerValor() ?: return emptyList()
            lat += deltaLat
            lon += deltaLon
            puntos.add(PuntoRuta(lat / factor, lon / factor))
        }
        return puntos
    }
}