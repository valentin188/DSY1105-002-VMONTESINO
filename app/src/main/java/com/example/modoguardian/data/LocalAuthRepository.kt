package com.example.modoguardian.data

import android.content.Context
import android.util.Base64
import android.util.Patterns
import com.example.modoguardian.data.local.AppDatabase
import com.example.modoguardian.data.local.UserEntity
import com.example.modoguardian.model.Role
import com.example.modoguardian.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class LocalAuthRepository(context: Context) {

    private val userDao =
        AppDatabase.getDatabase(
            context.applicationContext
        ).userDao()

    private val correosDemo = setOf(
        "admin@guardian.test",
        "supervisor@guardian.test",
        "operador@guardian.test"
    )

    suspend fun registrar(
        nombre: String,
        apellido: String,
        domicilio: String,
        telefono: String,
        email: String,
        password: String
    ): User = withContext(Dispatchers.IO) {

        val correo = email.trim()
            .lowercase(Locale.ROOT)

        require(
            nombre.isNotBlank() &&
            apellido.isNotBlank() &&
            domicilio.isNotBlank()
        ) {
            "Completa todos los campos."
        }

        require(
            telefono.count { it.isDigit() } in 8..15 &&
            telefono.matches(Regex("[+0-9() .-]+"))
        ) {
            "Ingresa un número de teléfono válido."
        }

        require(
            Patterns.EMAIL_ADDRESS
                .matcher(correo).matches()
        ) {
            "Ingresa un correo electrónico válido."
        }

        require(password.length >= 8) {
            "La contraseña debe tener al menos 8 caracteres."
        }

        require(correo !in correosDemo) {
            "Este correo está reservado."
        }

        require(userDao.existeEmail(correo) == 0) {
            "Ya existe una cuenta con este correo."
        }

        val salt = ByteArray(16)

        SecureRandom().nextBytes(salt)

        val hash = generarHash(password, salt)

        val usuario = UserEntity(

            id = UUID.randomUUID().toString(),

            nombre = nombre.trim(),

            apellido = apellido.trim(),

            domicilio = domicilio.trim(),

            telefono = telefono.trim(),

            email = correo,

            passwordHash = Base64.encodeToString(
                hash,
                Base64.NO_WRAP
            ),

            passwordSalt = Base64.encodeToString(
                salt,
                Base64.NO_WRAP
            ),

            rol = Role.OPERADOR.name,

            proveedor = "LOCAL"
        )

        userDao.registrarUsuario(usuario)

        convertirUsuario(usuario)
    }

    suspend fun iniciarSesion(
        email: String,
        password: String
    ): User = withContext(Dispatchers.IO) {

        val correo = email.trim()
            .lowercase(Locale.ROOT)

        val usuario = userDao.buscarPorEmail(correo)
            ?: throw IllegalArgumentException(
                "Correo o contraseña incorrectos."
            )

        require(usuario.proveedor == "LOCAL") {
            "Debes iniciar sesión con tu proveedor original."
        }

        val saltGuardado = usuario.passwordSalt
            ?: throw IllegalArgumentException(
                "Credenciales incorrectas."
            )

        val hashGuardado = usuario.passwordHash
            ?: throw IllegalArgumentException(
                "Credenciales incorrectas."
            )

        val salt = Base64.decode(
            saltGuardado,
            Base64.NO_WRAP
        )

        val hashOriginal = Base64.decode(
            hashGuardado,
            Base64.NO_WRAP
        )

        val hashIngresado = generarHash(
            password,
            salt
        )

        require(
            MessageDigest.isEqual(
                hashOriginal,
                hashIngresado
            )
        ) {
            "Correo o contraseña incorrectos."
        }

        convertirUsuario(usuario)
    }

    private fun generarHash(
        password: String,
        salt: ByteArray
    ): ByteArray {

        val especificacion = PBEKeySpec(
            password.toCharArray(),
            salt,
            600000,
            256
        )

        return try {

            SecretKeyFactory.getInstance(
                "PBKDF2WithHmacSHA256"
            )
                .generateSecret(especificacion)
                .encoded

        } finally {

            especificacion.clearPassword()
        }
    }

    private fun convertirUsuario(
        usuario: UserEntity
    ): User {

        return User(

            id = usuario.id,

            nombre = "${usuario.nombre} ${usuario.apellido}",

            email = usuario.email,

            rol = Role.OPERADOR,

            fotoPerfilUri = usuario.fotoPerfilUri
        )
    }
}
