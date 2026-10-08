package com.example.vicente_parrra.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vicente_parrra.viewmodel.LoginViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeSupervisorScreen(
    loginViewModel: LoginViewModel,
    onCerrarSesion: () -> Unit
) {
    val estado by loginViewModel.estado.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Inicio Supervisor") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bienvenido Supervisor", style = MaterialTheme.typography.headlineSmall)
                    Text(estado.usuarioLogueado?.email ?: "")
                }
            }

            Text("Opciones")
            Text("- Indicadores")
            Text("- Pedidos")
            Text("- Entregas")

            Button(
                onClick = {
                    loginViewModel.cerrarSesion()
                    onCerrarSesion()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}
