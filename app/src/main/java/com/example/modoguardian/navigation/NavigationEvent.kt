package com.example.modoguardian.navigation

/**
 * Eventos reactivos de navegación.
 */
sealed interface NavigationEvent {
    data class NavigateTo(val route: String) : NavigationEvent
    object PopBackStack : NavigationEvent
    object NavigateBack : NavigationEvent
}