package com.example.modoguardian.data

import android.content.Context
import com.example.modoguardian.data.local.AppDatabase
import com.example.modoguardian.data.local.toEntity
import com.example.modoguardian.data.local.toEvent
import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.Event
import com.example.modoguardian.model.Prioridad
import com.example.modoguardian.model.Role
import com.example.modoguardian.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GuardianRepository {

    companion object {

        @Volatile
        private var database: AppDatabase? = null

        fun inicializar(context: Context) {
            database = AppDatabase.getDatabase(
                context.applicationContext
            )
        }

        private fun obtenerDatabase(): AppDatabase {
            return requireNotNull(database) {
                "GuardianRepository no fue inicializado"
            }
        }
    }

    private val usuariosPrueba = listOf(
        User(
            "1",
            "Carlos Pérez (Admin)",
            "admin@guardián.cl",
            Role.ADMINISTRADOR
        ),
        User(
            "2",
            "María González (Sup)",
            "supervisor@guardián.cl",
            Role.SUPERVISOR
        ),
        User(
            "3",
            "Juan Silva (Operador)",
            "operador@guardián.cl",
            Role.OPERADOR
        )
    )

    private val eventosIniciales = listOf(
        Event(
            id = "EVT-001",
            titulo = "Detección de Movimiento Sospechoso",
            descripcion =
                "Sensor del sector Norte activado " +
                "fuera del horario hábil.",
            ubicacion = "Perímetro Norte - Bodega 2",
            prioridad = Prioridad.CRITICA,
            estado = EstadoEvento.PENDIENTE,
            fechaHora = "2026-10-01 21:15"
        ),
        Event(
            id = "EVT-002",
            titulo = "Cámara Desconectada",
            descripcion =
                "Pérdida de señal de video " +
                "en el acceso principal.",
            ubicacion = "Acceso Principal - Portón 1",
            prioridad = Prioridad.ALTA,
            estado = EstadoEvento.EN_PROCESO,
            fechaHora = "2026-10-01 20:40"
        ),
        Event(
            id = "EVT-003",
            titulo = "Puerta de Emergencia Abierta",
            descripcion =
                "Apertura no autorizada " +
                "en pasillo central.",
            ubicacion = "Edificio A - Piso 2",
            prioridad = Prioridad.MEDIA,
            estado = EstadoEvento.RESUELTO,
            fechaHora = "2026-10-01 18:30"
        )
    )

    suspend fun obtenerUsuarios(): List<User> {
        return usuariosPrueba
    }

    suspend fun inicializarEventos() {
        val dao = obtenerDatabase().eventDao()

        if (dao.contarEventos() == 0) {
            dao.insertarEventos(
                eventosIniciales.map {
                    it.toEntity()
                }
            )
        }
    }

    suspend fun obtenerEventos(): List<Event> {
        inicializarEventos()

        return obtenerDatabase()
            .eventDao()
            .obtenerEventos()
            .map { it.toEvent() }
    }

    fun observarEventos(): Flow<List<Event>> {
        return obtenerDatabase()
            .eventDao()
            .observarEventos()
            .map { entidades ->
                entidades.map { it.toEvent() }
            }
    }

    suspend fun actualizarEstadoEvento(
        eventoId: String,
        nuevoEstado: EstadoEvento,
        fotoUri: String? = null
    ): Boolean {

        val filasActualizadas = obtenerDatabase()
            .eventDao()
            .actualizarEstado(
                eventoId = eventoId,
                nuevoEstado = nuevoEstado.name,
                fotoUri = fotoUri
            )

        return filasActualizadas > 0
    }
}
