package com.example.modoguardian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.modoguardian.model.Role
import com.example.modoguardian.navigation.NavigationEvent
import com.example.modoguardian.navigation.Screen
import com.example.modoguardian.ui.screens.*
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

                val backStackEntry by
                    navController.currentBackStackEntryAsState()

                val rutaActual =
                    backStackEntry?.destination?.route

                LaunchedEffect(currentUser, rutaActual) {
                    if (
                        rutaActual != null &&
                        rutaActual != Screen.Login.route &&
                        currentUser == null
                    ) {
                        navController.navigate(
                            Screen.Login.route
                        ) {
                            popUpTo(navController.graph.id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                }

                LaunchedEffect(loginViewModel) {
                    loginViewModel.navigationEvent.collect { event ->
                        when (event) {
                            is NavigationEvent.NavigateTo -> {
                                val sesionIniciada =
                                    authViewModel.iniciarSesionDemo(
                                        event.route
                                    )

                                if (sesionIniciada) {
                                    navController.navigate(
                                        event.route
                                    ) {
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

                    // LOGIN
                    composable(Screen.Login.route) {
                        LoginScreen(
                            viewModel = loginViewModel
                        )
                    }

                    // INICIO
                    val homeRoutes = listOf(
                        Screen.HomeAdmin.route,
                        Screen.HomeSupervisor.route,
                        Screen.HomeOperador.route
                    )

                    homeRoutes.forEach { route ->
                        composable(route) {
                            HomeScreen(
                                user = currentUser,
                                eventsState = eventsState,

                                onOpenEvents = {
                                    navController.navigate(
                                        Screen.Events.route
                                    )
                                },

                                onOpenProfile = {
                                    navController.navigate(
                                        Screen.Perfil.route
                                    )
                                },

                                onOpenCameras = {
                                    navController.navigate("cameras")
                                },

                                onOpenAlerts = {
                                    navController.navigate("alerts")
                                },

                                onOpenEvent = { eventId ->
                                    eventsViewModel
                                        .seleccionarEventoPorId(eventId)

                                    navController.navigate(
                                        Screen.EventDetail
                                            .createRoute(eventId)
                                    )
                                }
                            )
                        }
                    }

                    // CÁMARAS
                    composable("cameras") {
                        CamerasScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onCameraClick = { cameraId ->
                                navController.navigate(
                                    "camera/$cameraId"
                                )
                            }
                        )
                    }

                    // DETALLE DE CÁMARA
                    composable("camera/{cameraId}") { entry ->
                        val cameraId =
                            entry.arguments
                                ?.getString("cameraId")
                                .orEmpty()

                        CameraDetailScreen(
                            cameraId = cameraId,
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // ALERTAS
                    composable("alerts") {
                        AlertsScreen(
                            eventsState = eventsState,

                            onBack = {
                                navController.popBackStack()
                            },

                            onStatusChange = { eventId, status ->
                                eventsViewModel.cambiarEstadoEvento(
                                    eventId,
                                    status
                                )
                            }
                        )
                    }

                    // HISTORIAL DE EVENTOS
                    composable(Screen.Events.route) {
                        EventsScreen(
                            currentUser = currentUser,
                            eventsState = eventsState,

                            onEventClick = { eventId ->
                                eventsViewModel
                                    .seleccionarEventoPorId(eventId)

                                navController.navigate(
                                    Screen.EventDetail
                                        .createRoute(eventId)
                                )
                            },

                            onNavigateToProfile = {
                                navController.navigate(
                                    Screen.Perfil.route
                                )
                            },

                            onNavigateToAdmin = {
                                if (
                                    currentUser?.rol ==
                                    Role.ADMINISTRADOR
                                ) {
                                    navController.navigate(
                                        Screen.Admin.route
                                    )
                                }
                            }
                        )
                    }

                    // DETALLE DE EVENTO
                    composable(Screen.EventDetail.route) { entry ->
                        val eventId =
                            entry.arguments
                                ?.getString("eventId")
                                .orEmpty()

                        val selectedEvent = when (val state = eventsState) {
                            is com.example.modoguardian.ui.state
                                .UiState.Success -> {
                                state.data.find {
                                    it.id == eventId
                                }
                            }
                            else -> null
                        }

                        EventDetailScreen(
                            event = selectedEvent,

                            onBack = {
                                navController.popBackStack()
                            },

                            onStatusChange = { status, photoUri ->
                                eventsViewModel.cambiarEstadoEvento(
                                    eventId,
                                    status,
                                    photoUri
                                )
                            }
                        )
                    }

                    // PERFIL
                    composable(Screen.Perfil.route) {
                        ProfileScreen(
                            user = currentUser,

                            onBack = {
                                navController.popBackStack()
                            },

                            onPhotoUpdated = { uri ->
                                authViewModel
                                    .actualizarFotoPerfil(uri)
                            },

                            onLogout = {
                                loginViewModel.cerrarSesion()
                                authViewModel.cerrarSesion()

                                navController.navigate(
                                    Screen.Login.route
                                ) {
                                    popUpTo(navController.graph.id) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // ADMINISTRACIÓN TEMPORAL
                    composable(Screen.Admin.route) {
                        if (
                            currentUser?.rol ==
                            Role.ADMINISTRADOR
                        ) {
                            EventsScreen(
                                currentUser = currentUser,
                                eventsState = eventsState,

                                onEventClick = { eventId ->
                                    eventsViewModel
                                        .seleccionarEventoPorId(eventId)

                                    navController.navigate(
                                        Screen.EventDetail
                                            .createRoute(eventId)
                                    )
                                },

                                onNavigateToProfile = {
                                    navController.navigate(
                                        Screen.Perfil.route
                                    )
                                },

                                onNavigateToAdmin = {}
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }
}
