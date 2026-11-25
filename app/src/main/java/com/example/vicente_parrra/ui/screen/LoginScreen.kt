package com.example.vicente_parrra.ui.screen

import android.accounts.AccountManager
import android.app.Activity
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.vicente_parrra.model.DesafioGoogle
import com.example.vicente_parrra.model.LoginUiState
import com.example.vicente_parrra.ui.components.Accion
import com.example.vicente_parrra.ui.components.Opcion
import com.example.vicente_parrra.ui.theme.Espacio
import com.example.vicente_parrra.ui.theme.Marfil
import com.example.vicente_parrra.ui.theme.Papel
import com.example.vicente_parrra.ui.theme.Pino
import com.example.vicente_parrra.ui.theme.Vicente_parrraTheme
import com.example.vicente_parrra.ui.utils.GoogleCuentaAndroid
import com.example.vicente_parrra.ui.utils.WindowSizeClass
import com.example.vicente_parrra.ui.utils.rememberWindowSizeClass
import com.example.vicente_parrra.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(modelo: LoginViewModel) {
    val estado by modelo.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    var desafioDemo by remember { mutableStateOf<DesafioGoogle?>(null) }
    val elegirCuenta = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
        val desafio = desafioDemo
        val nombre = resultado.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        val tipo = resultado.data?.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
        if (resultado.resultCode == Activity.RESULT_OK && desafio != null && !nombre.isNullOrBlank() && tipo == "com.google") {
            desafioDemo = null
            modelo.ingresarGoogleDemo(desafio, GoogleCuentaAndroid.seudonimoDemo(contexto, nombre))
        }
    }
    Acceso(
        estado, modelo::onNombreChange, modelo::onEmailChange, modelo::onPasswordChange, modelo::recordarCorreo,
        modelo::iniciarSesion, modelo::cambiarModo, google = {
            alcance.launch {
                modelo.iniciarGoogle({ desafioDemo = it }) { cliente, nonce ->
                    GoogleCuentaAndroid.token(contexto, cliente, nonce)
                }
            }
        }
    )
    desafioDemo?.let { desafio ->
        SelectorGoogleDemo(
            elegirFicticia = {
                desafioDemo = null
                modelo.ingresarGoogleDemo(desafio, "ejemplo")
            },
            elegirDispositivo = {
                try {
                    elegirCuenta.launch(GoogleCuentaAndroid.selectorDemo())
                } catch (_: ActivityNotFoundException) {
                    desafioDemo = null
                    modelo.avisar("Android no tiene un selector disponible. Vuelve a Google y usa la cuenta ficticia.")
                }
            },
            cerrar = { desafioDemo = null }
        )
    }
}

@Composable
private fun SelectorGoogleDemo(elegirFicticia: () -> Unit, elegirDispositivo: () -> Unit, cerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = cerrar,
        title = { Text("Google · modo demostración") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Espacio.s)) {
                Text("Prueba el registro o el acceso con una cuenta ficticia o selecciona una cuenta guardada en Android.")
                Text("La demostración no autentica con Google ni guarda tu correo personal.", style = MaterialTheme.typography.bodySmall)
                Accion("Usar cuenta ficticia", elegirFicticia, Modifier.fillMaxWidth())
                Accion("Elegir cuenta del dispositivo", elegirDispositivo, Modifier.fillMaxWidth(), secundaria = true)
            }
        },
        confirmButton = { TextButton(onClick = cerrar) { Text("Cancelar") } }
    )
}

@Composable
private fun Acceso(
    estado: LoginUiState,
    nombre: (String) -> Unit,
    correo: (String) -> Unit,
    clave: (String) -> Unit,
    recordar: (Boolean) -> Unit,
    entrar: () -> Unit,
    cambiarModo: () -> Unit,
    google: () -> Unit
) {
    val compacto = rememberWindowSizeClass() == WindowSizeClass.COMPACT
    val formulario: @Composable (Modifier) -> Unit = { modifier ->
        FormularioLogin(estado, nombre, correo, clave, recordar, entrar, cambiarModo, google, modifier)
    }
    Box(Modifier.fillMaxSize().imePadding().background(Marfil), contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 1000.dp).verticalScroll(rememberScrollState()).padding(Espacio.xl)) {
            if (compacto) {
                Column(verticalArrangement = Arrangement.spacedBy(Espacio.l)) {
                    Marca(Modifier.fillMaxWidth(), compacto = true)
                    formulario(Modifier.fillMaxWidth())
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(Espacio.xxl), verticalAlignment = Alignment.CenterVertically) {
                    Marca(Modifier.weight(1f), compacto = false)
                    formulario(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun Marca(modifier: Modifier, compacto: Boolean) {
    Column(
        modifier.background(Pino, MaterialTheme.shapes.medium).padding(if (compacto) Espacio.l else Espacio.xl),
        verticalArrangement = Arrangement.spacedBy(Espacio.xs)
    ) {
        Text("InsertCode", color = Papel, style = MaterialTheme.typography.labelLarge)
        Text(
            "Ventas y distribución", color = Papel,
            style = if (compacto) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displaySmall
        )
    }
}

@Composable
private fun FormularioLogin(
    estado: LoginUiState,
    nombre: (String) -> Unit,
    correo: (String) -> Unit,
    clave: (String) -> Unit,
    recordar: (Boolean) -> Unit,
    entrar: () -> Unit,
    cambiarModo: () -> Unit,
    google: () -> Unit,
    modifier: Modifier
) {
    var visible by rememberSaveable(estado.registrando) { mutableStateOf(false) }
    val registro = estado.registrando
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Espacio.s)) {
        Text(
            if (registro) "Crea una cuenta de prueba" else "Acceso al sistema",
            Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineLarge
        )
        if (registro) {
            OutlinedTextField(
                estado.nombre, nombre, Modifier.fillMaxWidth(), enabled = !estado.isLoading,
                label = { Text("Nombre ficticio") }, singleLine = true,
                isError = estado.errores.nombre != null,
                supportingText = estado.errores.nombre?.let { error -> { Text(error) } }
            )
        }
        OutlinedTextField(
            estado.email, correo, Modifier.fillMaxWidth(), enabled = !estado.isLoading,
            label = { Text(if (registro) "Correo @insertcode.test" else "Correo electrónico") }, singleLine = true,
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
        if (!registro) Opcion("Recordar correo", estado.recordarCorreo, !estado.isLoading, recordar)
        estado.errores.general?.let {
            Text(it, Modifier.semantics { liveRegion = LiveRegionMode.Polite }, color = MaterialTheme.colorScheme.error)
        }
        Accion(if (registro) "Registrar cuenta" else "Ingresar", entrar, Modifier.fillMaxWidth())
        Accion(
            if (registro) "Registrarse con Google" else "Ingresar con Google",
            google, Modifier.fillMaxWidth(), habilitada = !estado.isLoading, secundaria = true
        )
        TextButton(onClick = cambiarModo, enabled = !estado.isLoading) {
            Text(if (registro) "Volver al ingreso" else "Crear cuenta")
        }
        if (estado.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
    }
}

@Preview(widthDp = 380, heightDp = 760)
@Preview(widthDp = 900, heightDp = 580)
@Composable
private fun VistaAcceso() {
    Vicente_parrraTheme { Acceso(LoginUiState(), {}, {}, {}, {}, {}, {}, {}) }
}
