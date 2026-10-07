package com.example.modoguardian.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.Event
import com.example.modoguardian.ui.components.ImagenInteligente

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event?,
    onBack: () -> Unit,
    onStatusChange: (EstadoEvento, String?) -> Unit
) {
    var fotoUri by remember { mutableStateOf<Uri?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Evento") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (event == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Text("Evento no encontrado", modifier = Modifier.padding(16.dp))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(text = event.titulo, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Ubicación: ${event.ubicacion}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Prioridad: ${event.prioridad}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Estado actual: ${event.estado}", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Descripción:", style = MaterialTheme.typography.titleSmall)
                Text(text = event.descripcion, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Adjuntar Evidencia Fotográfica:", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                ImagenInteligente(
                    imageUri = fotoUri,
                    onImageSelected = { uri -> fotoUri = uri }
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Cambiar Estado:", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onStatusChange(EstadoEvento.EN_PROCESO, fotoUri?.toString()) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("En Proceso")
                    }
                    Button(
                        onClick = { onStatusChange(EstadoEvento.RESUELTO, fotoUri?.toString()) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Resolver")
                    }
                }
            }
        }
    }
}