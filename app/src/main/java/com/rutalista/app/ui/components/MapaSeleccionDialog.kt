package com.rutalista.app.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rutalista.app.data.model.Direccion
import com.rutalista.app.ui.theme.AccentYellow
import com.rutalista.app.ui.theme.PrimaryNavy
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale

// Osmdroid se configura una sola vez por proceso y siempre ANTES de crear el MapView.
// Si se configura después, el mapa puede salir gris porque aún no tiene el user agent
// ni la carpeta de caché.
private var osmdroidConfigurado = false

private fun configurarOsmdroid(context: Context) {
    if (osmdroidConfigurado) return
    Configuration.getInstance().load(
        context,
        context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
    )
    Configuration.getInstance().userAgentValue = context.packageName
    osmdroidConfigurado = true
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaSeleccionDialog(
    puntoInicial: Direccion? = null,
    onConfirmar: (Direccion) -> Unit,
    onGeocodificarRequest: (Double, Double, (Direccion) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Se ejecuta durante la composición, antes de que AndroidView cree el MapView
    remember(context) { configurarOsmdroid(context) }

    var latitudSeleccionada by remember {
        mutableDoubleStateOf(puntoInicial?.lat ?: 6.2476)
    }
    var longitudSeleccionada by remember {
        mutableDoubleStateOf(puntoInicial?.lon ?: -75.5636)
    }

    // Solo se puede confirmar si hay un punto inicial o si el usuario tocó el mapa.
    // Las coordenadas por defecto son solo el centro de la vista, no una selección.
    var puntoElegido by remember { mutableStateOf(puntoInicial != null) }

    var direccionPreview by remember {
        mutableStateOf(
            puntoInicial?.texto ?: String.format(
                Locale.US,
                "Lat: %.5f, Lon: %.5f",
                latitudSeleccionada,
                longitudSeleccionada
            )
        )
    }
    var cargandoGeocode by remember { mutableStateOf(false) }

    // Número de la última consulta de dirección. Una respuesta solo se aplica si
    // su número sigue siendo el actual; así una respuesta lenta de un toque anterior
    // no pisa la dirección del punto más reciente.
    var idSolicitud by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Seleccionar en Mapa",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PrimaryNavy
                    ),
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

                            val startPoint = GeoPoint(latitudSeleccionada, longitudSeleccionada)
                            controller.setCenter(startPoint)

                            val marker = Marker(this).apply {
                                position = startPoint
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = "Punto Seleccionado"
                                // Sin punto inicial el marcador queda oculto hasta el primer toque
                                isEnabled = puntoInicial != null
                            }
                            overlays.add(marker)

                            val mapEventsReceiver = object : MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                    p?.let { point ->
                                        latitudSeleccionada = point.latitude
                                        longitudSeleccionada = point.longitude
                                        puntoElegido = true

                                        marker.position = point
                                        marker.isEnabled = true
                                        invalidate()

                                        // Mientras llega la dirección, la vista previa muestra las
                                        // coordenadas del nuevo punto (no la dirección del anterior).
                                        // Si la geocodificación falla, se conservan estas coordenadas.
                                        direccionPreview = String.format(
                                            Locale.US,
                                            "Lat: %.5f, Lon: %.5f",
                                            point.latitude,
                                            point.longitude
                                        )

                                        cargandoGeocode = true
                                        idSolicitud += 1
                                        val miSolicitud = idSolicitud
                                        onGeocodificarRequest(point.latitude, point.longitude) { dir ->
                                            if (miSolicitud == idSolicitud) {
                                                direccionPreview = dir.texto
                                                cargandoGeocode = false
                                            }
                                        }
                                    }
                                    return true
                                }

                                override fun longPressHelper(p: GeoPoint?): Boolean {
                                    return singleTapConfirmedHelper(p)
                                }
                            }

                            overlays.add(0, MapEventsOverlay(mapEventsReceiver))
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Tarjeta flotante con previsualización y botón de confirmación
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryNavy),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Ubicación",
                                tint = AccentYellow
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ubicación seleccionada",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = AccentYellow,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        if (!puntoElegido) {
                            Text(
                                text = "Toca el mapa para elegir un punto.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                            )
                        } else if (cargandoGeocode) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Obteniendo dirección...", color = Color.White)
                            }
                        } else {
                            Text(
                                text = direccionPreview,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                            )
                        }

                        if (puntoElegido) {
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "Lat: %.5f | Lon: %.5f",
                                    latitudSeleccionada,
                                    longitudSeleccionada
                                ),
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f))
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                onConfirmar(
                                    Direccion(
                                        texto = direccionPreview,
                                        lat = latitudSeleccionada,
                                        lon = longitudSeleccionada
                                    )
                                )
                            },
                            // No se confirma sin haber elegido un punto ni mientras se carga la dirección
                            enabled = puntoElegido && !cargandoGeocode,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentYellow,
                                contentColor = PrimaryNavy,
                                disabledContainerColor = AccentYellow.copy(alpha = 0.4f),
                                disabledContentColor = PrimaryNavy.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirmar Ubicación", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}