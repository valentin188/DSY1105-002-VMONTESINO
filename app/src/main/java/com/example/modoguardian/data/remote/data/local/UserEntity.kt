package com.example.modoguardian.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val correo: String,
    val direccion: String,
    val modoEspecial: Boolean,
    val fotoPath: String?
)
