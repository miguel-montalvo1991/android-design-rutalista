package com.rutalista.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rutalista.app.ui.screens.ReviewScreen
import com.rutalista.app.ui.screens.RutaScreen
import com.rutalista.app.ui.screens.SummaryScreen
import com.rutalista.app.ui.theme.AccentYellow
import com.rutalista.app.ui.theme.PrimaryNavy
import com.rutalista.app.ui.viewmodel.RutaUiState
import com.rutalista.app.ui.viewmodel.RutaViewModel

sealed class Destination(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Destination("home", "Inicio", Icons.Default.Home)
    data object Import : Destination("import", "Importar", Icons.Default.Home)
    data object Review : Destination("review", "Revisión", Icons.Default.RateReview)
    data object Planning : Destination("planning", "Planificación", Icons.Default.Home)
    data object NextDelivery : Destination("next_delivery", "Siguiente entrega", Icons.Default.Home)
    data object Summary : Destination("summary", "Resumen", Icons.Default.Assessment)
}

/** Pestañas principales visibles en la barra de navegación inferior. */
private val mainTabs = listOf(
    Destination.Home,
    Destination.Review,
    Destination.Summary
)

/** Orden lineal del flujo de la app. */
private val flow = listOf(
    Destination.Home,
    Destination.Import,
    Destination.Review,
    Destination.Planning,
    Destination.NextDelivery,
    Destination.Summary
)

@Composable
fun RutaListaNavGraph(
    rutaViewModel: RutaViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val pedidos by rutaViewModel.pedidos.collectAsState()
    val origen by rutaViewModel.origen.collectAsState()
    val ruta by rutaViewModel.ruta.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = PrimaryNavy,
                contentColor = Color.White
            ) {
                mainTabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != tab.route) {
                                navController.navigate(tab.route) {
                                    popUpTo(Destination.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryNavy,
                            selectedTextColor = AccentYellow,
                            indicatorColor = AccentYellow,
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            flow.forEachIndexed { index, destination ->
                composable(destination.route) {
                    val isLast = index == flow.lastIndex
                    val nextLabel = if (isLast) "Volver al inicio" else "Ir a ${flow[index + 1].title}"
                    val onNext: () -> Unit = {
                        if (isLast) {
                            navController.navigate(Destination.Home.route) {
                                popUpTo(Destination.Home.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(flow[index + 1].route)
                        }
                    }
                    val onBack: (() -> Unit)? = if (index > 0) {
                        { navController.popBackStack() }
                    } else null

                    when (destination) {
                        Destination.Home -> RutaScreen(
                            viewModel = rutaViewModel,
                            onNavigateToReview = { navController.navigate(Destination.Review.route) }
                        )

                        Destination.Review -> ReviewScreen(
                            pedidos = pedidos,
                            onQuitar = { direccion -> rutaViewModel.quitar(direccion) },
                            onMoverArriba = { i -> rutaViewModel.moverArriba(i) },
                            onMoverAbajo = { i -> rutaViewModel.moverAbajo(i) },
                            onContinuar = { navController.navigate(Destination.Summary.route) },
                            onVolverInicio = {
                                navController.navigate(Destination.Home.route) {
                                    popUpTo(Destination.Home.route) { inclusive = true }
                                }
                            },
                            nextLabel = "Ver Resumen de Jornada"
                        )

                        Destination.Summary -> {
                            val resumenRuta = (ruta as? RutaUiState.Exito)?.ruta?.summary
                            SummaryScreen(
                                origen = origen,
                                pedidos = pedidos,
                                distanciaMetros = resumenRuta?.distance,
                                duracionSegundos = resumenRuta?.duration,
                                nextLabel = "Volver al Inicio",
                                onFinalizar = {
                                    navController.navigate(Destination.Home.route) {
                                        popUpTo(Destination.Home.route) { inclusive = true }
                                    }
                                },
                                onAtras = onBack
                            )
                        }

                        else -> PlaceholderScreen(
                            title = destination.title,
                            nextLabel = nextLabel,
                            onNext = onNext,
                            onBack = onBack
                        )
                    }
                }
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
