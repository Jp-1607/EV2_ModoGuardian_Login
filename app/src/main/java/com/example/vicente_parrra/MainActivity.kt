package com.example.vicente_parrra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vicente_parrra.navigation.NavigationEvent
import com.example.vicente_parrra.navigation.NavigationViewModel
import com.example.vicente_parrra.navigation.Screen
import com.example.vicente_parrra.ui.screen.HomeAdminScreen
import com.example.vicente_parrra.ui.screen.HomeOperadorScreen
import com.example.vicente_parrra.ui.screen.HomeSupervisorScreen
import com.example.vicente_parrra.ui.screen.LoginScreen
import com.example.vicente_parrra.ui.screen.ModoScreen
import com.example.vicente_parrra.ui.screen.PerfilScreen
import com.example.vicente_parrra.ui.screen.PostScreen
import com.example.vicente_parrra.ui.screen.RegistroScreen
import com.example.vicente_parrra.ui.screen.ResumenScreen
import com.example.vicente_parrra.ui.theme.Vicente_parrraTheme
import com.example.vicente_parrra.viewmodel.LoginViewModel
import com.example.vicente_parrra.viewmodel.UsuarioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Vicente_parrraTheme {
                val navController = rememberNavController()
                val navigationViewModel: NavigationViewModel = viewModel()
                val loginViewModel: LoginViewModel = viewModel()
                val registro: UsuarioViewModel = viewModel()
                val entrada by navController.currentBackStackEntryAsState()
                val ruta = entrada?.destination?.route

                LaunchedEffect(Unit) {
                    navigationViewModel.navigationEvent.collect { evento ->
                        when (evento) {
                            is NavigationEvent.NavigateTo -> {
                                navController.navigate(evento.screen.route)
                            }

                            NavigationEvent.NavigateBack -> navController.popBackStack()
                        }
                    }
                }

                Scaffold(bottomBar = {
                    if (ruta in listOf(Screen.HomeAdmin.route, Screen.HomeSupervisor.route, Screen.HomeOperador.route)) {
                        BottomAppBar {
                            TextButton(onClick = { navController.navigate("perfil") }) { Text("Perfil") }
                            TextButton(onClick = { navController.navigate("modo") }) { Text("Modo") }
                            TextButton(onClick = { navController.navigate("posts") }) { Text("Publicaciones") }
                        }
                    }
                }) { padding ->
                    NavHost(
                        navController = navController,
                        modifier = Modifier.padding(padding),
                        startDestination = Screen.Login.route
                    ) {
                        composable(Screen.Login.route) {
                            LoginScreen(
                                viewModel = loginViewModel,
                                onRegistro = { navController.navigate("registro") },
                                onLoginExitoso = { rol ->
                                    when (rol) {
                                        "Admin" -> navigationViewModel.navigateTo(Screen.HomeAdmin)
                                        "Supervisor" -> navigationViewModel.navigateTo(Screen.HomeSupervisor)
                                        "Operador" -> navigationViewModel.navigateTo(Screen.HomeOperador)
                                    }
                                }
                            )
                        }

                        composable(Screen.HomeAdmin.route) {
                            HomeAdminScreen(
                                loginViewModel = loginViewModel,
                                onCerrarSesion = {
                                    navigationViewModel.navigateBack()
                                }
                            )
                        }

                        composable(Screen.HomeSupervisor.route) {
                            HomeSupervisorScreen(
                                loginViewModel = loginViewModel,
                                onCerrarSesion = {
                                    navigationViewModel.navigateBack()
                                }
                            )
                        }

                        composable(Screen.HomeOperador.route) {
                            HomeOperadorScreen(
                                loginViewModel = loginViewModel,
                                onCerrarSesion = {
                                    navigationViewModel.navigateBack()
                                }
                            )
                        }
                        composable("registro") { RegistroScreen(registro) { navController.navigate("resumen") } }
                        composable("resumen") { ResumenScreen(registro) { navController.popBackStack() } }
                        composable("perfil") { PerfilScreen(viewModel()) }
                        composable("modo") { ModoScreen(viewModel()) }
                        composable("posts") { PostScreen(viewModel()) }
                    }
                }
            }
        }
    }
}

