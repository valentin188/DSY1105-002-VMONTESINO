package com.example.modoguardian.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registrarUsuario(
        usuario: UserEntity
    )

    @Query("""
        SELECT * FROM usuarios
        WHERE email = :email COLLATE NOCASE
        LIMIT 1
    """)
    suspend fun buscarPorEmail(
        email: String
    ): UserEntity?

    @Query("""
        SELECT * FROM usuarios
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun buscarPorId(
        id: String
    ): UserEntity?

    @Query("""
        SELECT * FROM usuarios
        WHERE googleId = :googleId
        LIMIT 1
    """)
    suspend fun buscarPorGoogleId(
        googleId: String
    ): UserEntity?

    @Query("""
        SELECT COUNT(*) FROM usuarios
        WHERE email = :email COLLATE NOCASE
    """)
    suspend fun existeEmail(
        email: String
    ): Int

    @Query("""
        SELECT * FROM usuarios
        ORDER BY nombre ASC
    """)
    suspend fun obtenerUsuarios(): List<UserEntity>

    @Query("""
        SELECT * FROM usuarios
        ORDER BY nombre ASC
    """)
    fun observarUsuarios(): Flow<List<UserEntity>>
    
    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun actualizarUsuario(
        usuario: UserEntity
    )
}
