package com.example.vicente_parrra

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vicente_parrra.model.Sesion
import com.example.vicente_parrra.navigation.Screen
import com.example.vicente_parrra.ui.components.Accion
import com.example.vicente_parrra.ui.components.Mensajes
import com.example.vicente_parrra.ui.screen.CatalogoScreen
import com.example.vicente_parrra.ui.screen.ClientesScreen
import com.example.vicente_parrra.ui.screen.EntregasScreen
import com.example.vicente_parrra.ui.screen.HomeScreen
import com.example.vicente_parrra.ui.screen.LoginScreen
import com.example.vicente_parrra.ui.screen.NuevoPedidoScreen
import com.example.vicente_parrra.ui.screen.PedidosScreen
import com.example.vicente_parrra.ui.screen.PerfilScreen
import com.example.vicente_parrra.ui.screen.RutasScreen
import com.example.vicente_parrra.ui.screen.UsuariosScreen
import com.example.vicente_parrra.ui.screen.VentasScreen
import com.example.vicente_parrra.ui.theme.Espacio
import com.example.vicente_parrra.ui.theme.Papel
import com.example.vicente_parrra.ui.theme.Pino
import com.example.vicente_parrra.ui.theme.Salvia
import com.example.vicente_parrra.ui.theme.Vicente_parrraTheme
import com.example.vicente_parrra.ui.utils.WindowSizeClass
import com.example.vicente_parrra.ui.utils.rememberWindowSizeClass
import com.example.vicente_parrra.viewmodel.LoginViewModel
import com.example.vicente_parrra.viewmodel.OperacionUiState
import com.example.vicente_parrra.viewmodel.OperacionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(TRANSPARENT, TRANSPARENT))
        setContent { Vicente_parrraTheme { InsertCode() } }
    }
}

@Composable
private fun InsertCode(login: LoginViewModel = viewModel(), modelo: OperacionViewModel = viewModel()) {
    val acceso by login.estado.collectAsStateWithLifecycle()
    val operacion by modelo.estado.collectAsStateWithLifecycle()
    val sesion = acceso.sesion
    LaunchedEffect(sesion) { modelo.conectar(sesion) }
    LaunchedEffect(operacion.sesionVencida) {
        if (operacion.sesionVencida) login.cerrarSesion("Tu sesión caducó. Vuelve a ingresar.")
    }

    if (sesion == null) {
        Scaffold { margen -> Box(Modifier.padding(margen)) { LoginScreen(login) } }
    } else {
        key(sesion.usuario.id) {
            OperacionApp(sesion, operacion, modelo) {
                modelo.conectar(null)
                login.cerrarSesion()
            }
        }
    }
}

