package com.example.vicente_parrra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vicente_parrra.navigation.NavigationEvent
import com.example.vicente_parrra.navigation.NavigationViewModel
import com.example.vicente_parrra.navigation.Screen
import com.example.vicente_parrra.ui.screen.HomeAdminScreen
import com.example.vicente_parrra.ui.screen.HomeOperadorScreen
import com.example.vicente_parrra.ui.screen.HomeSupervisorScreen
import com.example.vicente_parrra.ui.screen.LoginScreen
import com.example.vicente_parrra.ui.theme.Vicente_parrraTheme
import com.example.vicente_parrra.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Vicente_parrraTheme {
                val navController = rememberNavController()
                val navigationViewModel: NavigationViewModel = viewModel()
                val loginViewModel: LoginViewModel = viewModel()

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

                NavHost(
                    navController = navController,
                    startDestination = Screen.Login.route
                ) {
                    composable(Screen.Login.route) {
                        LoginScreen(
                            viewModel = loginViewModel,
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
                }
            }
        }
    }
}
