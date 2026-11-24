package com.example.vicente_parrra.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vicente_parrra.model.LoginUiState
import com.example.vicente_parrra.ui.components.Accion
import com.example.vicente_parrra.ui.components.Opcion
import com.example.vicente_parrra.ui.theme.Espacio
import com.example.vicente_parrra.ui.theme.Marfil
import com.example.vicente_parrra.ui.theme.Papel
import com.example.vicente_parrra.ui.theme.Pino
import com.example.vicente_parrra.ui.theme.Vicente_parrraTheme
import com.example.vicente_parrra.ui.utils.WindowSizeClass
import com.example.vicente_parrra.ui.utils.rememberWindowSizeClass
import com.example.vicente_parrra.viewmodel.LoginViewModel

@Composable
fun LoginScreen(modelo: LoginViewModel) {
    val estado by modelo.estado.collectAsStateWithLifecycle()
    Acceso(estado, modelo::onEmailChange, modelo::onPasswordChange, modelo::recordarCorreo, modelo::iniciarSesion)
}

@Composable
private fun Acceso(
    estado: LoginUiState,
    correo: (String) -> Unit,
    clave: (String) -> Unit,
    recordar: (Boolean) -> Unit,
    entrar: () -> Unit
) {
    val compacto = rememberWindowSizeClass() == WindowSizeClass.COMPACT
    Box(Modifier.fillMaxSize().imePadding().background(Marfil), contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 1000.dp).verticalScroll(rememberScrollState()).padding(Espacio.xl)) {
            if (compacto) {
                Column(verticalArrangement = Arrangement.spacedBy(Espacio.xl)) {
                    Marca(Modifier.fillMaxWidth())
                    FormularioLogin(estado, correo, clave, recordar, entrar, Modifier.fillMaxWidth())
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(Espacio.xxl), verticalAlignment = Alignment.CenterVertically) {
                    Marca(Modifier.weight(1f))
                    FormularioLogin(estado, correo, clave, recordar, entrar, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun Marca(modifier: Modifier) {
    Column(
        modifier.background(Pino, MaterialTheme.shapes.medium).padding(Espacio.xl),
        verticalArrangement = Arrangement.spacedBy(Espacio.s)
    ) {
        Text("InsertCode", color = Papel, style = MaterialTheme.typography.labelLarge)
        Text("Ventas y\ndistribución", color = Papel, style = MaterialTheme.typography.displaySmall)
    }
}

@Composable
private fun FormularioLogin(
    estado: LoginUiState,
    correo: (String) -> Unit,
    clave: (String) -> Unit,
    recordar: (Boolean) -> Unit,
    entrar: () -> Unit,
    modifier: Modifier
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Espacio.m)) {
        Text("Acceso al sistema", Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineLarge)
        Text("Ingresa con tu cuenta de prueba.")
        OutlinedTextField(
            estado.email, correo, Modifier.fillMaxWidth(), enabled = !estado.isLoading,
            label = { Text("Correo electrónico") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = estado.errores.email != null,
            supportingText = estado.errores.email?.let { error -> { Text(error) } }
        )
        OutlinedTextField(
            estado.password, clave, Modifier.fillMaxWidth(), enabled = !estado.isLoading,
            label = { Text("Contraseña") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = estado.errores.password != null,
            supportingText = estado.errores.password?.let { error -> { Text(error) } },
            trailingIcon = { TextButton(onClick = { visible = !visible }) { Text(if (visible) "Ocultar" else "Ver") } }
        )
        Opcion("Recordar correo", estado.recordarCorreo, !estado.isLoading, recordar)
        estado.errores.general?.let {
            Text(it, Modifier.semantics { liveRegion = LiveRegionMode.Polite }, color = MaterialTheme.colorScheme.error)
        }
        Accion("Ingresar", entrar, Modifier.fillMaxWidth(), habilitada = estado.isLoginEnabled)
        if (estado.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
    }
}

@Preview(widthDp = 380, heightDp = 760)
@Preview(widthDp = 900, heightDp = 580)
@Composable
private fun VistaAcceso() {
    Vicente_parrraTheme { Acceso(LoginUiState(), {}, {}, {}, {}) }
}
