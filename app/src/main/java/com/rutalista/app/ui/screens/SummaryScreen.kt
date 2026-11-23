package com.rutalista.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rutalista.app.data.model.Direccion
import com.rutalista.app.ui.theme.AccentYellow
import com.rutalista.app.ui.theme.PrimaryNavy
import com.rutalista.app.ui.theme.RutaListaTheme
import com.rutalista.app.ui.theme.SoftBlueBg
import com.rutalista.app.ui.theme.SurfaceLight
import com.rutalista.app.ui.theme.TextMuted
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Vista de resumen de la jornada.
 * No conoce el ViewModel: recibe los datos y las funciones por parámetro.
 * Si la ruta aún no se calculó, distanciaMetros y duracionSegundos llegan en null.
 */
@Composable
fun SummaryScreen(
    origen: Direccion?,
    pedidos: List<Direccion>,
    distanciaMetros: Double?,
    duracionSegundos: Double?,
    nextLabel: String,
    onFinalizar: () -> Unit,
    modifier: Modifier = Modifier,
    onAtras: (() -> Unit)? = null
) {
    val rutaCalculada = distanciaMetros != null && duracionSegundos != null

    Scaffold(
        modifier = modifier,
        containerColor = SoftBlueBg,
        bottomBar = {
            SummaryBottomBar(
                nextLabel = nextLabel,
                onFinalizar = onFinalizar,
                onAtras = onAtras
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { SummaryHeader() }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        icon = { Icon(Icons.Default.Place, null, tint = AccentYellow) },
                        valor = "${pedidos.size}",
                        etiqueta = if (pedidos.size == 1) "Destino" else "Destinos"
                    )
                    MetricCard(
                        icon = { Icon(Icons.Default.Navigation, null, tint = AccentYellow) },
                        valor = distanciaMetros?.let { formatearDistancia(it) } ?: "--",
                        etiqueta = "Distancia"
                    )
                    MetricCard(
                        icon = { Icon(Icons.Default.Schedule, null, tint = AccentYellow) },
                        valor = duracionSegundos?.let { formatearDuracion(it) } ?: "--",
                        etiqueta = "Duración"
                    )
                }
            }

            if (!rutaCalculada) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = AccentYellow.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = "Aún no has calculado la ruta. Vuelve al inicio y pulsa \"Calcular Ruta Directa\" para ver la distancia y la duración.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = PrimaryNavy),
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Recorrido",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                )
            }

            item {
                ParadaRow(
                    icono = { Icon(Icons.Default.Flag, null, tint = AccentYellow, modifier = Modifier.size(20.dp)) },
                    titulo = "Origen",
                    texto = origen?.texto ?: "Sin origen seleccionado",
                    destacado = true
                )
            }

            if (pedidos.isEmpty()) {
                item {
                    Text(
                        text = "No hay destinos en el recorrido.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                    )
                }
            } else {
                pedidos.forEachIndexed { index, direccion ->
                    item(key = "parada-$index") {
                        ParadaRow(
                            icono = {
                                Text(
                                    text = "${index + 1}",
                                    color = AccentYellow,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            titulo = "Parada ${index + 1}",
                            texto = direccion.texto,
                            destacado = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AccentYellow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = PrimaryNavy
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Resumen de jornada",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Así quedó tu recorrido de hoy",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.75f)
                    )
                )
            }
        }
    }
}

@Composable
private fun RowScope.MetricCard(
    icon: @Composable () -> Unit,
    valor: String,
    etiqueta: String
) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icon()
            Text(
                text = valor,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White.copy(alpha = 0.7f)
                )
            )
        }
    }
}

@Composable
private fun ParadaRow(
    icono: @Composable () -> Unit,
    titulo: String,
    texto: String,
    destacado: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = if (destacado) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryNavy),
                contentAlignment = Alignment.Center
            ) {
                icono()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.labelMedium.copy(color = TextMuted)
                )
                Text(
                    text = texto,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SummaryBottomBar(
    nextLabel: String,
    onFinalizar: () -> Unit,
    onAtras: (() -> Unit)?
) {
    Surface(shadowElevation = 8.dp, color = SurfaceLight) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onAtras != null) {
                OutlinedButton(
                    onClick = onAtras,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Atrás")
                }
            }
            Button(
                onClick = onFinalizar,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentYellow,
                    contentColor = PrimaryNavy
                )
            ) {
                Text(nextLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatearDistancia(metros: Double): String =
    if (metros < 1000) "${metros.roundToInt()} m"
    else String.format(Locale.US, "%.1f km", metros / 1000.0)

private fun formatearDuracion(segundos: Double): String {
    val minutos = (segundos / 60.0).roundToInt()
    return if (minutos >= 60) "${minutos / 60} h ${minutos % 60} min" else "$minutos min"
}

@Preview(showBackground = true, showSystemUi = true, name = "Ruta calculada")
@Composable
private fun SummaryScreenPreview() {
    RutaListaTheme {
        SummaryScreen(
            origen = Direccion("Calle 30 #65-10, Belén, Medellín", 6.23000, -75.60000),
            pedidos = listOf(
                Direccion("Carrera 43A #1-50, El Poblado, Medellín", 6.21010, -75.57100),
                Direccion("Avenida 80 #45-20, Laureles, Medellín", 6.25500, -75.59000)
            ),
            distanciaMetros = 12400.0,
            duracionSegundos = 2340.0,
            nextLabel = "Volver al inicio",
            onFinalizar = {},
            onAtras = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Sin ruta calculada")
@Composable
private fun SummaryScreenSinRutaPreview() {
    RutaListaTheme {
        SummaryScreen(
            origen = null,
            pedidos = emptyList(),
            distanciaMetros = null,
            duracionSegundos = null,
            nextLabel = "Volver al inicio",
            onFinalizar = {},
            onAtras = {}
        )
    }
}