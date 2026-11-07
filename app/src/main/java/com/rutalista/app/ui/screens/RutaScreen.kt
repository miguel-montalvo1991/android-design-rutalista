package com.rutalista.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.rutalista.app.data.model.Direccion
import com.rutalista.app.ui.components.HeaderRutaLista
import com.rutalista.app.ui.components.MapaSeleccionDialog
import com.rutalista.app.ui.theme.*
import com.rutalista.app.ui.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutaScreen(viewModel: RutaViewModel) {

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var textoBusqueda by remember { mutableStateOf("") }
    var modoSeleccionOrigen by remember { mutableStateOf(true) } // true: para origen, false: para agregar destino
    var indexReemplazarDestino by remember { mutableStateOf<Int?>(null) }

    var mostrarMapaDialog by remember { mutableStateOf(false) }

    val origen by viewModel.origen.collectAsState()
    val busqueda by viewModel.busqueda.collectAsState()
    val pedidos by viewModel.pedidos.collectAsState()
    val ruta by viewModel.ruta.collectAsState()
    val optimizacion by viewModel.optimizacion.collectAsState()
    val ubicacionGpsState by viewModel.ubicacionGps.collectAsState()

    // Manejador de permisos de ubicación GPS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            viewModel.obtenerUbicacionGps(
                context = context,
                esOrigen = modoSeleccionOrigen,
                indexDestino = indexReemplazarDestino
            )
            indexReemplazarDestino = null
        } else {
            Toast.makeText(
                context,
                "Permiso de ubicación denegado. Puedes ingresarla manualmente o en el mapa.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun solicitarUbicacionGps(esOrigen: Boolean, indexDestino: Int? = null) {
        modoSeleccionOrigen = esOrigen
        indexReemplazarDestino = indexDestino

        val finePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarsePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (finePermission || coarsePermission) {
            viewModel.obtenerUbicacionGps(
                context = context,
                esOrigen = esOrigen,
                indexDestino = indexDestino
            )
            indexReemplazarDestino = null
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        containerColor = SoftBlueBg
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. Header de la aplicación
            item {
                HeaderRutaLista()
            }

            // 2. Buscador Manual e Interactivo de Direcciones
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "1. Búsqueda y Selección Manual",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                        )

                        OutlinedTextField(
                            value = textoBusqueda,
                            onValueChange = { textoBusqueda = it },
                            label = { Text("Escribe o pega una dirección") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = PrimaryNavy)
                            },
                            trailingIcon = {
                                if (textoBusqueda.isNotEmpty()) {
                                    IconButton(onClick = {
                                        textoBusqueda = ""
                                        viewModel.limpiarBusqueda()
                                    }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                keyboardController?.hide()
                                viewModel.buscar(textoBusqueda)
                            }),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    viewModel.buscar(textoBusqueda)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buscar")
                            }

                            // Botón de Abrir Mapa
                            OutlinedButton(
                                onClick = {
                                    modoSeleccionOrigen = true
                                    mostrarMapaDialog = true
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mapa")
                            }
                        }

                        // Estado de Búsqueda
                        when (busqueda) {
                            is BusquedaUiState.Cargando ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Buscando direcciones...", style = MaterialTheme.typography.bodySmall)
                                }

                            is BusquedaUiState.Error ->
                                Text(
                                    text = (busqueda as BusquedaUiState.Error).mensaje,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )

                            is BusquedaUiState.Inicial -> Unit

                            is BusquedaUiState.Exito -> {
                                Text(
                                    text = "Sugerencias encontradas (${(busqueda as BusquedaUiState.Exito).resultados.size}):",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryNavy)
                                )
                            }
                        }
                    }
                }
            }

            // Resultados de Sugerencias de Búsqueda
            if (busqueda is BusquedaUiState.Exito) {
                items((busqueda as BusquedaUiState.Exito).resultados) { direccion ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = direccion.texto,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        viewModel.establecerOrigen(direccion)
                                        viewModel.limpiarBusqueda()
                                        textoBusqueda = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Establecer Origen", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.agregar(direccion)
                                        viewModel.limpiarBusqueda()
                                        textoBusqueda = ""
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Agregar Destino", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Tarjeta de Origen
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryNavy),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = AccentYellow
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Punto de Origen",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            if (origen != null) {
                                TextButton(onClick = { viewModel.establecerOrigen(null) }) {
                                    Text("Borrar", color = Color.White.copy(alpha = 0.7f))
                                }
                            }
                        }

                        if (origen != null) {
                            Text(
                                text = origen!!.texto,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        } else {
                            Text(
                                text = "No se ha seleccionado una dirección de origen.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f))
                            )
                        }

                        // Botones de 3 métodos para Origen
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    modoSeleccionOrigen = true
                                    mostrarMapaDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentYellow, contentColor = PrimaryNavy),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mapa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { solicitarUbicacionGps(esOrigen = true) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("GPS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Status de GPS si está cargando o con error
            item {
                when (ubicacionGpsState) {
                    is UbicacionGpsUiState.Cargando ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Obteniendo posición GPS...", style = MaterialTheme.typography.bodySmall)
                        }

                    is UbicacionGpsUiState.Error ->
                        Text(
                            text = (ubicacionGpsState as UbicacionGpsUiState.Error).mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                    else -> Unit
                }
            }

            // 4. Lista de Destinos Seleccionados
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Destinos seleccionados (${pedidos.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                    )

                    // Botón rápido para agregar destino mediante GPS o Mapa
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = {
                            modoSeleccionOrigen = false
                            indexReemplazarDestino = null
                            mostrarMapaDialog = true
                        }) {
                            Icon(imageVector = Icons.Default.Map, contentDescription = "Agregar en Mapa", tint = PrimaryNavy)
                        }

                        IconButton(onClick = { solicitarUbicacionGps(esOrigen = false) }) {
                            Icon(imageVector = Icons.Default.MyLocation, contentDescription = "Agregar por GPS", tint = PrimaryNavy)
                        }
                    }
                }
            }

            if (pedidos.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
                    ) {
                        Text(
                            text = "Aún no has agregado ningún destino. Utiliza la búsqueda manual, el mapa o el GPS para añadir paradas a tu recorrido.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                itemsIndexed(pedidos) { index, direccion ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryNavy),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    color = AccentYellow,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = direccion.texto,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.weight(1f)
                            )

                            Row {
                                IconButton(onClick = {
                                    modoSeleccionOrigen = false
                                    indexReemplazarDestino = index
                                    mostrarMapaDialog = true
                                }) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar en Mapa", tint = PrimaryNavy)
                                }

                                IconButton(onClick = { viewModel.quitar(direccion) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            // 5. Botones de Acción (Calcular y Optimizar)
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { viewModel.calcularRuta() },
                        enabled = origen != null && pedidos.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryNavy,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Navigation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calcular Ruta Directa", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Button(
                        onClick = { viewModel.optimizarRuta() },
                        enabled = origen != null && pedidos.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentYellow,
                            contentColor = PrimaryNavy
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Directions, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Optimizar Orden de Destinos", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // 6. Resultado de la Ruta
            item {
                when (ruta) {
                    is RutaUiState.Cargando ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLight)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Calculando la mejor ruta...")
                            }
                        }

                    is RutaUiState.Error ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text(
                                text = (ruta as RutaUiState.Error).mensaje,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                    is RutaUiState.Exito -> {
                        val resultado = (ruta as RutaUiState.Exito).ruta

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Resultado de la Ruta",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryNavy)
                                )
                                Text("Distancia total: ${(resultado.summary?.distance ?: 0.0) / 1000.0} km")
                                Text("Duración estimada: ${(resultado.summary?.duration ?: 0.0) / 60.0} min")
                            }
                        }
                    }

                    is RutaUiState.Inicial -> Unit
                }
            }

            // 7. Resultado de la Optimización
            item {
                when (optimizacion) {
                    is OptimizacionUiState.Cargando ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLight)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Optimizando itinerario de recorrido...")
                            }
                        }

                    is OptimizacionUiState.Error ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text(
                                text = (optimizacion as OptimizacionUiState.Error).mensaje,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                    is OptimizacionUiState.Exito -> {
                        val resultado = (optimizacion as OptimizacionUiState.Exito).resultado

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Recorrido Optimizado",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryNavy)
                                )

                                resultado.routes.forEach { recorrido ->
                                    Text("Distancia total: ${recorrido.distance / 1000.0} km")
                                    Text("Duración estimada: ${recorrido.duration / 60.0} min")

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Secuencia de paradas sugerida:", fontWeight = FontWeight.Bold)

                                    recorrido.steps.forEach { paso ->
                                        if (paso.type == "job") {
                                            val idVal = paso.id ?: 1
                                            val destIndex = idVal - 1
                                            val destNombre = pedidos.getOrNull(destIndex)?.texto ?: "Destino #${idVal}"
                                            Text("• Parada ${idVal}: $destNombre", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }

                                if (resultado.unassigned.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Advertencia: ${resultado.unassigned.size} destino(s) no pudieron ser asignados.",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    is OptimizacionUiState.Inicial -> Unit
                }
            }
        }
    }

    // Modal de Mapa osmdroid
    if (mostrarMapaDialog) {
        val puntoInit = if (modoSeleccionOrigen) origen else indexReemplazarDestino?.let { pedidos.getOrNull(it) }

        MapaSeleccionDialog(
            puntoInicial = puntoInit,
            onConfirmar = { direccionSeleccionada ->
                if (modoSeleccionOrigen) {
                    viewModel.establecerOrigen(direccionSeleccionada)
                } else if (indexReemplazarDestino != null) {
                    viewModel.reemplazarDestino(indexReemplazarDestino!!, direccionSeleccionada)
                    indexReemplazarDestino = null
                } else {
                    viewModel.agregar(direccionSeleccionada)
                }
                mostrarMapaDialog = false
            },
            onGeocodificarRequest = { lat, lon, callback ->
                viewModel.geocodificarPuntoMapa(lat, lon, callback)
            },
            onDismiss = {
                mostrarMapaDialog = false
                indexReemplazarDestino = null
            }
        )
    }
}
