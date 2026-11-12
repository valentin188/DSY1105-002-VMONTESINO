package com.example.modoguardian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.modoguardian.ui.state.UiState

private val alertsBg = Color(0xFF0B1426)
private val alertsSurface = Color(0xFF192740)
private val alertsBlue = Color(0xFF2864E8)
private val alertsRed = Color(0xFFF04452)
private val alertsGreen = Color(0xFF20C997)
private val alertsYellow = Color(0xFFFFC857)
private val alertsGray = Color(0xFF9BAAC3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    eventsState: UiState<List<Event>>,
    onBack: () -> Unit,
    onStatusChange: (String, EstadoEvento) -> Unit
) {
    var selectedEventId by remember {
        mutableStateOf<String?>(null)
    }

    var pendingAction by remember {
        mutableStateOf<EstadoEvento?>(null)
    }

    var showResolved by remember {
        mutableStateOf(false)
    }

    val events = when (eventsState) {
        is UiState.Success -> eventsState.data
        else -> emptyList()
    }

    val filteredEvents = events
        .filter {
            it.prioridad == Prioridad.CRITICA ||
                it.prioridad == Prioridad.ALTA
        }
        .filter {
            showResolved ||
                it.estado != EstadoEvento.RESUELTO
        }
        .sortedWith(
            compareBy<Event> {
                if (it.prioridad == Prioridad.CRITICA) 0 else 1
            }.thenByDescending { it.fechaHora }
        )

    val selectedEvent = events.find {
        it.id == selectedEventId
    }

    Scaffold(
        containerColor = alertsBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Alertas de seguridad",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Volver", color = alertsBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = alertsBg
                )
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = alertsSurface
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "CENTRO DE ALERTAS",
                            color = alertsGray,
                            style =
                                MaterialTheme.typography.labelMedium
                        )

                        Text(
                            "Monitoreo de incidentes",
                            color = Color.White,
                            style =
                                MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        val criticalCount = events.count {
                            it.prioridad == Prioridad.CRITICA &&
                                it.estado != EstadoEvento.RESUELTO
                        }

                        val pendingCount = events.count {
                            it.estado == EstadoEvento.PENDIENTE &&
                                (
                                    it.prioridad == Prioridad.CRITICA ||
                                    it.prioridad == Prioridad.ALTA
                                )
                        }

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {
                            AlertMetric(
                                "CRÍTICAS",
                                criticalCount.toString(),
                                alertsRed,
                                Modifier.weight(1f)
                            )

                            AlertMetric(
                                "PENDIENTES",
                                pendingCount.toString(),
                                alertsYellow,
                                Modifier.weight(1f)
                            )
                        }

                        Text(
                            "Datos de demostración. " +
                                "Sin conexión a una central real.",
                            color = alertsGray,
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Incidentes",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Ver resueltos",
                            color = alertsGray,
                            style =
                                MaterialTheme.typography.labelMedium
                        )

                        Switch(
                            checked = showResolved,
                            onCheckedChange = {
                                showResolved = it
                            }
                        )
                    }
                }
            }

            when (eventsState) {
                UiState.Loading -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = alertsBlue
                        )
                    }
                }

                is UiState.Error -> item {
                    Text(
                        "Error: ${eventsState.message}",
                        color = alertsRed
                    )
                }

                is UiState.Success -> {
                    if (filteredEvents.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = alertsSurface
                                )
                            ) {
                                Text(
                                    "No hay alertas para mostrar.",
                                    modifier =
                                        Modifier.padding(24.dp),
                                    color = alertsGray
                                )
                            }
                        }
                    } else {
                        items(
                            filteredEvents,
                            key = { it.id }
                        ) { event ->

                            val severityColor =
                                if (
                                    event.prioridad ==
                                    Prioridad.CRITICA
                                ) alertsRed
                                else alertsYellow

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedEventId = event.id
                                    },
                                shape =
                                    RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = alertsSurface
                                )
                            ) {
                                Column(
                                    modifier =
                                        Modifier.padding(18.dp),
                                    verticalArrangement =
                                        Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier =
                                            Modifier.fillMaxWidth(),
                                        horizontalArrangement =
                                            Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            event.prioridad.name,
                                            color = severityColor,
                                            fontWeight =
                                                FontWeight.Bold
                                        )

                                        Text(
                                            event.estado.name
                                                .replace("_", " "),
                                            color = alertsGray
                                        )
                                    }

                                    Text(
                                        event.titulo,
                                        color = Color.White,
                                        style =
                                            MaterialTheme.typography
                                                .titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        event.ubicacion,
                                        color = alertsGray
                                    )

                                    Text(
                                        event.fechaHora,
                                        color = alertsGray,
                                        style =
                                            MaterialTheme.typography
                                                .bodySmall
                                    )

                                    HorizontalDivider(
                                        color = alertsBg
                                    )

                                    Text(
                                        "Ver detalles y acciones",
                                        color = alertsBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedEvent != null) {
        val event = selectedEvent

        AlertDialog(
            onDismissRequest = {
                selectedEventId = null
            },
            containerColor = alertsSurface,
            titleContentColor = Color.White,
            textContentColor = alertsGray,
            title = {
                Column {
                    Text(
                        if (event.prioridad == Prioridad.CRITICA)
                            "ALERTA CRÍTICA"
                        else "ALERTA ALTA",
                        color = if (
                            event.prioridad == Prioridad.CRITICA
                        ) alertsRed else alertsYellow,
                        style =
                            MaterialTheme.typography.labelLarge
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(event.titulo)
                }
            },
            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    Text(event.descripcion)
                    Text("Ubicación: ${event.ubicacion}")
                    Text("Fecha: ${event.fechaHora}")
                    Text("Código: ${event.id}")

                    Text(
                        "Estado: ${event.estado.name.replace("_", " ")}",
                        color = alertsBlue
                    )

                    if (
                        event.estado != EstadoEvento.RESUELTO
                    ) {
                        Button(
                            onClick = {
                                pendingAction =
                                    EstadoEvento.EN_PROCESO
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled =
                                event.estado ==
                                    EstadoEvento.PENDIENTE,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = alertsRed
                                )
                        ) {
                            Text("Confirmar alerta")
                        }

                        OutlinedButton(
                            onClick = {
                                pendingAction =
                                    EstadoEvento.RESUELTO
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Marcar como falsa alarma")
                        }
                    } else {
                        Text(
                            "Incidente resuelto",
                            color = alertsGreen
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedEventId = null
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }

    val eventToUpdate = selectedEvent
    val actionToConfirm = pendingAction

    if (
        eventToUpdate != null &&
        actionToConfirm != null
    ) {
        AlertDialog(
            onDismissRequest = {
                pendingAction = null
            },
            title = {
                Text("Confirmar acción")
            },
            text = {
                Text(
                    if (
                        actionToConfirm ==
                        EstadoEvento.EN_PROCESO
                    ) {
                        "¿Deseas confirmar esta alerta y " +
                            "marcarla como en proceso?"
                    } else {
                        "¿Deseas cerrar este incidente " +
                            "como falsa alarma de demostración?"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onStatusChange(
                            eventToUpdate.id,
                            actionToConfirm
                        )
                        pendingAction = null
                        selectedEventId = null
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingAction = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun AlertMetric(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                alertsBg,
                RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            title,
            color = alertsGray,
            style = MaterialTheme.typography.labelSmall
        )

        Text(
            value,
            color = color,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
