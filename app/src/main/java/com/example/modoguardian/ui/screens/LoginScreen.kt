package com.example.modoguardian.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.modoguardian.data.FirebaseAccess
import com.example.modoguardian.model.User
import com.example.modoguardian.ui.utils.WindowSizeClass
import com.example.modoguardian.ui.utils.rememberWindowSizeClass
import com.example.modoguardian.viewmodel.LoginViewModel
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    windowSize: WindowSizeClass = rememberWindowSizeClass(),
    onAuthenticated: (User) -> Unit = {}
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by viewModel.uiState.collectAsState()

    var pantalla by rememberSaveable {
        mutableStateOf("login")
    }

    var nombre by rememberSaveable {
        mutableStateOf("")
    }

    var apellido by rememberSaveable {
        mutableStateOf("")
    }

    var domicilio by rememberSaveable {
        mutableStateOf("")
    }

    var telefono by rememberSaveable {
        mutableStateOf("")
    }

    var correoRegistro by rememberSaveable {
        mutableStateOf("")
    }

    var claveRegistro by remember {
        mutableStateOf("")
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    var cargando by remember {
        mutableStateOf(false)
    }

    val ocupado = cargando || state.isLoading

    val demoEmails = setOf(
        "admin@guardian.test",
        "supervisor@guardian.test",
        "operador@guardian.test"
    )

    fun ejecutar(accion: suspend () -> Unit) {

        if (ocupado) return

        error = null
        cargando = true

        scope.launch {

            try {

                accion()

            } catch (e: CancellationException) {

                throw e

            } catch (e: GetCredentialCancellationException) {

                error = "Acceso con Google cancelado."

            } catch (e: FirebaseAuthUserCollisionException) {

                error = "Ya existe una cuenta con ese correo."

            } catch (e: Exception) {

                error = "No se pudo completar la operación. Revisa tus datos, conexión y configuración de Firebase."

            } finally {

                cargando = false
            }
        }
    }

    fun completarPerfil(firebaseUser: FirebaseUser) {

        correoRegistro = firebaseUser.email.orEmpty()

        val partes = firebaseUser.displayName
            .orEmpty()
            .trim()
            .split(Regex("\\s+"), limit = 2)

        nombre = partes.firstOrNull().orEmpty()
        apellido = partes.getOrNull(1).orEmpty()

        domicilio = ""
        telefono = ""
        claveRegistro = ""

        pantalla = "completar"
    }

    suspend fun continuar(firebaseUser: FirebaseUser) {

        val perfil = FirebaseAccess.obtenerPerfil(firebaseUser)

        if (perfil == null) {

            completarPerfil(firebaseUser)

        } else {

            onAuthenticated(perfil)
        }
    }

    fun volver() {

        if (pantalla == "completar") {
            FirebaseAccess.cerrarSesion()
        }

        pantalla = "login"
        error = null
        claveRegistro = ""
    }

    Scaffold { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {

            Column(
                modifier = Modifier
                    .widthIn(
                        max = if (
                            windowSize == WindowSizeClass.Compact
                        ) 600.dp else 480.dp
                    )
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),

                horizontalAlignment = Alignment.CenterHorizontally,

                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Logo Modo Guardián",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                )

                Text(
                    text = "Modo Guardián",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "Sistema de Control e Incidencias",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                if (pantalla == "login") {

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = {
                            error = null
                            viewModel.onEmailChange(it)
                        },
                        label = {
                            Text("Correo Electrónico")
                        },
                        singleLine = true,
                        enabled = !ocupado,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        isError = state.errores.emailError != null,
                        supportingText = {
                            state.errores.emailError?.let {
                                Text(it)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = {
                            error = null
                            viewModel.onPasswordChange(it)
                        },
                        label = {
                            Text("Contraseña")
                        },
                        singleLine = true,
                        enabled = !ocupado,
                        visualTransformation =
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        isError =
                            state.errores.passwordError != null,
                        supportingText = {
                            state.errores.passwordError?.let {
                                Text(it)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {

                            val email = state.email.trim()

                            when {

                                email in demoEmails ->
                                    viewModel.autenticar()

                                !Patterns.EMAIL_ADDRESS
                                    .matcher(email).matches() ->
                                    error = "Ingresa un correo válido."

                                state.password.isBlank() ->
                                    error = "Ingresa tu contraseña."

                                else -> ejecutar {

                                    continuar(
                                        FirebaseAccess.iniciarSesion(
                                            email,
                                            state.password
                                        )
                                    )
                                }
                            }
                        },
                        enabled = !ocupado,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Iniciar sesión")
                    }

                    OutlinedButton(
                        onClick = {

                            nombre = ""
                            apellido = ""
                            domicilio = ""
                            telefono = ""
                            correoRegistro = ""
                            claveRegistro = ""
                            error = null

                            pantalla = "registro"
                        },
                        enabled = !ocupado,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Registrarse")
                    }

                    // SEPARADOR

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        HorizontalDivider(
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "  O  ",
                            color = MaterialTheme
                                .colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider(
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedButton(
                        onClick = {

                            ejecutar {

                                continuar(
                                    FirebaseAccess.iniciarGoogle(
                                        context
                                    )
                                )
                            }
                        },
                        enabled = !ocupado,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = MaterialTheme.shapes.small
                    ) {

                        Text(
                            text = "G",
                            color = Color(0xFF4285F4),
                            style = MaterialTheme
                                .typography.titleLarge
                        )

                        Spacer(Modifier.width(12.dp))

                        Text("Continuar con Google")
                    }

                    state.errores.generalError?.let {

                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                } else {

                    val completando = pantalla == "completar"

                    Text(
                        text = if (completando)
                            "Completa tu perfil"
                        else
                            "Crear cuenta",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    CampoRegistro(
                        "Nombre",
                        nombre,
                        { nombre = it },
                        !ocupado
                    )

                    CampoRegistro(
                        "Apellido",
                        apellido,
                        { apellido = it },
                        !ocupado
                    )

                    CampoRegistro(
                        "Domicilio",
                        domicilio,
                        { domicilio = it },
                        !ocupado
                    )

                    CampoRegistro(
                        "Número de teléfono",
                        telefono,
                        { telefono = it },
                        !ocupado,
                        KeyboardType.Phone
                    )

                    CampoRegistro(
                        "Correo electrónico",
                        correoRegistro,
                        { correoRegistro = it },
                        !ocupado && !completando,
                        KeyboardType.Email
                    )

                    if (!completando) {

                        CampoRegistro(
                            "Contraseña",
                            claveRegistro,
                            { claveRegistro = it },
                            !ocupado,
                            KeyboardType.Password
                        )
                    }

                    Button(
                        onClick = {

                            val email = correoRegistro.trim()

                            val cantidadDigitos =
                                telefono.count { it.isDigit() }

                            error = when {

                                nombre.isBlank() ||
                                apellido.isBlank() ||
                                domicilio.isBlank() ->
                                    "Completa todos los campos."

                                cantidadDigitos !in 8..15 ||
                                !telefono.matches(
                                    Regex("[+0-9() .-]+")
                                ) ->
                                    "Ingresa un número de teléfono válido."

                                !Patterns.EMAIL_ADDRESS
                                    .matcher(email).matches() ->
                                    "Ingresa un correo válido."

                                !completando &&
                                email.lowercase() in demoEmails ->
                                    "Ese correo está reservado."

                                !completando &&
                                claveRegistro.length < 8 ->
                                    "La contraseña debe tener al menos 8 caracteres."

                                else -> null
                            }

                            if (error == null) {

                                ejecutar {

                                    val firebaseUser =
                                        if (completando) {

                                            requireNotNull(
                                                FirebaseAccess.usuarioActual()
                                            )

                                        } else {

                                            FirebaseAccess.registrar(
                                                email,
                                                claveRegistro
                                            )
                                        }

                                    pantalla = "completar"

                                    correoRegistro =
                                        firebaseUser.email.orEmpty()

                                    claveRegistro = ""

                                    val perfil =
                                        FirebaseAccess.guardarPerfil(
                                            firebaseUser,
                                            nombre,
                                            apellido,
                                            domicilio,
                                            telefono
                                        )

                                    onAuthenticated(perfil)
                                }
                            }
                        },
                        enabled = !ocupado,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {

                        Text(
                            if (completando)
                                "Guardar y continuar"
                            else
                                "Registrarse"
                        )
                    }

                    TextButton(
                        onClick = { volver() },
                        enabled = !ocupado
                    ) {

                        Text("Volver al inicio de sesión")
                    }
                }

                if (ocupado) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp)
                    )
                }

                error?.let {

                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun CampoRegistro(
    etiqueta: String,
    valor: String,
    cambiar: (String) -> Unit,
    habilitado: Boolean,
    tipo: KeyboardType = KeyboardType.Text
) {

    OutlinedTextField(
        value = valor,
        onValueChange = cambiar,
        label = { Text(etiqueta) },
        enabled = habilitado,
        singleLine = true,

        keyboardOptions = KeyboardOptions(
            keyboardType = tipo
        ),

        visualTransformation =
            if (tipo == KeyboardType.Password)
                PasswordVisualTransformation()
            else
                VisualTransformation.None,

        modifier = Modifier.fillMaxWidth()
    )
}
