package com.example.modoguardian.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

import com.example.modoguardian.model.Event
import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.MotivoCierre
import com.example.modoguardian.model.Prioridad

// TABLA DE EVENTOS DE INSERCODE
@Entity(tableName = "eventos")
data class EventEntity(
    @PrimaryKey
    val id: String,
    val titulo: String,
    val descripcion: String,
    val ubicacion: String,
    val prioridad: String,
    val estado: String,
    val fechaHora: String,
    val evidenciaFotoUri: String? = null,
    val motivoCierre: String? = null
    val evidenciaVideoUri: String? = null,
    val cameraId: String? = null,
    val reportadoPorId: String? = null,
    val reportadoPorNombre: String? = null
)

// CONVERTIR ENTIDAD ROOM A MODELO
fun EventEntity.toEvent(): Event {
    return Event(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        ubicacion = ubicacion,
        prioridad = Prioridad.valueOf(prioridad),
        estado = EstadoEvento.valueOf(estado),
        fechaHora = fechaHora,
        evidenciaFotoUri = evidenciaFotoUri,
        motivoCierre = motivoCierre?.let {
            MotivoCierre.valueOf(it)
            evidenciaVideoUri = evidenciaVideoUri,
            cameraId = cameraId,
            reportadoPorId = reportadoPorId,
            reportadoPorNombre = reportadoPorNombre
        }
    )
}

// CONVERTIR MODELO A ENTIDAD ROOM
fun Event.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        ubicacion = ubicacion,
        prioridad = prioridad.name,
        estado = estado.name,
        fechaHora = fechaHora,
        evidenciaFotoUri = evidenciaFotoUri,
        motivoCierre = motivoCierre?.name
        evidenciaVideoUri = evidenciaVideoUri,
        cameraId = cameraId,
        reportadoPorId = reportadoPorId,
        reportadoPorNombre = reportadoPorNombre
    )
}
