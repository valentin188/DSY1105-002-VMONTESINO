package com.example.modoguardian.data.local

import android.content.Context

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration

import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        EventEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun eventDao(): EventDao
    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(
                db: SupportSQLiteDatabase
            ) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS eventos (
                        id TEXT NOT NULL PRIMARY KEY,
                        titulo TEXT NOT NULL,
                        descripcion TEXT NOT NULL,
                        ubicacion TEXT NOT NULL,
                        prioridad TEXT NOT NULL,
                        estado TEXT NOT NULL,
                        fechaHora TEXT NOT NULL,
                        evidenciaFotoUri TEXT
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(
                db: SupportSQLiteDatabase
            ) {

                db.execSQL(
                    """
                    ALTER TABLE eventos
                    ADD COLUMN motivoCierre TEXT
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(
            context: Context
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "modo_guardian_db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3
                    )
                    .build()
                    .also {
                        INSTANCE = it
                    }
            }
        }
    }
}
