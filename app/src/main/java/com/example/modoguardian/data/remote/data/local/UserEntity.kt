package com.example.modoguardian.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "usuarios",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["googleId"], unique = true)
    ]
)
data class UserEntity(

    @PrimaryKey
    val id: String,

    val nombre: String,

    val apellido: String,

    val domicilio: String,

    val telefono: String,

    val email: String,

    val passwordHash: String? = null,

    val passwordSalt: String? = null,

    val rol: String = "OPERADOR",

    val proveedor: String = "LOCAL",

    val googleId: String? = null,

    val fotoPerfilUri: String? = null
)
