package com.rutalista.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PolylineDecoderTest {

    @Test
    fun decodifica_el_ejemplo_oficial_de_google() {
        // Ejemplo de la documentación de Google: 3 puntos conocidos
        val puntos = PolylineDecoder.decodificar("_p~iF~ps|U_ulLnnqC_mqNvxq`@")

        assertEquals(3, puntos.size)
        assertEquals(38.5, puntos[0].latitud, 0.00001)
        assertEquals(-120.2, puntos[0].longitud, 0.00001)
        assertEquals(40.7, puntos[1].latitud, 0.00001)
        assertEquals(-120.95, puntos[1].longitud, 0.00001)
        assertEquals(43.252, puntos[2].latitud, 0.00001)
        assertEquals(-126.453, puntos[2].longitud, 0.00001)
    }

    @Test
    fun texto_vacio_devuelve_lista_vacia() {
        assertTrue(PolylineDecoder.decodificar("").isEmpty())
    }

    @Test
    fun texto_cortado_a_la_mitad_devuelve_lista_vacia_sin_fallar() {
        // Se corta el ejemplo a mitad de un punto: no debe lanzar excepción
        assertTrue(PolylineDecoder.decodificar("_p~iF~ps|U_ulL").isEmpty())
    }
}