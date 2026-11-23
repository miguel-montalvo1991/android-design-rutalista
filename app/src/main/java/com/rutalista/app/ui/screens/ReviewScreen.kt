package com.rutalista.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutalista.app.data.model.Direccion
import com.rutalista.app.ui.theme.AccentYellow
import com.rutalista.app.ui.theme.PrimaryNavy
import com.rutalista.app.ui.theme.SoftBlueBg
import com.rutalista.app.ui.theme.SurfaceLight
import com.rutalista.app.ui.theme.TextMuted
import java.util.Locale

/**
 * Vista de revisión de pedidos.
 * Permite reorganizar manualmente el orden de las paradas usando botones de subir/bajar.
 */
@Composable
fun ReviewScreen(
    pedidos: List<Direccion>,
    onQuitar: (Direccion) -> Unit,
    modifier: Modifier = Modifier,
    onMoverArriba: (Int) -> Unit = {},
    onMoverAbajo: (Int) -> Unit = {},
    onContinuar: () -> Unit,
    onVolverInicio: (() -> Unit)? = null,
    nextLabel: String = "Ver Resumen de Jornada"
) {
    var pedidoAQuitar by remember { mutableStateOf<Direccion?>(null) }

    Scaffold(
        modifier = modifier,
        containerColor = SoftBlueBg,
        bottomBar = {
            ReviewBottomBar(
                nextLabel = nextLabel,
                continuarHabilitado = pedidos.isNotEmpty(),
                onContinuar = onContinuar,
                onVolverInicio = onVolverInicio
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ReviewHeader(total = pedidos.size) }

            if (pedidos.isEmpty()) {
                item { ReviewEmptyState() }
            } else {
                itemsIndexed(pedidos) { index, direccion ->
                    PedidoCard(
                        index = index,
                        totalItems = pedidos.size,
                        direccion = direccion,
                        onMoverArriba = { onMoverArriba(index) },
                        onMoverAbajo = { onMoverAbajo(index) },
                        onQuitar = { pedidoAQuitar = direccion }
                    )
                }
            }
        }
    }

    pedidoAQuitar?.let { direccion ->
        AlertDialog(
            onDismissRequest = { pedidoAQuitar = null },
            title = { Text("¿Quitar este destino?") },
            text = { Text(direccion.texto) },
            confirmButton = {
                TextButton(onClick = {
                    onQuitar(direccion)
                    pedidoAQuitar = null
                }) {
                    Text("Quitar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pedidoAQuitar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ReviewHeader(total: Int) {
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
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = AccentYellow,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Revisión de Paradas",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Reordena las paradas manualmente con las flechas o elimina destinos.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}

@Composable
private fun PedidoCard(
    index: Int,
    totalItems: Int,
    direccion: Direccion,
    onMoverArriba: () -> Unit,
    onMoverAbajo: () -> Unit,
    onQuitar: () -> Unit
) {
    val numero = index + 1
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryNavy),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$numero",
                    color = AccentYellow,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = direccion.texto,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Controles para subir o bajar el orden manualmente
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = onMoverArriba,
                    enabled = index > 0,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Subir",
                        tint = if (index > 0) PrimaryNavy else Color.LightGray
                    )
                }

                IconButton(
                    onClick = onMoverAbajo,
                    enabled = index < totalItems - 1,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Bajar",
                        tint = if (index < totalItems - 1) PrimaryNavy else Color.LightGray
                    )
                }
            }

            IconButton(onClick = onQuitar, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Quitar destino $numero",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ReviewEmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(PrimaryNavy.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = PrimaryNavy,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = "Aún no hay destinos",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )
            )
            Text(
                text = "Vuelve al inicio y agrega direcciones con la búsqueda, el mapa o el GPS.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReviewBottomBar(
    nextLabel: String,
    continuarHabilitado: Boolean,
    onContinuar: () -> Unit,
    onVolverInicio: (() -> Unit)? = null
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
            if (onVolverInicio != null) {
                OutlinedButton(
                    onClick = onVolverInicio,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Inicio", fontWeight = FontWeight.SemiBold)
                }
            }

            Button(
                onClick = onContinuar,
                enabled = continuarHabilitado,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNavy,
                    contentColor = Color.White
                )
            ) {
                Text(nextLabel, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
