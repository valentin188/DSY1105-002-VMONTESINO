package com.example.modoguardian.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.modoguardian.model.Event
import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.Prioridad

@Entity(tableName = "eventos")
data class EventEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val descripcion: String,
    val ubicacion: String,
    val prioridad: String,
    val estado: String,
    val fechaHora: String,
    val evidenciaFotoUri: String? = null
)

fun EventEntity.toEvent(): Event {
    return Event(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        ubicacion = ubicacion,
        prioridad = Prioridad.valueOf(prioridad),
        estado = EstadoEvento.valueOf(estado),
        fechaHora = fechaHora,
        evidenciaFotoUri = evidenciaFotoUri
    )
}

fun Event.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        ubicacion = ubicacion,
        prioridad = prioridad.name,
        estado = estado.name,
        fechaHora = fechaHora,
        evidenciaFotoUri = evidenciaFotoUri
    )
}
