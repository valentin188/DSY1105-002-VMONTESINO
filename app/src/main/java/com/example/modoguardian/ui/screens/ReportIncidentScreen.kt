package com.example.modoguardian.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import com.example.modoguardian.model.Event
import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.Prioridad
import com.example.modoguardian.model.Role
import com.example.modoguardian.model.User

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportIncidentScreen(
    currentUser: User?,
    saving: Boolean,
    operationError: String?,
    operationMessage: String?,
    onBack: () -> Unit,
    onSave: (Event, User) -> Unit
) {
    val context = LocalContext.current

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var prioridad by remember {
        mutableStateOf(Prioridad.MEDIA)
    }

    var fotoUri by remember { mutableStateOf<String?>(null) }
    var videoUri by remember { mutableStateOf<String?>(null) }

    var prioridadMenu by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val fotoLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                }

                fotoUri = uri.toString()
            }
        }

    val videoLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                }

                videoUri = uri.toString()
            }
        }

    if (currentUser?.rol != Role.SUPERVISOR) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Nuevo reporte") },
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
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Acceso denegado",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    "Solo el supervisor puede crear reportes."
                )
            }
        }

        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Crear reporte de incidente")
                },
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
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Text(
                "Información del incidente",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título del incidente") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = ubicacion,
                onValueChange = { ubicacion = it },
                label = { Text("Ubicación") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = prioridadMenu,
                onExpandedChange = {
                    prioridadMenu = !prioridadMenu
                }
            ) {
                OutlinedTextField(
                    value = prioridad.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Prioridad") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = prioridadMenu
                        )
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = prioridadMenu,
                    onDismissRequest = {
                        prioridadMenu = false
                    }
                ) {
                    Prioridad.values().forEach { opcion ->
                        DropdownMenuItem(
                            text = {
                                Text(opcion.name)
                            },
                            onClick = {
                                prioridad = opcion
                                prioridadMenu = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider()

            Text(
                "Evidencia del incidente",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedButton(
                onClick = {
                    fotoLauncher.launch(arrayOf("image/*"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text("Adjuntar fotografía")
            }

            if (fotoUri != null) {
                Text(
                    "Fotografía adjunta",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedButton(
                onClick = {
                    videoLauncher.launch(arrayOf("video/*"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Videocam,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text("Adjuntar video")
            }

            if (videoUri != null) {
                Text(
                    "Video adjunto",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                "El reporte quedará registrado como PENDIENTE.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            validationError?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            operationError?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            operationMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = {
                    if (
                        titulo.isBlank() ||
                        descripcion.isBlank() ||
                        ubicacion.isBlank()
                    ) {
                        validationError =
                            "Completa el título, descripción y ubicación."
                    } else {
                        validationError = null

                        val fecha = SimpleDateFormat(
                            "yyyy-MM-dd HH:mm",
                            Locale.getDefault()
                        ).format(Date())

                        val nuevoEvento = Event(
                            id = UUID.randomUUID().toString(),
                            titulo = titulo.trim(),
                            descripcion = descripcion.trim(),
                            ubicacion = ubicacion.trim(),
                            prioridad = prioridad,
                            estado = EstadoEvento.PENDIENTE,
                            fechaHora = fecha,
                            evidenciaFotoUri = fotoUri,
                            evidenciaVideoUri = videoUri,
                            reportadoPorId = currentUser.id,
                            reportadoPorNombre = currentUser.nombre
                        )

                        onSave(nuevoEvento, currentUser)
                    }
                },
                enabled = !saving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Registrar reporte")
                }
            }
        }
    }
}