@Composable
private fun OperacionApp(
    sesion: Sesion,
    estado: OperacionUiState,
    modelo: OperacionViewModel,
    salir: () -> Unit
) {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val actual = entrada?.destination?.route
    val compacto = rememberWindowSizeClass() == WindowSizeClass.COMPACT
    val menu = Screen.menu(sesion.usuario)
    val elegir: (Screen) -> Unit = { pantalla ->
        nav.navigate(pantalla.route) {
            popUpTo(Screen.Inicio.route)
            launchSingleTop = true
        }
    }
    Scaffold(
        topBar = {
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Espacio.l, vertical = Espacio.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("InsertCode", color = Pino, style = MaterialTheme.typography.titleLarge)
                    Text(sesion.usuario.rol.etiqueta, style = MaterialTheme.typography.labelMedium)
                }
                Accion("Actualizar", modelo::actualizar, secundaria = true, habilitada = !estado.cargando && !estado.guardando)
            }
        },
        bottomBar = { if (compacto) Barra(menu, actual, elegir) }
    ) { margen ->
        Row(Modifier.fillMaxSize().padding(margen).imePadding()) {
            if (!compacto) MenuLateral(menu, actual, elegir)
            Column(Modifier.weight(1f)) {
                Mensajes(estado.cargando || estado.guardando, estado.sinConexion, estado.actualizada,
                    estado.error, estado.aviso, modelo::actualizar)
                Destinos(nav, sesion, estado, modelo, salir, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Destinos(
    nav: NavHostController,
    sesion: Sesion,
    estadoActual: OperacionUiState,
    modelo: OperacionViewModel,
    salir: () -> Unit,
    modifier: Modifier
) {
    val usuario = sesion.usuario
    val estado by rememberUpdatedState(estadoActual)
    val nuevo: (String?) -> Unit = { producto ->
        modelo.limpiarFormulario()
        val ruta = if (producto == null) Screen.NuevoPedido.route else "${Screen.NuevoPedido.route}?producto=$producto"
        nav.navigate(ruta) { launchSingleTop = true }
    }

    NavHost(nav, startDestination = Screen.Inicio.route, modifier = modifier) {
        composable(Screen.Inicio.route) {
            HomeScreen(usuario, estado.operacion?.indicadores) {
                if (it == Screen.NuevoPedido) nuevo(null)
                else nav.navigate(it.route) { launchSingleTop = true }
            }
        }
        if (Screen.Pedidos.disponible(usuario)) composable(Screen.Pedidos.route) {
            PedidosScreen(estado.operacion?.pedidos, if (usuario.puede("crearPedido")) ({ nuevo(null) }) else null)
        }
        if (usuario.puede("comercial")) {
            composable(Screen.Catalogo.route) {
                CatalogoScreen(estado.operacion?.productos, if (usuario.puede("crearPedido")) ({ nuevo(it) }) else null)
            }
            composable(Screen.Clientes.route) { ClientesScreen(estado.operacion?.clientes) }
            composable(Screen.Ventas.route) { VentasScreen(estado.operacion?.ventas) }
        }
        if (usuario.puede("despacho")) {
            composable(Screen.Rutas.route) {
                RutasScreen(estado.operacion?.rutas, estado.operacion?.entregas.orEmpty()) {
                    nav.navigate(Screen.Entregas.route) { launchSingleTop = true }
                }
            }
            composable(Screen.Entregas.route) {
                val cambiar = if (usuario.puede("actualizarEntrega")) modelo::cambiarEntrega else null
                EntregasScreen(estado.operacion?.entregas, !estado.cargando && !estado.guardando && !estado.sinConexion, cambiar)
            }
        }
        if (usuario.puede("usuarios")) composable(Screen.Usuarios.route) {
            UsuariosScreen(estado, modelo::guardarUsuario, modelo::limpiarFormulario)
        }
        if (usuario.puede("crearPedido")) composable(
            "${Screen.NuevoPedido.route}?producto={producto}",
            arguments = listOf(androidx.navigation.navArgument("producto") {
                type = androidx.navigation.NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { entrada ->
            NuevoPedidoScreen(estado, entrada.arguments?.getString("producto"), modelo::crearPedido) {
                nav.navigate(Screen.Pedidos.route) {
                    popUpTo(Screen.Inicio.route)
                    launchSingleTop = true
                }
            }
        }
        composable(Screen.Perfil.route) { PerfilScreen(usuario, salir) }
    }
}

@Composable
private fun Barra(menu: List<Screen>, actual: String?, elegir: (Screen) -> Unit) {
    NavigationBar(containerColor = Pino) {
        menu.forEach { pantalla ->
            NavigationBarItem(
                selected = actual == pantalla.route,
                onClick = { elegir(pantalla) },
                icon = { Icon(icono(pantalla), contentDescription = null) },
                label = { Text(pantalla.titulo) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Pino, selectedTextColor = Papel, indicatorColor = Salvia,
                    unselectedIconColor = Salvia, unselectedTextColor = Papel
                )
            )
        }
    }
}

@Composable
private fun MenuLateral(menu: List<Screen>, actual: String?, elegir: (Screen) -> Unit) {
    NavigationRail(Modifier.fillMaxHeight().background(Pino), containerColor = Pino) {
        menu.forEach { pantalla ->
            NavigationRailItem(
                selected = actual == pantalla.route,
                onClick = { elegir(pantalla) },
                icon = { Icon(icono(pantalla), contentDescription = null) },
                label = { Text(pantalla.titulo) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = Pino, selectedTextColor = Papel, indicatorColor = Salvia,
                    unselectedIconColor = Salvia, unselectedTextColor = Papel
                )
            )
        }
    }
}

private fun icono(pantalla: Screen): ImageVector = when (pantalla) {
    Screen.Pedidos -> Icons.AutoMirrored.Filled.List
    Screen.Catalogo -> Icons.Filled.ShoppingCart
    Screen.Rutas -> Icons.Filled.LocationOn
    Screen.Perfil -> Icons.Filled.Person
    else -> Icons.Filled.Home
}
