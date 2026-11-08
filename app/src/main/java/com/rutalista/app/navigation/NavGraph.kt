package com.rutalista.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Destination(val route: String, val title: String) {
    data object Home : Destination("home", "Inicio")
    data object Import : Destination("import", "Importar pedidos")
    data object Review : Destination("review", "Revisión de pedidos")
    data object Addresses : Destination("addresses", "Direcciones")
    data object Planning : Destination("planning", "Planificación")
    data object NextDelivery : Destination("next_delivery", "Siguiente entrega")
    data object Summary : Destination("summary", "Resumen de jornada")
}

/** Orden lineal del flujo de la app. */
private val flow = listOf(
    Destination.Home,
    Destination.Import,
    Destination.Review,
    Destination.Addresses,
    Destination.Planning,
    Destination.NextDelivery,
    Destination.Summary
)

@Composable
fun RutaListaNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Destination.Home.route) {
        flow.forEachIndexed { index, destination ->
            composable(destination.route) {
                val isLast = index == flow.lastIndex
                PlaceholderScreen(
                    title = destination.title,
                    nextLabel = if (isLast) "Volver al inicio" else "Ir a ${flow[index + 1].title}",
                    onNext = {
                        if (isLast) {
                            navController.navigate(Destination.Home.route) {
                                popUpTo(Destination.Home.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(flow[index + 1].route)
                        }
                    },
                    onBack = if (index > 0) {
                        { navController.popBackStack() }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    nextLabel: String,
    onNext: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Text(text = "Pantalla pendiente de diseño", style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onNext) { Text(nextLabel) }
        if (onBack != null) {
            Button(onClick = onBack) { Text("Atrás") }
        }
    }
}