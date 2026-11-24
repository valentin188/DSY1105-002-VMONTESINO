package com.example.modoguardian.data

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.modoguardian.R
import com.example.modoguardian.model.Role
import com.example.modoguardian.model.User
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseAccess {

    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()

    fun usuarioActual(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun registrar(
        email: String,
        password: String
    ): FirebaseUser {

        return requireNotNull(
            auth.createUserWithEmailAndPassword(
                email,
                password
            ).await().user
        )
    }

    suspend fun iniciarSesion(
        email: String,
        password: String
    ): FirebaseUser {

        return requireNotNull(
            auth.signInWithEmailAndPassword(
                email,
                password
            ).await().user
        )
    }

    suspend fun iniciarGoogle(
        context: Context
    ): FirebaseUser {

        val option = GetSignInWithGoogleOption.Builder(
            context.getString(R.string.default_web_client_id)
        ).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val credential = CredentialManager
            .create(context)
            .getCredential(
                context = context,
                request = request
            ).credential

        require(
            credential is CustomCredential &&
            credential.type ==
                GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            "No se recibió una credencial de Google válida"
        }

        val token = GoogleIdTokenCredential
            .createFrom(credential.data)
            .idToken

        val firebaseCredential =
            GoogleAuthProvider.getCredential(
                token,
                null
            )

        return requireNotNull(
            auth.signInWithCredential(
                firebaseCredential
            ).await().user
        )
    }

    suspend fun obtenerPerfil(
        firebaseUser: FirebaseUser
    ): User? {

        val doc = db.collection("usuarios")
            .document(firebaseUser.uid)
            .get()
            .await()

        if (!doc.exists()) return null

        val nombre =
            doc.getString("nombre").orEmpty()

        val apellido =
            doc.getString("apellido").orEmpty()

        val domicilio =
            doc.getString("domicilio").orEmpty()

        val telefono =
            doc.getString("telefono").orEmpty()

        if (
            listOf(
                nombre,
                apellido,
                domicilio,
                telefono
            ).any { it.isBlank() }
        ) return null

        return User(
            firebaseUser.uid,
            "$nombre $apellido",
            firebaseUser.email.orEmpty(),
            Role.OPERADOR
        )
    }
    
    suspend fun guardarPerfil(
        firebaseUser: FirebaseUser,
        nombre: String,
        apellido: String,
        domicilio: String,
        telefono: String
    ): User {

        require(
            firebaseUser.uid == auth.currentUser?.uid
        )

        val email = requireNotNull(firebaseUser.email)

        val datos = mapOf(
            "nombre" to nombre.trim(),
            "apellido" to apellido.trim(),
            "domicilio" to domicilio.trim(),
            "telefono" to telefono.trim(),
            "email" to email,
            "rol" to "OPERADOR"
        )

        db.collection("usuarios")
            .document(firebaseUser.uid)
            .set(datos)
            .await()

        return User(
            firebaseUser.uid,
            "${nombre.trim()} ${apellido.trim()}",
            email,
            Role.OPERADOR
        )
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    suspend fun limpiarCredenciales(
        context: Context
    ) {
        CredentialManager.create(context)
            .clearCredentialState(
                ClearCredentialStateRequest()
            )
    }
}
