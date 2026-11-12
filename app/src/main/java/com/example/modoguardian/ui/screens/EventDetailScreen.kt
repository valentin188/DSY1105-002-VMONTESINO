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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.Event
import com.example.modoguardian.model.Prioridad
import com.example.modoguardian.ui.components.ImagenInteligente

private val detailBackground = Color(0xFF0B1426)
private val detailCard = Color(0xFF192740)
private val detailPrimary = Color(0xFF2864E8)
private val detailCritical = Color(0xFFF04452)
private val detailSuccess = Color(0xFF20C997)
private val detailSecondary = Color(0xFF9BAAC3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event?,
    onBack: () -> Unit,
    onStatusChange: (EstadoEvento, String?) -> Unit
) {
    var fotoUri by remember(event?.id) {
        mutableStateOf(
            event?.evidenciaFotoUri?.let {
                Uri.parse(it)
            }
        )
    }

    var pendingStatus by remember {
        mutableStateOf<EstadoEvento?>(null)
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            fotoUri = uri
        }
    }

    Scaffold(
        containerColor = detailBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detalle del evento",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = detailBackground
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
                Prioridad.CRITICA -> detailCritical
                Prioridad.ALTA -> Color(0xFFFFC857)
                Prioridad.MEDIA -> detailPrimary
                Prioridad.BAJA -> detailSuccess
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

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = detailCard
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
                            color = detailSecondary,
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
                            event.prioridad.name,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold
                        )

                        HorizontalDivider(
                            color = detailBackground
                        )

                        DetailInfo(
                            "Código",
                            event.id
                        )

                        DetailInfo(
                            "Ubicación",
                            event.ubicacion
                        )

                        DetailInfo(
                            "Fecha y hora",
                            event.fechaHora
                        )

                        DetailInfo(
                            "Estado actual",
                            event.estado.name.replace("_", " ")
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = detailCard
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            "DETALLES DEL INCIDENTE",
                            color = detailSecondary,
                            style =
                                MaterialTheme.typography.labelMedium
                        )

                        Text(
                            event.descripcion,
                            color = Color.White
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = detailCard
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
                            color = detailSecondary,
                            style =
                                MaterialTheme.typography.labelMedium
                        )

                        ImagenInteligente(
                            fotoUri = fotoUri
                        )

                        Text(
                            if (fotoUri == null)
                                "No hay fotografía adjunta"
                            else
                                "Fotografía seleccionada",
                            color = detailSecondary
                        )

                        OutlinedButton(
                            onClick = {
                                photoPicker.launch("image/*")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (fotoUri == null)
                                    "Seleccionar fotografía"
                                else
                                    "Cambiar fotografía"
                            )
                        }

                        Text(
                            "La evidencia se asocia al evento " +
                                "al confirmar un cambio de estado.",
                            color = detailSecondary,
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = detailCard
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
                            color = detailSecondary,
                            style =
                                MaterialTheme.typography.labelMedium
                        )

                        if (
                            event.estado ==
                            EstadoEvento.PENDIENTE
                        ) {
                            Button(
                                onClick = {
                                    pendingStatus =
                                        EstadoEvento.EN_PROCESO
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                            detailPrimary
                                    )
                            ) {
                                Text("Confirmar y poner en proceso")
                            }
                        }

                        if (
                            event.estado !=
                            EstadoEvento.RESUELTO
                        ) {
                            Button(
                                onClick = {
                                    pendingStatus =
                                        EstadoEvento.RESUELTO
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                            detailSuccess
                                    )
                            ) {
                                Text("Resolver incidente")
                            }
                        } else {
                            Text(
                                "Este incidente está resuelto.",
                                color = detailSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            "Acciones internas de demostración. " +
                                "No se envían avisos a una central.",
                            color = detailSecondary,
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    val statusToConfirm = pendingStatus

    if (
        statusToConfirm != null &&
        event != null
    ) {
        AlertDialog(
            onDismissRequest = {
                pendingStatus = null
            },
            title = {
                Text("Confirmar actualización")
            },
            text = {
                Text(
                    "¿Deseas cambiar el estado del evento " +
                        "${event.id} a " +
                        statusToConfirm.name
                            .replace("_", " ") +
                        "?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onStatusChange(
                            statusToConfirm,
                            fotoUri?.toString()
                        )
                        pendingStatus = null
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
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
private fun DetailInfo(
    label: String,
    value: String
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(4.dp)
    ) {
        Text(
            label,
            color = detailSecondary,
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            value,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}
