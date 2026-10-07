package com.example.modoguardian.data

import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.Event
import com.example.modoguardian.model.Prioridad
import com.example.modoguardian.model.Role
import com.example.modoguardian.model.User
import kotlinx.coroutines.delay

class GuardianRepository {

    // Usuarios de prueba con distintos roles
    private val usuariosPrueba = listOf(
        User("1", "Carlos Pérez (Admin)", "admin@guardián.cl", Role.ADMINISTRADOR),
        User("2", "María González (Sup)", "supervisor@guardián.cl", Role.SUPERVISOR),
        User("3", "Juan Silva (Operador)", "operador@guardián.cl", Role.OPERADOR)
    )

    // Lista mutable de eventos en memoria
    private val listaEventos = mutableListOf(
        Event(
            id = "EVT-001",
            titulo = "Detección de Movimiento Sospechoso",
            descripcion = "Sensor del sector Norte activado fuera del horario hábil.",
            ubicacion = "Perímetro Norte - Bodega 2",
            prioridad = Prioridad.CRITICA,
            estado = EstadoEvento.PENDIENTE,
            fechaHora = "2026-10-01 21:15"
        ),
        Event(
            id = "EVT-002",
            titulo = "Cámara Desconectada",
            descripcion = "Pérdida de señal de video en el acceso principal.",
            ubicacion = "Acceso Principal - Portón 1",
            prioridad = Prioridad.ALTA,
            estado = EstadoEvento.EN_PROCESO,
            fechaHora = "2026-10-01 20:40"
        ),
        Event(
            id = "EVT-003",
            titulo = "Puerta de Emergencia Abierta",
            descripcion = "Apertura no autorizada en pasillo central.",
            ubicacion = "Edificio A - Piso 2",
            prioridad = Prioridad.MEDIA,
            estado = EstadoEvento.RESUELTO,
            fechaHora = "2026-10-01 18:30"
        )
    )

    // Simula una petición asíncrona de red con delay (Corrutinas)
    suspend fun obtenerEventos(): List<Event> {
        delay(1000) // Simula 1 segundo de latencia de red
        return listaEventos.toList()
    }

    suspend fun obtenerUsuarios(): List<User> {
        delay(500)
        return usuariosPrueba
    }

    // Actualiza el estado de un evento y le adjunta una evidencia fotográfica
    suspend fun actualizarEstadoEvento(
        eventoId: String,
        nuevoEstado: EstadoEvento,
        fotoUri: String? = null
    ): Boolean {
        delay(800)
        val indice = listaEventos.indexOfFirst { it.id == eventoId }
        if (indice != -1) {
            val eventoExistente = listaEventos[indice]
            listaEventos[indice] = eventoExistente.copy(
                estado = nuevoEstado,
                evidenciaFotoUri = fotoUri ?: eventoExistente.evidenciaFotoUri
            )
            return true
        }
        return false
    }
}