package com.rutalista.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rutalista.app.core.PuntoRuta
import com.rutalista.app.data.model.Direccion
import com.rutalista.app.ui.theme.PrimaryNavy
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * Mapa a pantalla completa que dibuja la ruta calculada:
 * una línea azul con el recorrido, un marcador en el origen y uno por cada parada.
 *
 * Es solo "vista": recibe todo ya listo y no llama a ninguna API.
 *
 * @param origen     punto de partida de la ruta
 * @param destinos   paradas en el MISMO orden en que se calculó la ruta
 * @param puntosRuta puntos del trazado (salen de PolylineDecoder.decodificar)
 * @param onDismiss  se llama al cerrar el mapa
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaRutaDialog(
    origen: Direccion,
    destinos: List<Direccion>,
    puntosRuta: List<PuntoRuta>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Misma configuración de osmdroid del mapa de selección (commit 5):
    // debe ejecutarse ANTES de crear el MapView.
    remember(context) { configurarOsmdroid(context) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Ruta en el mapa",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryNavy),
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            controller.setCenter(GeoPoint(origen.lat, origen.lon))

                            // 1. La línea de la ruta va primero para que los marcadores
                            //    se dibujen ENCIMA de ella.
                            if (puntosRuta.size >= 2) {
                                val linea = Polyline().apply {
                                    setPoints(puntosRuta.map { GeoPoint(it.latitud, it.longitud) })
                                    outlinePaint.color = Color(0xFF1E6FD9).toArgb() // azul visible sobre el mapa
                                    outlinePaint.strokeWidth = 12f
                                }
                                overlays.add(linea)
                            }

                            // 2. Marcador del origen
                            overlays.add(
                                Marker(this).apply {
                                    position = GeoPoint(origen.lat, origen.lon)
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    title = "Origen"
                                    snippet = origen.texto   // se ve al tocar el marcador
                                }
                            )

                            // 3. Un marcador por parada, numerado en el orden del recorrido
                            destinos.forEachIndexed { indice, destino ->
                                overlays.add(
                                    Marker(this).apply {
                                        position = GeoPoint(destino.lat, destino.lon)
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        title = "Parada ${indice + 1}"
                                        snippet = destino.texto
                                    }
                                )
                            }

                            // 4. Encuadrar todo el recorrido. Debe hacerse cuando el mapa ya
                            //    tiene tamaño en pantalla; por eso se usa el listener de
                            //    "primer layout" y no se llama directamente aquí.
                            val todos = buildList {
                                add(GeoPoint(origen.lat, origen.lon))
                                destinos.forEach { add(GeoPoint(it.lat, it.lon)) }
                                puntosRuta.forEach { add(GeoPoint(it.latitud, it.longitud)) }
                            }
                            if (todos.size >= 2) {
                                addOnFirstLayoutListener { _, _, _, _, _ ->
                                    zoomToBoundingBox(BoundingBox.fromGeoPoints(todos), false, 120)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Si la API no trajo el trazado, no fingimos una ruta: lo avisamos
                // y solo se ven los puntos.
                if (puntosRuta.size < 2) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.BottomCenter),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryNavy)
                    ) {
                        Text(
                            text = "No se recibió el trazado de la ruta. Solo se muestran el origen y las paradas.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                        )
                    }
                }
            }
        }
    }
}