package com.example.modoguardian.model

data class User(
    val id: String,
    val nombre: String,
    val email: String,
    val rol: Role,
    val fotoPerfilUri: String? = null
)