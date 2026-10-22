package com.example.modoguardian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.modoguardian.navigation.NavigationEvent
import com.example.modoguardian.navigation.Screen
import com.example.modoguardian.ui.screens.LoginScreen
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ModoGuardianTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    // Cierra la sesión, borra las credenciales y retira la pantalla
                    // protegida del historial para impedir volver con el botón Atrás.
                    val cerrarSesion: (String) -> Unit = { rutaActual ->
                        loginViewModel.cerrarSesion()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(rutaActual) { inclusive = true }
                            launchSingleTop = true
                        }
                    }

                    LaunchedEffect(navController) {
                        loginViewModel.navigationEvent.collect { event ->
                            when (event) {
                                is NavigationEvent.NavigateTo -> {
                                    navController.navigate(event.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                                is NavigationEvent.NavigateBack,
                                NavigationEvent.PopBackStack -> navController.popBackStack()
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route
                    ) {
                        composable(Screen.Login.route) {
                            LoginScreen(viewModel = loginViewModel)
                        }

                        composable(Screen.HomeAdmin.route) {
                            InicioPorRol("Bienvenido Administrador") {
                                cerrarSesion(Screen.HomeAdmin.route)
                            }
                        }

                        composable(Screen.HomeSupervisor.route) {
                            InicioPorRol("Bienvenido Supervisor") {
                                cerrarSesion(Screen.HomeSupervisor.route)
                            }
                        }

                        composable(Screen.HomeOperador.route) {
                            InicioPorRol("Bienvenido Operador") {
                                cerrarSesion(Screen.HomeOperador.route)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InicioPorRol(
    titulo: String,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onCerrarSesion) {
            Text("Cerrar sesión")
        }
    }
}
