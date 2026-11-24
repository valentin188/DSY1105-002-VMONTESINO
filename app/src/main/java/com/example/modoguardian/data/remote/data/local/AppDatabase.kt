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
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun eventDao(): EventDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 =
            object : Migration(1, 2) {

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

        private val MIGRATION_2_3 =
            object : Migration(2, 3) {

                override fun migrate(
                    db: SupportSQLiteDatabase
                ) {

                    db.execSQL(
                        "ALTER TABLE eventos ADD COLUMN motivoCierre TEXT"
                    )
                }
            }

        private val MIGRATION_3_4 =
            object : Migration(3, 4) {

                override fun migrate(
                    db: SupportSQLiteDatabase
                ) {

                    db.execSQL(
                        "ALTER TABLE eventos ADD COLUMN evidenciaVideoUri TEXT"
                    )

                    db.execSQL(
                        "ALTER TABLE eventos ADD COLUMN cameraId TEXT"
                    )

                    db.execSQL(
                        "ALTER TABLE eventos ADD COLUMN reportadoPorId TEXT"
                    )

                    db.execSQL(
                        "ALTER TABLE eventos ADD COLUMN reportadoPorNombre TEXT"
                    )
                }
            }

        private val MIGRATION_4_5 =
            object : Migration(4, 5) {

                override fun migrate(
                    db: SupportSQLiteDatabase
                ) {

                    db.execSQL(
                        "ALTER TABLE usuarios RENAME TO usuarios_anteriores"
                    )

                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS usuarios (
                            id TEXT NOT NULL PRIMARY KEY,
                            nombre TEXT NOT NULL,
                            apellido TEXT NOT NULL,
                            domicilio TEXT NOT NULL,
                            telefono TEXT NOT NULL,
                            email TEXT NOT NULL,
                            passwordHash TEXT,
                            passwordSalt TEXT,
                            rol TEXT NOT NULL,
                            proveedor TEXT NOT NULL,
                            googleId TEXT,
                            fotoPerfilUri TEXT
                        )
                        """.trimIndent()
                    )

                    db.execSQL(
                        """
                        CREATE UNIQUE INDEX IF NOT EXISTS
                        index_usuarios_email
                        ON usuarios(email)
                        """.trimIndent()
                    )

                    db.execSQL(
                        """
                        CREATE UNIQUE INDEX IF NOT EXISTS
                        index_usuarios_googleId
                        ON usuarios(googleId)
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
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5
                    )
                    .build()
                    .also {
                        INSTANCE = it
                    }
            }
        }
    }
}
