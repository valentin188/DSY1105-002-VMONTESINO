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
    onPhotoUpdated: (String) -> Unit
) {
    var fotoUri by remember { mutableStateOf<Uri?>(user?.fotoPerfilUri?.let { Uri.parse(it) }) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                    uri?.let { onPhotoUpdated(it.toString()) }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = user?.nombre ?: "Usuario Desconocido",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user?.email ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            AssistChip(
                onClick = { },
                label = { Text("Rol: ${user?.rol?.name ?: "SIN ROL"}") }
            )
        }
    }
}