package com.rutalista.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.rutalista.app.ui.screens.RutaScreen
import com.rutalista.app.ui.theme.RutaListaTheme
import com.rutalista.app.ui.viewmodel.RutaViewModel

class MainActivity : ComponentActivity() {

    private val rutaViewModel: RutaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RutaListaTheme {
                RutaScreen(viewModel = rutaViewModel)
            }
        }
    }
}