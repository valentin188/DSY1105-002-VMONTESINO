package com.example.modoguardian.model

enum class Prioridad {
    CRITICA,
    ALTA,
    MEDIA,
    BAJA
}

enum class EstadoEvento {
    PENDIENTE,
    EN_PROCESO,
    RESUELTO
}

data class Event(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val ubicacion: String,
    val prioridad: Prioridad,
    val estado: EstadoEvento,
    val fechaHora: String,
    val evidenciaFotoUri: String? = null
)