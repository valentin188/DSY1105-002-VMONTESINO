package com.example.modoguardian.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.LoginUiState
import com.example.modoguardian.ui.utils.WindowSizeClass
import com.example.modoguardian.ui.utils.rememberWindowSizeClass
import com.example.modoguardian.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    windowSize: WindowSizeClass = rememberWindowSizeClass()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (windowSize == WindowSizeClass.Compact) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    EncabezadoLogo()
                    Spacer(modifier = Modifier.height(24.dp))
                    FormularioLogin(state = state, viewModel = viewModel)
                }
            } else {
                Card(
                    modifier = Modifier
                        .width(480.dp)
                        .padding(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        EncabezadoLogo()
                        Spacer(modifier = Modifier.height(24.dp))
                        FormularioLogin(state = state, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun EncabezadoLogo() {
    Icon(
        imageVector = Icons.Default.Shield,
        contentDescription = "Logo Modo Guardián",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(72.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Modo Guardián",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
    Text(
        text = "Sistema de Control e Incidencias",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun FormularioLogin(
    state: LoginUiState,
    viewModel: LoginViewModel
) {
    OutlinedTextField(
        value = state.email,
        onValueChange = viewModel::onEmailChange,
        label = { Text("Correo Electrónico") },
        isError = state.errores.emailError != null,
        supportingText = {
            state.errores.emailError?.let { Text(it) }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = state.password,
        onValueChange = viewModel::onPasswordChange,
        label = { Text("Contraseña") },
        visualTransformation = PasswordVisualTransformation(),
        isError = state.errores.passwordError != null,
        supportingText = {
            state.errores.passwordError?.let { Text(it) }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth()
    )

    state.errores.generalError?.let { error ->
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Button(
        onClick = { viewModel.autenticar() },
        enabled = !state.isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text("Iniciar sesión")
        }
    }
}
