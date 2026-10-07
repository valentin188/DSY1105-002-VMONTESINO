package com.example.modoguardian.model

/**
 * Estado general de la pantalla de Login para la arquitectura MVVM.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoginEnabled: Boolean = false,
    val errores: LoginErrores = LoginErrores(),
    val usuarioLogueado: String? = null,
    val rolLogueado: String? = null
)