package com.example.modoguardian.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query(
        "SELECT * FROM eventos ORDER BY fechaHora DESC"
    )
    fun observarEventos(): Flow<List<EventEntity>>

    @Query(
        "SELECT * FROM eventos ORDER BY fechaHora DESC"
    )
    suspend fun obtenerEventos(): List<EventEntity>

    @Query(
        "SELECT * FROM eventos WHERE id = :eventoId LIMIT 1"
    )
    suspend fun obtenerEvento(
        eventoId: String
    ): EventEntity?

    @Query("SELECT COUNT(*) FROM eventos")
    suspend fun contarEventos(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarEventos(
        eventos: List<EventEntity>
    )

    @Query(
        """
        UPDATE eventos
        SET estado = :nuevoEstado,
            evidenciaFotoUri = COALESCE(
                :fotoUri,
                evidenciaFotoUri
            )
        WHERE id = :eventoId
        """
    )
    suspend fun actualizarEstado(
        eventoId: String,
        nuevoEstado: String,
        fotoUri: String?
    ): Int
}
