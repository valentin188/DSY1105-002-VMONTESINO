package com.example.modoguardian.navigation

/**
 * Rutas de navegación del proyecto Modo Guardián.
 */
sealed class Screen(val route: String) {
    // Rutas obligatorias para el login por roles
    data object HomeAdmin : Screen("home_admin")
    data object HomeSupervisor : Screen("home_supervisor")
    data object HomeOperador : Screen("home_operador")

    // Tus rutas previas
    data object Login : Screen("login")
    data object Events : Screen("events")
    data object Admin : Screen("admin")
    data object Perfil : Screen("perfil")
    data object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
}