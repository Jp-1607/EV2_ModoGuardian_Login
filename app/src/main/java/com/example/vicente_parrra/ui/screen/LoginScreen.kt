package com.example.vicente_parrra.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vicente_parrra.R
import com.example.vicente_parrra.model.LoginUiState
import com.example.vicente_parrra.ui.utils.WindowSizeClass
import com.example.vicente_parrra.ui.utils.rememberWindowSizeClass
import com.example.vicente_parrra.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginExitoso: (String) -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val tamaño = rememberWindowSizeClass()

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (tamaño) {
                WindowSizeClass.COMPACT -> LoginCompact(
                    estado = estado,
                    onEmailChange = viewModel::onEmailChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onLoginClick = { viewModel.iniciarSesion(onLoginExitoso) }
                )

                WindowSizeClass.MEDIUM,
                WindowSizeClass.EXPANDED -> LoginGrande(
                    estado = estado,
                    onEmailChange = viewModel::onEmailChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onLoginClick = { viewModel.iniciarSesion(onLoginExitoso) }
                )
            }
        }
    }
}

@Composable
fun LoginCompact(
    estado: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.logo_guardian),
            contentDescription = "Logo",
            modifier = Modifier.size(90.dp)
        )

        Text(
            text = "Modo Guardián",
            style = MaterialTheme.typography.headlineSmall
        )

        FormularioLogin(
            estado = estado,
            onEmailChange = onEmailChange,
            onPasswordChange = onPasswordChange,
            onLoginClick = onLoginClick
        )
    }
}

@Composable
fun LoginGrande(
    estado: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(0.85f),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_guardian),
                    contentDescription = "Logo",
                    modifier = Modifier.size(120.dp)
                )

                Text(
                    text = "Modo Guardián",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                FormularioLogin(
                    estado = estado,
                    onEmailChange = onEmailChange,
                    onPasswordChange = onPasswordChange,
                    onLoginClick = onLoginClick
                )
            }
        }
    }
}

@Composable
fun FormularioLogin(
    estado: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    OutlinedTextField(
        value = estado.email,
        onValueChange = onEmailChange,
        label = { Text("Correo") },
        isError = estado.errores.email != null,
        supportingText = {
            estado.errores.email?.let {
                Text(it)
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
        value = estado.password,
        onValueChange = onPasswordChange,
        label = { Text("Contraseña") },
        visualTransformation = PasswordVisualTransformation(),
        isError = estado.errores.password != null,
        supportingText = {
            estado.errores.password?.let {
                Text(it)
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    estado.errores.general?.let {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Button(
        onClick = onLoginClick,
        enabled = estado.isLoginEnabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (estado.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text("Iniciar sesión")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Usuarios de prueba",
        style = MaterialTheme.typography.titleSmall
    )
    Text("admin@guardian.test")
    Text("supervisor@guardian.test")
    Text("operador@guardian.test")
    Text("Clave: 123456")
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun PreviewCompact() {
    MaterialTheme {
        LoginCompact(
            estado = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 700, heightDp = 500)
@Composable
fun PreviewMedium() {
    MaterialTheme {
        LoginGrande(
            estado = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 1000, heightDp = 600)
@Composable
fun PreviewExpanded() {
    MaterialTheme {
        LoginGrande(
            estado = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {}
        )
    }
}

