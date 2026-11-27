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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WarningAmber

import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.Event
import com.example.modoguardian.model.MotivoCierre
import com.example.modoguardian.model.Prioridad

import com.example.modoguardian.ui.components.ImagenInteligente
import com.example.modoguardian.ui.utils.EvidenceStorage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event?,
    onBack: () -> Unit,
    onStatusChange: (EstadoEvento, String?) -> Unit,
    onCloseWithReason:
        ((MotivoCierre, String?) -> Unit)? = null
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme

    var selectedPhoto by remember(event?.id) {
        mutableStateOf<Uri?>(null)
    }

    var pendingStatus by remember(event?.id) {
        mutableStateOf<EstadoEvento?>(null)
    }

    var savingPhoto by remember(event?.id) {
        mutableStateOf(false)
    }

    var errorMessage by remember(event?.id) {
        mutableStateOf<String?>(null)
    }

    val storedPhoto = remember(
        event?.id,
        event?.evidenciaFotoUri
    ) {
        event?.evidenciaFotoUri?.let(Uri::parse)
    }

    val previewPhoto = selectedPhoto ?: storedPhoto

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->

        if (uri != null) {
            selectedPhoto = uri
            errorMessage = null
        }
    }

    Scaffold(
        containerColor = colors.background,

        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Detalle del evento",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "INSERCODE · Gestión de incidentes",
                            color = colors.onSurfaceVariant,
                            style =
                                MaterialTheme.typography.labelSmall
                        )
                    }
                },

                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        enabled = !savingPhoto
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.onBackground,
                    navigationIconContentColor =
                        colors.onBackground
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
                    color = colors.onBackground
                )
            }

        } else {

            val priorityColor = when (event.prioridad) {
                Prioridad.CRITICA -> colors.error
                Prioridad.ALTA -> colors.tertiary
                Prioridad.MEDIA -> colors.primary
                Prioridad.BAJA -> colors.secondary
            }

            val resolved =
                event.estado == EstadoEvento.RESUELTO

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),

                verticalArrangement =
                    Arrangement.spacedBy(18.dp)
            ) {

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceContainer
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(13.dp)
                    ) {

                        Text(
                            "INSERCODE · INCIDENTE",
                            color = colors.onSurfaceVariant,
                            style =
                                MaterialTheme.typography.labelMedium
                        )

                        Text(
                            event.titulo,
                            color = colors.onSurface,
                            style =
                                MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "PRIORIDAD ${event.prioridad.name}",
                            color = priorityColor,
                            fontWeight = FontWeight.Bold
                        )

                        HorizontalDivider()

                        DetailInformation(
                            "Código del evento",
                            event.id
                        )

                        DetailInformation(
                            "Ubicación",
                            event.ubicacion
                        )

                        DetailInformation(
                            "Fecha y hora",
                            event.fechaHora
                        )

                        DetailInformation(
                            "Estado",
                            event.estado.name.replace("_", " ")
                        )
                    }
                }

                if (resolved) {

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor =
                                colors.secondaryContainer
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {

                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(10.dp)
                            ) {

                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = colors.onSecondaryContainer
                                )

                                Text(
                                    "EVENTO RESUELTO",
                                    color = colors.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                when (event.motivoCierre) {

                                    MotivoCierre.FALSA_ALARMA ->
                                        "Motivo: falsa alarma"

                                    MotivoCierre.INCIDENTE_SOLUCIONADO ->
                                        "Motivo: incidente solucionado"

                                    null ->
                                        "Motivo de cierre no registrado"
                                },

                                color = colors.onSecondaryContainer,
                                style =
                                    MaterialTheme.typography.bodyMedium
                            )

                            if (event.motivoCierre == null) {
                                Text(
                                    "Este evento pudo haberse cerrado " +
                                        "antes de implementar la " +
                                        "clasificación de motivos.",
                                    color = colors.onSecondaryContainer,
                                    style =
                                        MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceContainer
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
                            color = colors.onSurfaceVariant,
                            style =
                                MaterialTheme.typography.labelMedium
                        )

                        Text(
                            event.descripcion,
                            color = colors.onSurface,
                            style =
                                MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceContainer
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
                            color = colors.onSurfaceVariant,
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

                                storedPhoto != null ->
                                    "Evidencia registrada"

                                else ->
                                    "Sin fotografía adjunta"
                            },

                            color = colors.onSurfaceVariant,
                            style =
                                MaterialTheme.typography.bodySmall
                        )

                        if (!resolved) {

                            OutlinedButton(
                                onClick = {
                                    photoPicker.launch("image/*")
                                },

                                enabled = !savingPhoto,
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
                                    "La fotografía se copiará al " +
                                        "almacenamiento interno cuando " +
                                        "confirmes una acción.",
                                    color = colors.onSurfaceVariant,
                                    style =
                                        MaterialTheme.typography.bodySmall
                                )

                                TextButton(
                                    onClick = {
                                        selectedPhoto = null
                                    },

                                    enabled = !savingPhoto
                                ) {
                                    Text("Cancelar selección")
                                }
                            }
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceContainer
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
                            color = colors.onSurfaceVariant,
                            style =
                                MaterialTheme.typography.labelMedium
                        )

                        if (event.estado == EstadoEvento.PENDIENTE) {

                            Button(
                                onClick = {
                                    pendingStatus =
                                        EstadoEvento.EN_PROCESO
                                },

                                enabled = !savingPhoto,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Confirmar alerta")
                            }
                        }

                        if (!resolved) {

                            Button(
                                onClick = {
                                    pendingStatus =
                                        EstadoEvento.RESUELTO
                                },

                                enabled =
                                    !savingPhoto &&
                                        onCloseWithReason != null,

                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Resolver incidente")
                            }

                            if (onCloseWithReason == null) {
                                Text(
                                    "La resolución con motivo de " +
                                        "cierre se habilitará al " +
                                        "conectar MainActivity.",
                                    color = colors.onSurfaceVariant,
                                    style =
                                        MaterialTheme.typography.bodySmall
                                )
                            }

                        } else {

                            Text(
                                "Este evento ya está cerrado.",
                                color = colors.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (savingPhoto) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                "Preparando evidencia...",
                                color = colors.onSurfaceVariant
                            )
                        }

                        errorMessage?.let { message ->
                            Text(
                                message,
                                color = colors.error
                            )
                        }

                        Text(
                            "La evidencia se guarda " +
                                "localmente en el dispositivo.",
                            color = colors.onSurfaceVariant,
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    val statusToConfirm = pendingStatus
    val currentEvent = event

    if (statusToConfirm != null && currentEvent != null) {

        AlertDialog(
            onDismissRequest = {
                if (!savingPhoto) {
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
                        if (statusToConfirm == EstadoEvento.RESUELTO) {

                            "¿Deseas resolver el evento " +
                                "${currentEvent.id} como " +
                                "INCIDENTE SOLUCIONADO?"

                        } else {

                            "¿Deseas confirmar la alerta " +
                                "${currentEvent.id} y " +
                                "marcarla como EN PROCESO?"
                        }
                    )

                    if (selectedPhoto != null) {
                        Text(
                            "También se copiará la fotografía " +
                                "seleccionada como evidencia.",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },

            confirmButton = {
                TextButton(
                    enabled = !savingPhoto,

                    onClick = {

                        scope.launch {

                            savingPhoto = true
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

                                if (
                                    statusToConfirm ==
                                    EstadoEvento.RESUELTO
                                ) {

                                    onCloseWithReason?.invoke(
                                        MotivoCierre.INCIDENTE_SOLUCIONADO,
                                        storedUri
                                    )

                                } else {

                                    onStatusChange(
                                        statusToConfirm,
                                        storedUri
                                    )
                                }

                                selectedPhoto = null
                                pendingStatus = null

                            } catch (error: Exception) {

                                errorMessage =
                                    "No se pudo preparar la " +
                                        "evidencia: " +
                                        (error.message
                                            ?: "Error desconocido")

                            } finally {

                                savingPhoto = false
                            }
                        }
                    }
                ) {
                    Text(
                        if (savingPhoto)
                            "Preparando..."
                        else
                            "Confirmar"
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        pendingStatus = null
                    },
                    enabled = !savingPhoto
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun DetailInformation(
    label: String,
    value: String
) {
    val colors = MaterialTheme.colorScheme

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        Text(
            label,
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )

        Text(
            value,
            color = colors.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
