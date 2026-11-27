package com.example.modoguardian.ui.screens

import android.util.Patterns
import androidx.activity.compose.BackHandler
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
import com.example.modoguardian.data.LocalAuthRepository
import com.example.modoguardian.data.GoogleAuthRepository
import com.example.modoguardian.model.User
import com.example.modoguardian.ui.utils.WindowSizeClass
import com.example.modoguardian.ui.utils.rememberWindowSizeClass
import com.example.modoguardian.viewmodel.LoginViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    windowSize: WindowSizeClass = rememberWindowSizeClass(),
    onAuthenticated: (User) -> Unit = {}
) {

    val context = LocalContext.current

    val repository = remember(context) {
        LocalAuthRepository(context)
    }

    val googleRepository = remember(context) {
        GoogleAuthRepository(context)
    }

    val scope = rememberCoroutineScope()

    val state by viewModel.uiState.collectAsState()

    var mostrarRegistro by rememberSaveable {
        mutableStateOf(false)
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

    var correo by rememberSaveable {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    var cargando by remember {
        mutableStateOf(false)
    }

    val ocupado = cargando || state.isLoading

    LaunchedEffect(Unit) {
        cargando = false
        mostrarRegistro = false
        error = null
    }

    val correosDemo = setOf(
        "admin@guardian.test",
        "supervisor@guardian.test",
        "operador@guardian.test"
    )

    fun ejecutar(accion: suspend () -> Unit) {

        if (cargando || state.isLoading) return

        error = null
        cargando = true

        scope.launch {

            try {

                accion()

            } catch (e: CancellationException) {

                throw e

            } catch (e: GetCredentialCancellationException) {

                error = "Inicio de sesión con Google cancelado."

            } catch (e: IllegalArgumentException) {

                error = e.message ?: "Revisa los datos ingresados."

            } catch (e: Exception) {

                error = "No se pudo completar la operación."

            } finally {

                cargando = false
            }
        }
    }

    fun volver() {

        mostrarRegistro = false
        password = ""
        error = null
    }

    BackHandler(
        enabled = mostrarRegistro && !ocupado
    ) {
        volver()
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

                    .verticalScroll(
                        rememberScrollState()
                    )

                    .padding(24.dp),

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)

            ) {

                Icon(

                    imageVector = Icons.Default.Shield,

                    contentDescription = "Modo Guardián",

                    tint = MaterialTheme.colorScheme.primary,

                    modifier = Modifier.size(72.dp)
                )

                Text(

                    text = "Modo Guardián",

                    style =
                        MaterialTheme.typography.headlineMedium
                )

                Text(

                    text = "Sistema de Control e Incidencias",

                    style =
                        MaterialTheme.typography.bodyMedium,

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                if (!mostrarRegistro) {

                    OutlinedTextField(

                        value = state.email,

                        onValueChange = {
                            error = null
                            viewModel.onEmailChange(it)
                        },

                        label = {
                            Text("Correo electrónico")
                        },

                        singleLine = true,

                        enabled = !ocupado,

                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),

                        isError =
                            state.errores.emailError != null,

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

                        enabled = !ocupado,

                        singleLine = true,

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

                            if (ocupado) return@Button

                            val email = state.email.trim()

                            when {

                                email.lowercase() in correosDemo -> {

                                    viewModel.autenticar()
                                }

                                !Patterns.EMAIL_ADDRESS
                                    .matcher(email).matches() -> {

                                    error = "Ingresa un correo válido."
                                }

                                state.password.isBlank() -> {

                                    error = "Ingresa tu contraseña."
                                }

                                else -> {

                                    ejecutar {

                                        val usuario =
                                            repository.iniciarSesion(
                                                email,
                                                state.password
                                            )

                                        onAuthenticated(usuario)
                                    }
                                }
                            }
                        },

                        enabled = true,

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
                            correo = ""
                            password = ""
                            error = null

                            mostrarRegistro = true
                        },

                        enabled = !ocupado,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)

                    ) {

                        Text("Registrarse")
                    }

                    Row(

                        verticalAlignment =
                            Alignment.CenterVertically,

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

                                val usuario =
                                    googleRepository.iniciarSesion()

                                onAuthenticated(usuario)
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

                            style =
                                MaterialTheme.typography.titleLarge
                        )

                        Spacer(
                            modifier = Modifier.width(12.dp)
                        )

                        Text("Continuar con Google")
                    }

                    state.errores.generalError?.let {

                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                } else {

                    Text(

                        text = "Crear cuenta",

                        style =
                            MaterialTheme.typography.headlineSmall
                    )

                    CampoRegistro(
                        etiqueta = "Nombre",
                        valor = nombre,
                        cambiar = { nombre = it },
                        habilitado = !ocupado
                    )

                    CampoRegistro(
                        etiqueta = "Apellido",
                        valor = apellido,
                        cambiar = { apellido = it },
                        habilitado = !ocupado
                    )

                    CampoRegistro(
                        etiqueta = "Domicilio",
                        valor = domicilio,
                        cambiar = { domicilio = it },
                        habilitado = !ocupado
                    )

                    CampoRegistro(
                        etiqueta = "Número de teléfono",
                        valor = telefono,
                        cambiar = { telefono = it },
                        habilitado = !ocupado,
                        tipo = KeyboardType.Phone
                    )

                    CampoRegistro(
                        etiqueta = "Correo electrónico",
                        valor = correo,
                        cambiar = { correo = it },
                        habilitado = !ocupado,
                        tipo = KeyboardType.Email
                    )

                    CampoRegistro(
                        etiqueta = "Contraseña",
                        valor = password,
                        cambiar = { password = it },
                        habilitado = !ocupado,
                        tipo = KeyboardType.Password
                    )

                    Text(

                        text = "La contraseña debe tener al menos 8 caracteres.",

                        style =
                            MaterialTheme.typography.bodySmall,

                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(

                        onClick = {

                            ejecutar {

                                val usuario = repository.registrar(

                                    nombre = nombre,

                                    apellido = apellido,

                                    domicilio = domicilio,

                                    telefono = telefono,

                                    email = correo,

                                    password = password
                                )

                                password = ""

                                onAuthenticated(usuario)
                            }
                        },

                        enabled = !ocupado,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)

                    ) {

                        Text("Registrarse")
                    }

                    OutlinedButton(

                        onClick = { volver() },

                        enabled = true,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)

                    ) {

                        Text("Iniciar sesión")
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

                        color =
                            MaterialTheme.colorScheme.error,

                        style =
                            MaterialTheme.typography.bodySmall
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

        label = {
            Text(etiqueta)
        },

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