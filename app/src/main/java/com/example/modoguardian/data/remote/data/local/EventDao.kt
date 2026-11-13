package com.example.modoguardian.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    // ======================================
    // OBSERVAR EVENTOS EN TIEMPO REAL LOCAL
    // ======================================
    @Query(
        "SELECT * FROM eventos ORDER BY fechaHora DESC"
    )
    fun observarEventos(): Flow<List<EventEntity>>

    // ======================================
    // OBTENER TODOS LOS EVENTOS
    // ======================================
    @Query(
        "SELECT * FROM eventos ORDER BY fechaHora DESC"
    )
    suspend fun obtenerEventos(): List<EventEntity>

    // ======================================
    // OBTENER UN EVENTO POR SU ID
    // ======================================
    @Query(
        "SELECT * FROM eventos WHERE id = :eventoId LIMIT 1"
    )
    suspend fun obtenerEvento(
        eventoId: String
    ): EventEntity?

    // ======================================
    // CONTAR EVENTOS REGISTRADOS
    // ======================================
    @Query(
        "SELECT COUNT(*) FROM eventos"
    )
    suspend fun contarEventos(): Int

    // ======================================
    // INSERTAR EVENTOS SIN DUPLICAR IDS
    // ======================================
    @Insert(
        onConflict = OnConflictStrategy.IGNORE
    )
    suspend fun insertarEventos(
        eventos: List<EventEntity>
    )

    // ======================================
    // ACTUALIZAR ESTADO DEL EVENTO
    // ======================================
    @Query(
        """
        UPDATE eventos
        SET estado = :nuevoEstado,
            evidenciaFotoUri = COALESCE(
                :fotoUri,
                evidenciaFotoUri
            ),
            motivoCierre = NULL
        WHERE id = :eventoId
        """
    )
    suspend fun actualizarEstado(
        eventoId: String,
        nuevoEstado: String,
        fotoUri: String?
    ): Int

    // ======================================
    // ACTUALIZAR ESTADO Y MOTIVO DE CIERRE
    // ======================================
    @Query(
        """
        UPDATE eventos
        SET estado = :nuevoEstado,
            motivoCierre = :motivoCierre,
            evidenciaFotoUri = COALESCE(
                :fotoUri,
                evidenciaFotoUri
            )
        WHERE id = :eventoId
        """
    )
    suspend fun actualizarEstadoConMotivo(
        eventoId: String,
        nuevoEstado: String,
        motivoCierre: String?,
        fotoUri: String?
    ): Int

    // ======================================
    // CONTAR FALSAS ALARMAS
    // ======================================
    @Query(
        """
        SELECT COUNT(*)
        FROM eventos
        WHERE motivoCierre = 'FALSA_ALARMA'
          AND estado = 'RESUELTO'
        """
    )
    suspend fun contarFalsasAlarmas(): Int

    // ======================================
    // CONSULTAR FALSAS ALARMAS
    // ======================================
    @Query(
        """
        SELECT *
        FROM eventos
        WHERE motivoCierre = 'FALSA_ALARMA'
          AND estado = 'RESUELTO'
        ORDER BY fechaHora DESC
        """
    )
    suspend fun obtenerFalsasAlarmas(): List<EventEntity>

    // ======================================
    // CONSULTAR INCIDENTES SOLUCIONADOS
    // ======================================
    @Query(
        """
        SELECT *
        FROM eventos
        WHERE estado = 'RESUELTO'
          AND motivoCierre = 'INCIDENTE_SOLUCIONADO'
        ORDER BY fechaHora DESC
        """
    )
    suspend fun obtenerIncidentesSolucionados():
        List<EventEntity>
}
