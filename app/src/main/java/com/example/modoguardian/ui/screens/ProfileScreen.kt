package com.example.modoguardian.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.User
import com.example.modoguardian.ui.components.ImagenInteligente

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User?,
    onBack: () -> Unit,
    onPhotoUpdated: (String) -> Unit,
    onLogout: () -> Unit
) {
    var fotoUri by remember(user?.id, user?.fotoPerfilUri) {
        mutableStateOf(
            user?.fotoPerfilUri?.let { Uri.parse(it) }
        )
    }

    var mostrarConfirmacion by remember {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            ImagenInteligente(
                imageUri = fotoUri,
                onImageSelected = { uri ->
                    fotoUri = uri
                    uri?.let {
                        onPhotoUpdated(it.toString())
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = user?.nombre ?: "Usuario desconocido",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = user?.email ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            AssistChip(
                onClick = {},
                label = {
                    Text(
                        "Rol: ${user?.rol?.name ?: "SIN ROL"}"
                    )
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    mostrarConfirmacion = true
                },
                enabled = user != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        MaterialTheme.colorScheme.error
                )
            ) {
                Text("Cerrar sesión")
            }
        }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = {
                mostrarConfirmacion = false
            },
            title = {
                Text("Cerrar sesión")
            },
            text = {
                Text(
                    "¿Estás seguro de que deseas salir de INSERCODE?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false
                        onLogout()
                    }
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
