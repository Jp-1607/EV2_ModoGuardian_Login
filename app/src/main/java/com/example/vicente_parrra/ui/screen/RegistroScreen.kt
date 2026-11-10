package com.example.vicente_parrra.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.vicente_parrra.viewmodel.UsuarioViewModel

@Composable
fun RegistroScreen(modelo: UsuarioViewModel, continuar: () -> Unit) {
    val datos by modelo.estado.collectAsState()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Registro", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(datos.nombre, modelo::nombre, label = { Text("Nombre") },
            isError = datos.errores.nombre != null, supportingText = { datos.errores.nombre?.let { Text(it) } })
        OutlinedTextField(datos.correo, modelo::correo, label = { Text("Correo") },
            isError = datos.errores.correo != null, supportingText = { datos.errores.correo?.let { Text(it) } })
        OutlinedTextField(datos.clave, modelo::clave, label = { Text("Clave") },
            visualTransformation = PasswordVisualTransformation(),
            isError = datos.errores.clave != null, supportingText = { datos.errores.clave?.let { Text(it) } })
        Button(onClick = { if (modelo.validarFormulario()) continuar() }) { Text("Ver resumen") }
    }
}

@Composable
fun ResumenScreen(modelo: UsuarioViewModel, volver: () -> Unit) {
    val datos by modelo.estado.collectAsState()
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Resumen del registro", style = MaterialTheme.typography.headlineSmall)
        Text("Nombre: ${datos.nombre}")
        Text("Correo: ${datos.correo}")
        Text("Este formulario no crea una cuenta de acceso.")
        Button(onClick = volver) { Text("Volver") }
    }
}
