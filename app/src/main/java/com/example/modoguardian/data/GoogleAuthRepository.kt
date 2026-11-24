package com.example.modoguardian.data

import android.content.Context
import android.util.Base64
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.modoguardian.data.local.AppDatabase
import com.example.modoguardian.data.local.UserEntity
import com.example.modoguardian.model.Role
import com.example.modoguardian.model.User
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Locale
import java.util.UUID
import java.security.SecureRandom
import javax.net.ssl.HttpsURLConnection

class GoogleAuthRepository(
    private val context: Context
) {

    companion object {

        private const val WEB_CLIENT_ID =
            "REEMPLAZAR_CON_CLIENT_ID_DE_GOOGLE"
    }

    private val credentialManager =
        CredentialManager.create(context)

    private val userDao =
        AppDatabase.getDatabase(
            context.applicationContext
        ).userDao()

    suspend fun iniciarSesion(): User {

        require(
            WEB_CLIENT_ID.endsWith(
                ".apps.googleusercontent.com"
            )
        ) {
            "Falta configurar el Client ID de Google."
        }

        val bytes = ByteArray(32)

        SecureRandom().nextBytes(bytes)

        val nonce = Base64.encodeToString(
            bytes,
            Base64.URL_SAFE or
                Base64.NO_WRAP or
                Base64.NO_PADDING
        )

        val googleOption =
            GetSignInWithGoogleOption.Builder(
                WEB_CLIENT_ID
            )
                .setNonce(nonce)
                .build()

        val request =
            GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build()

        val resultado =
            credentialManager.getCredential(
                context = context,
                request = request
            )

        val credential = resultado.credential

        require(
            credential is CustomCredential &&
            credential.type ==
                GoogleIdTokenCredential
                    .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            "No se recibió una cuenta válida de Google."
        }

        val googleCredential =
            GoogleIdTokenCredential.createFrom(
                credential.data
            )

        val datos = verificarToken(
            googleCredential.idToken,
            nonce
        )

        val googleId = datos.getString("sub")

        val correo = datos.getString("email")
            .trim()
            .lowercase(Locale.ROOT)

        val nombre = datos.optString(
            "given_name"
        ).ifBlank {
            googleCredential.givenName
                .orEmpty()
                .ifBlank { "Usuario" }
        }

        val apellido = datos.optString(
            "family_name"
        ).ifBlank {
            googleCredential.familyName.orEmpty()
        }

        return withContext(Dispatchers.IO) {

            val usuarioExistente =
                userDao.buscarPorGoogleId(googleId)

            if (usuarioExistente != null) {

                convertirUsuario(usuarioExistente)

            } else {

                require(
                    correo !in setOf(
                        "admin@guardian.test",
                        "supervisor@guardian.test",
                        "operador@guardian.test"
                    )
                ) {
                    "Este correo está reservado."
                }

                require(
                    userDao.buscarPorEmail(correo) == null
                ) {
                    "Ya existe una cuenta con este correo."
                }

                val nuevoUsuario = UserEntity(

                    id = UUID.randomUUID().toString(),

                    nombre = nombre,

                    apellido = apellido,

                    domicilio = "",

                    telefono = "",

                    email = correo,

                    passwordHash = null,

                    passwordSalt = null,

                    rol = Role.OPERADOR.name,

                    proveedor = "GOOGLE",

                    googleId = googleId,

                    fotoPerfilUri = null
                )

                userDao.registrarUsuario(nuevoUsuario)

                convertirUsuario(nuevoUsuario)
            }
        }
    }

    private suspend fun verificarToken(
        token: String,
        nonce: String
    ): JSONObject = withContext(Dispatchers.IO) {

        val url = URL(
            "https://oauth2.googleapis.com/tokeninfo?id_token=$token"
        )

        val conexion =
            url.openConnection() as HttpsURLConnection

        conexion.requestMethod = "GET"
        conexion.connectTimeout = 15000
        conexion.readTimeout = 15000
        conexion.instanceFollowRedirects = false
        conexion.useCaches = false

        try {

            require(conexion.responseCode == 200) {
                "No se pudo verificar la cuenta de Google."
            }

            val respuesta =
                conexion.inputStream
                    .bufferedReader()
                    .use { it.readText() }

            val datos = JSONObject(respuesta)

            require(
                datos.optString("aud") == WEB_CLIENT_ID
            ) {
                "La cuenta no corresponde a esta aplicación."
            }

            require(
                datos.optString("iss") in setOf(
                    "accounts.google.com",
                    "https://accounts.google.com"
                )
            ) {
                "Proveedor de autenticación incorrecto."
            }

            require(
                datos.optLong("exp") >
                    System.currentTimeMillis() / 1000
            ) {
                "La sesión de Google ha expirado."
            }

            require(
                datos.optString("email_verified") == "true"
            ) {
                "El correo de Google no está verificado."
            }

            val partes = token.split(".")

            require(partes.size == 3) {
                "Token de Google incorrecto."
            }

            val contenido = String(
                Base64.decode(
                    partes[1],
                    Base64.URL_SAFE or
                        Base64.NO_WRAP or
                        Base64.NO_PADDING
                ),
                Charsets.UTF_8
            )

            val payload = JSONObject(contenido)

            require(
                payload.optString("nonce") == nonce
            ) {
                "La solicitud de Google no es válida."
            }

            datos

        } finally {

            conexion.disconnect()
        }
    }

    suspend fun cerrarSesion() {

        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
    }

    private fun convertirUsuario(
        usuario: UserEntity
    ): User {

        return User(

            id = usuario.id,

            nombre =
                "${usuario.nombre} ${usuario.apellido}".trim(),
