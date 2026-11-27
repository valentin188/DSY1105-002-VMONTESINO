package com.example.modoguardian.model

/**
 * Modelo para gestionar las validaciones y mensajes de error del formulario de login.
 */
data class LoginErrores(
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null
)