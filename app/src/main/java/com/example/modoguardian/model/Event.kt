package com.example.modoguardian.model

//PRIORIDAD DEL EVENTO
enum class Prioridad {
    CRITICA,
    ALTA,
    MEDIA,
    BAJA
}

//ESTADO DEL EVENTO
enum class EstadoEvento {
    PENDIENTE,
    EN_PROCESO,
    RESUELTO
}

//MOTIVO DE CIERRE
enum class MotivoCierre {
    INCIDENTE_SOLUCIONADO,
    FALSA_ALARMA
}

//MODELO PRINCIPAL DE EVENTOS
data class Event(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val ubicacion: String,
    val prioridad: Prioridad,
    val estado: EstadoEvento,
    val fechaHora: String,
    val evidenciaFotoUri: String? = null,
    val motivoCierre: MotivoCierre? = null
)
