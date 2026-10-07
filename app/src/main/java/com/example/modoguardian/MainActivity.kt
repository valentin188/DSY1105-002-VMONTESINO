package com.example.modoguardian

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
                    val context = LocalContext.current

                    LaunchedEffect(Unit) {
                        loginViewModel.navigationEvent.collect { event ->
                            when (event) {
                                is NavigationEvent.NavigateTo -> {
                                    navController.navigate(event.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                                is NavigationEvent.NavigateBack, NavigationEvent.PopBackStack -> {
                                    navController.popBackStack()
                                }
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

                        // Pantallas para cada rol con contenido centrado
                        composable(Screen.HomeAdmin.route) {
                            Toast.makeText(context, "Bienvenido Administrador", Toast.LENGTH_SHORT).show()
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Bienvenido Administrador",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }

                        composable(Screen.HomeSupervisor.route) {
                            Toast.makeText(context, "Bienvenido Supervisor", Toast.LENGTH_SHORT).show()
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Pantalla Principal Supervisor",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }

                        composable(Screen.HomeOperador.route) {
                            Toast.makeText(context, "Bienvenido Operador", Toast.LENGTH_SHORT).show()
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Pantalla Principal Operador",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}