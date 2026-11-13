package com.example.modoguardian.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.Event
import com.example.modoguardian.model.Prioridad
import com.example.modoguardian.ui.components.ImagenInteligente
import com.example.modoguardian.ui.utils.EvidenceStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val eventBg = Color(0xFF0B1426)
private val eventCard = Color(0xFF192740)
private val eventBlue = Color(0xFF2864E8)
private val eventRed = Color(0xFFF04452)
private val eventGreen = Color(0xFF20C997)
private val eventGray = Color(0xFF9BAAC3)
private val eventYellow = Color(0xFFFFC857)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event?,
    onBack: () -> Unit,
    onStatusChange: (EstadoEvento, String?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedPhoto by remember(event?.id) {
        mutableStateOf<Uri?>(null)
    }

    var pendingStatus by remember(event?.id) {
        mutableStateOf<EstadoEvento?>(null)
    }

    var saving by remember(event?.id) {
        mutableStateOf(false)
    }

    var errorMessage by remember(event?.id) {
        mutableStateOf<String?>(null)
    }

    val savedPhoto = remember(
        event?.id,
        event?.evidenciaFotoUri
    ) {
        event?.evidenciaFotoUri?.let(Uri::parse)
    }

    val previewPhoto = selectedPhoto ?: savedPhoto

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedPhoto = uri
            errorMessage = null
        }
    }

    Scaffold(
        containerColor = eventBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detalle del evento",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        enabled = !saving
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = eventBg
                )
            )
        }
    ) { padding ->

        if (event == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Evento no encontrado",
                    color = Color.White
                )
            }

        } else {

            val priorityColor = when (event.prioridad) {
                Prioridad.CRITICA -> eventRed
                Prioridad.ALTA -> eventYellow
                Prioridad.MEDIA -> eventBlue
                Prioridad.BAJA -> eventGreen
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(18.dp)
            ) {

                // INFORMACIÓN DEL EVENTO
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = eventCard
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            "INSERCODE · INCIDENTE",
                            color = eventGray,
                            style =
                                MaterialTheme.typography.labelMedium
                        )

                        Text(
                            event.titulo,
                            color = Color.White,
                            style =
                                MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "PRIORIDAD ${event.prioridad.name}",
                            color = priorityColor,
                            fontWeight = FontWeight.Bold
                        )

                        HorizontalDivider(color = eventBg)

                        EventDetailField(
                            "Código del evento",
                            event.id
                        )

                        EventDetailField(
                            "Ubicación",
                            event.ubicacion
                        )

                        EventDetailField(
                            "Fecha y hora",
                            event.fechaHora
                        )

                        EventDetailField(
                            "Estado",
                            event.estado.name.replace("_", " ")
                        )
                    }
                }

                // DESCRIPCIÓN
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = eventCard
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "DESCRIPCIÓN DEL INCIDENTE",
                            color = eventGray,
                            style =
                                MaterialTheme.typography.labelMedium
                        )

                        Text(
                            event.descripcion,
                            color = Color.White,
                            style =
                                MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // EVIDENCIA FOTOGRÁFICA
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = eventCard
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.spacedBy(14.dp)
                    ) {

                        Text(
                            "EVIDENCIA FOTOGRÁFICA",
                            color = eventGray,
                            style =
                                MaterialTheme.typography.labelMedium
                        )

                        ImagenInteligente(
                            fotoUri = previewPhoto
                        )

                        Text(
                            when {
                                selectedPhoto != null ->
                                    "Nueva fotografía seleccionada"

                                savedPhoto != null ->
                                    "Evidencia guardada en el evento"

                                else ->
                                    "No hay fotografía adjunta"
                            },
                            color = if (previewPhoto != null)
                                eventGreen else eventGray,
                            style =
                                MaterialTheme.typography.bodySmall
                        )

                        OutlinedButton(
                            onClick = {
                                photoPicker.launch("image/*")
                            },
                            enabled = !saving &&
                                event.estado != EstadoEvento.RESUELTO,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = null
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                if (previewPhoto == null)
                                    "Seleccionar fotografía"
                                else
                                    "Cambiar fotografía"
                            )
                        }

                        if (selectedPhoto != null) {
                            Text(
                                "La nueva fotografía se guardará " +
                                    "al confirmar una acción del evento.",
                                color = eventGray,
                                style =
                                    MaterialTheme.typography.bodySmall
                            )
                        }

                        if (selectedPhoto != null && !saving) {
                            TextButton(
                                onClick = {
                                    selectedPhoto = null
                                }
                            ) {
                                Text("Cancelar selección")
                            }
                        }
                    }
                }

                // ACCIONES
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = eventCard
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            "ACCIONES DEL EVENTO",
                            color = eventGray,
                            style =
                                MaterialTheme.typography.labelMedium
                        )

                        if (
                            event.estado == EstadoEvento.PENDIENTE
                        ) {
                            Button(
                                onClick = {
                                    pendingStatus =
                                        EstadoEvento.EN_PROCESO
                                },
                                enabled = !saving,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = eventBlue
                                )
                            ) {
                                Text("Confirmar alerta")
                            }
                        }

                        if (
                            event.estado != EstadoEvento.RESUELTO
                        ) {
                            Button(
                                onClick = {
                                    pendingStatus =
                                        EstadoEvento.RESUELTO
                                },
                                enabled = !saving,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = eventGreen
                                )
                            ) {
                                Text("Resolver incidente")
                            }
                        } else {
                            Text(
                                "Este incidente está resuelto.",
                                color = eventGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (saving) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = eventBlue
                            )

                            Text(
                                "Guardando evidencia...",
                                color = eventGray
                            )
                        }

                        errorMessage?.let { message ->
                            Text(
                                message,
                                color = eventRed,
                                style =
                                    MaterialTheme.typography.bodySmall
                            )
                        }

                        Text(
                            "Los cambios se almacenan " +
                                "localmente en el dispositivo.",
                            color = eventGray,
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    // CONFIRMACIÓN
    val statusToConfirm = pendingStatus
    val currentEvent = event

    if (statusToConfirm != null && currentEvent != null) {

        AlertDialog(
            onDismissRequest = {
                if (!saving) {
                    pendingStatus = null
                }
            },
            title = {
                Text("Confirmar actualización")
            },
            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "¿Deseas cambiar el estado " +
                            "del evento ${currentEvent.id} a " +
                            statusToConfirm.name
                                .replace("_", " ") +
                            "?"
                    )

                    if (selectedPhoto != null) {
                        Text(
                            "También se guardará la fotografía " +
                                "seleccionada como evidencia.",
                            color =
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !saving,
                    onClick = {
                        scope.launch {
                            saving = true
                            errorMessage = null

                            try {
                                val photoToSave = selectedPhoto

                                val storedUri =
                                    if (photoToSave != null) {
                                        withContext(Dispatchers.IO) {
                                            EvidenceStorage.save(
                                                context,
                                                photoToSave
                                            )
                                        }
                                    } else {
                                        null
                                    }

                                onStatusChange(
                                    statusToConfirm,
                                    storedUri
                                )

                                selectedPhoto = null
                                pendingStatus = null

                            } catch (error: Exception) {
                                errorMessage =
                                    "No se pudo guardar la " +
                                        "evidencia: " +
                                        (error.message
                                            ?: "Error desconocido")
                            } finally {
                                saving = false
                            }
                        }
                    }
                ) {
                    Text(
                        if (saving)
                            "Guardando..."
                        else
                            "Confirmar"
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !saving,
                    onClick = {
                        pendingStatus = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun EventDetailField(
    label: String,
    value: String
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(4.dp)
    ) {
        Text(
            label,
            color = eventGray,
            style =
                MaterialTheme.typography.labelSmall
        )

        Text(
            value,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}
