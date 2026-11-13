package com.example.modoguardian

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.example.modoguardian.data.GuardianRepository
import com.example.modoguardian.model.Role

import com.example.modoguardian.navigation.NavigationEvent
import com.example.modoguardian.navigation.Screen

import com.example.modoguardian.ui.screens.*
import com.example.modoguardian.ui.state.UiState
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

        // Inicializar base de datos Room.
        GuardianRepository.inicializar(applicationContext)

        val preferences = getSharedPreferences(
            "insercode_profile_preferences",
            Context.MODE_PRIVATE
        )

        val profilePhotos = getSharedPreferences(
            "insercode_profile_photos",
            Context.MODE_PRIVATE
        )

        setContent {

            val currentUser by
                authViewModel.currentUser.collectAsState()

            val eventsState by
                eventsViewModel.eventsState.collectAsState()

            val navController = rememberNavController()

            val backStackEntry by
                navController.currentBackStackEntryAsState()

            val currentRoute =
                backStackEntry?.destination?.route

            val homeRoutes = listOf(
                Screen.HomeAdmin.route,
                Screen.HomeSupervisor.route,
                Screen.HomeOperador.route
            )

            val homeRoute = when (currentUser?.rol) {
                Role.ADMINISTRADOR ->
                    Screen.HomeAdmin.route

                Role.SUPERVISOR ->
                    Screen.HomeSupervisor.route

                Role.OPERADOR ->
                    Screen.HomeOperador.route

                else ->
                    Screen.Login.route
            }

            // =====================================
            // PREFERENCIA GLOBAL DE TEMA
            // =====================================

            var darkTheme by remember {
                mutableStateOf(true)
            }

            DisposableEffect(currentUser?.id) {

                val userId = currentUser?.id

                if (userId != null) {
                    val key = "${userId}_dark_mode"

                    darkTheme = preferences.getBoolean(
                        key,
                        true
                    )

                    val listener =
                        SharedPreferences
                            .OnSharedPreferenceChangeListener {
                                sharedPreferences,
                                changedKey ->

                                if (changedKey == key) {
                                    darkTheme =
                                        sharedPreferences
                                            .getBoolean(
                                                key,
                                                true
                                            )
                                }
                            }

                    preferences
                        .registerOnSharedPreferenceChangeListener(
                            listener
                        )

                    onDispose {
                        preferences
                            .unregisterOnSharedPreferenceChangeListener(
                                listener
                            )
                    }

                } else {
                    darkTheme = true
                    onDispose {}
                }
            }

            // =====================================
            // RECUPERAR FOTO DEL PERFIL
            // =====================================

            LaunchedEffect(currentUser?.id) {
                val userId = currentUser?.id

                if (userId != null) {
                    val savedPhoto = profilePhotos.getString(
                        "photo_$userId",
                        null
                    )

                    if (!savedPhoto.isNullOrBlank() &&
                        savedPhoto != currentUser?.fotoPerfilUri
                    ) {
                        authViewModel.actualizarFotoPerfil(
                            savedPhoto
                        )
                    }
                }
            }

            // =====================================
            // PROTECCIÓN BÁSICA DE NAVEGACIÓN
            // =====================================

            LaunchedEffect(currentUser, currentRoute) {

                if (
                    currentRoute != null &&
                    currentRoute != Screen.Login.route &&
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

            // =====================================
            // NAVEGACIÓN DESDE LOGIN
            // =====================================

            LaunchedEffect(loginViewModel) {

                loginViewModel.navigationEvent.collect {
                    event ->

                    when (event) {

                        is NavigationEvent.NavigateTo -> {

                            val loginSuccess =
                                authViewModel.iniciarSesionDemo(
                                    event.route
                                )

                            if (loginSuccess) {
                                navController.navigate(
                                    event.route
                                ) {
                                    popUpTo(
                                        Screen.Login.route
                                    ) {
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

            // =====================================
            // IDENTIDAD VISUAL
            // =====================================

            ModoGuardianTheme(
                darkTheme = darkTheme
            ) {

                val showBottomBar =
                    currentUser != null &&
                    (
                        currentRoute == "cameras" ||
                        currentRoute == "alerts" ||
                        currentRoute == Screen.Events.route ||
                        currentRoute == Screen.Perfil.route
                    )

                // HomeScreen ya dispone de su propia
                // barra inferior. Evitamos duplicarla.

                fun navigateToTab(route: String) {

                    if (
                        currentUser == null ||
                        route == Screen.Login.route
                    ) {
                        return
                    }

                    navController.navigate(route) {

                        popUpTo(homeRoute) {
                            saveState = true
                        }

                        launchSingleTop = true
                        restoreState = true
                    }
                }

                Scaffold(
                    bottomBar = {

                        if (showBottomBar) {

                            NavigationBar(
                                containerColor =
                                    MaterialTheme.colorScheme.surface
                            ) {

                                NavigationBarItem(
                                    selected =
                                        currentRoute in homeRoutes,
                                    onClick = {
                                        navigateToTab(homeRoute)
                                    },
                                    icon = {
                                        Icon(
                                            Icons.Default.Home,
                                            contentDescription = "Inicio"
                                        )
                                    },
                                    label = {
                                        Text("Inicio")
                                    }
                                )

                                NavigationBarItem(
                                    selected =
                                        currentRoute == "cameras",
                                    onClick = {
                                        navigateToTab("cameras")
                                    },
                                    icon = {
                                        Icon(
                                            Icons.Default.Videocam,
                                            contentDescription = "Cámaras"
                                        )
                                    },
                                    label = {
                                        Text("Cámaras")
                                    }
                                )

                                NavigationBarItem(
                                    selected =
                                        currentRoute ==
                                            Screen.Events.route,
                                    onClick = {
                                        navigateToTab(
                                            Screen.Events.route
                                        )
                                    },
                                    icon = {
                                        Icon(
                                            Icons.Default.Event,
                                            contentDescription = "Eventos"
                                        )
                                    },
                                    label = {
                                        Text("Eventos")
                                    }
                                )

                                NavigationBarItem(
                                    selected =
                                        currentRoute == "alerts",
                                    onClick = {
                                        navigateToTab("alerts")
                                    },
                                    icon = {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = "Alertas"
                                        )
                                    },
                                    label = {
                                        Text("Alertas")
                                    }
                                )

                                NavigationBarItem(
                                    selected =
                                        currentRoute ==
                                            Screen.Perfil.route,
                                    onClick = {
                                        navigateToTab(
                                            Screen.Perfil.route
                                        )
                                    },
                                    icon = {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = "Perfil"
                                        )
                                    },
                                    label = {
                                        Text("Perfil")
                                    }
                                )
                            }
                        }
                    }
                ) { scaffoldPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route,
                        modifier = Modifier.padding(
                            scaffoldPadding
                        )
                    ) {

                        // =========================
                        // LOGIN
                        // =========================

                        composable(
                            Screen.Login.route
                        ) {
                            LoginScreen(
                                viewModel = loginViewModel
                            )
                        }

                        // =========================
                        // INICIO POR ROL
                        // =========================

                        homeRoutes.forEach { route ->

                            composable(route) {

                                HomeScreen(
                                    user = currentUser,
                                    eventsState = eventsState,

                                    onOpenEvents = {
                                        navigateToTab(
                                            Screen.Events.route
                                        )
                                    },

                                    onOpenProfile = {
                                        navigateToTab(
                                            Screen.Perfil.route
                                        )
                                    },

                                    onOpenCameras = {
                                        navigateToTab("cameras")
                                    },

                                    onOpenAlerts = {
                                        navigateToTab("alerts")
                                    },

                                    onOpenEvent = { eventId ->

                                        eventsViewModel
                                            .seleccionarEventoPorId(
                                                eventId
                                            )

                                        navController.navigate(
                                            Screen.EventDetail
                                                .createRoute(
                                                    eventId
                                                )
                                        )
                                    }
                                )
                            }
                        }

                        // =========================
                        // CÁMARAS
                        // =========================

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

                        // =========================
                        // DETALLE DE CÁMARA
                        // =========================

                        composable(
                            "camera/{cameraId}"
                        ) { entry ->

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

                        // =========================
                        // ALERTAS
                        // =========================

                        composable("alerts") {

                            AlertsScreen(
                                eventsState = eventsState,

                                onBack = {
                                    navController.popBackStack()
                                },

                                onStatusChange = {
                                    eventId, status ->

                                    eventsViewModel
                                        .cambiarEstadoEvento(
                                            eventId,
                                            status
                                        )
                                }
                            )
                        }

                        // =========================
                        // HISTORIAL DE EVENTOS
                        // =========================

                        composable(
                            Screen.Events.route
                        ) {

                            EventsScreen(
                                currentUser = currentUser,
                                eventsState = eventsState,

                                onEventClick = { eventId ->

                                    eventsViewModel
                                        .seleccionarEventoPorId(
                                            eventId
                                        )

                                    navController.navigate(
                                        Screen.EventDetail
                                            .createRoute(
                                                eventId
                                            )
                                    )
                                },

                                onNavigateToProfile = {
                                    navigateToTab(
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

                        // =========================
                        // DETALLE DE EVENTO
                        // =========================

                        composable(
                            Screen.EventDetail.route
                        ) { entry ->

                            val eventId =
                                entry.arguments
                                    ?.getString("eventId")
                                    .orEmpty()

                            val selectedEvent =
                                when (
                                    val state = eventsState
                                ) {

                                    is UiState.Success ->
                                        state.data.find {
                                            it.id == eventId
                                        }

                                    else -> null
                                }

                            EventDetailScreen(
                                event = selectedEvent,

                                onBack = {
                                    navController.popBackStack()
                                },

                                onStatusChange = {
                                    status, photoUri ->

                                    eventsViewModel
                                        .cambiarEstadoEvento(
                                            eventId,
                                            status,
                                            photoUri
                                        )
                                }
                            )
                        }

                        // =========================
                        // PERFIL
                        // =========================

                        composable(
                            Screen.Perfil.route
                        ) {

                            ProfileScreen(
                                user = currentUser,

                                onBack = {
                                    navController.popBackStack()
                                },

                                onOpenCameras = {
                                    navigateToTab("cameras")
                                },

                                onPhotoUpdated = { uri ->

                                    val userId =
                                        currentUser?.id

                                    if (userId != null) {

                                        profilePhotos.edit()
                                            .putString(
                                                "photo_$userId",
                                                uri
                                            )
                                            .apply()
                                    }

                                    authViewModel
                                        .actualizarFotoPerfil(
                                            uri
                                        )
                                },

                                onLogout = {

                                    loginViewModel.cerrarSesion()
                                    authViewModel.cerrarSesion()

                                    navController.navigate(
                                        Screen.Login.route
                                    ) {
                                        popUpTo(
                                            navController.graph.id
                                        ) {
                                            inclusive = false
                                        }

                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        // =========================
                        // ADMINISTRACIÓN TEMPORAL
                        // =========================

                        composable(
                            Screen.Admin.route
                        ) {

                            if (
                                currentUser?.rol ==
                                Role.ADMINISTRADOR
                            ) {

                                EventsScreen(
                                    currentUser = currentUser,
                                    eventsState = eventsState,

                                    onEventClick = { eventId ->

                                        eventsViewModel
                                            .seleccionarEventoPorId(
                                                eventId
                                            )

                                        navController.navigate(
                                            Screen.EventDetail
                                                .createRoute(
                                                    eventId
                                                )
                                        )
                                    },

                                    onNavigateToProfile = {
                                        navigateToTab(
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
}
