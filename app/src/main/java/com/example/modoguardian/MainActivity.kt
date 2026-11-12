package com.example.modoguardian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.modoguardian.navigation.NavigationEvent
import com.example.modoguardian.navigation.Screen
import com.example.modoguardian.ui.screens.EventsScreen
import com.example.modoguardian.ui.screens.LoginScreen
import com.example.modoguardian.ui.screens.ProfileScreen
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.viewmodel.AuthViewModel
import com.example.modoguardian.viewmodel.EventsViewModel
import com.example.modoguardian.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val eventsViewModel: EventsViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ModoGuardianTheme {
                val navController = rememberNavController()
                val currentUser by
                    authViewModel.currentUser.collectAsState()
                val eventsState by
                    eventsViewModel.eventsState.collectAsState()
                LaunchedEffect(loginViewModel) {
                    loginViewModel.navigationEvent.collect { event ->
                        when (event) {
                            is NavigationEvent.NavigateTo -> {
                                val sesionIniciada =
                                    authViewModel.iniciarSesionDemo(event.route)
                                if (sesionIniciada) {
                                    navController.navigate(event.route) {
                                        popUpTo(Screen.Login.route) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            }

                            NavigationEvent.PopBackStack,
                            NavigationEvent.NavigateBack -> {
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
                        LoginScreen(
                            viewModel = loginViewModel
                        )
                    }

                    val homeRoutes = listOf(
                        Screen.HomeAdmin.route,
                        Screen.HomeSupervisor.route,
                        Screen.HomeOperador.route
                    )

                    homeRoutes.forEach { route ->
                        composable(route) {

                            EventsScreen(
                                currentUser = currentUser,
                                eventsState = eventsState,

                                onEventClick = { eventId ->
                                    eventsViewModel
                                        .seleccionarEventoPorId(eventId)
                                },

                                onNavigateToProfile = {
                                    navController.navigate(
                                        Screen.Perfil.route
                                    )
                                },

                                onNavigateToAdmin = {
                                    navController.navigate(
                                        Screen.Admin.route
                                    )
                                }
                            )
                        }
                    }

                    composable(Screen.Perfil.route) {
                        ProfileScreen(
                            user = currentUser,

                            onBack = {
                                navController.popBackStack()
                            },

                            onPhotoUpdated = { uri ->
                                authViewModel.actualizarFotoPerfil(uri)
                            },

                            onLogout = {
                                loginViewModel.cerrarSesion()
                                authViewModel.cerrarSesion()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(navController.graph.id) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }

                    composable(Screen.Events.route) {
                        EventsScreen(
                            currentUser = currentUser,
                            eventsState = eventsState,

                            onEventClick = { eventId ->
                                eventsViewModel
                                    .seleccionarEventoPorId(eventId)
                            },

                            onNavigateToProfile = {
                                navController.navigate(
                                    Screen.Perfil.route
                                )
                            },

                            onNavigateToAdmin = {
                                navController.navigate(
                                    Screen.Admin.route
                                )
                            }
                        )
                    }

                    composable(Screen.Admin.route) {
                        EventsScreen(
                            currentUser = currentUser,
                            eventsState = eventsState,

                            onEventClick = { eventId ->
                                eventsViewModel
                                    .seleccionarEventoPorId(eventId)
                            },

                            onNavigateToProfile = {
                                navController.navigate(
                                    Screen.Perfil.route
                                )
                            },

                            onNavigateToAdmin = {}
                        )
                    }
                }
            }
        }
    }
}
