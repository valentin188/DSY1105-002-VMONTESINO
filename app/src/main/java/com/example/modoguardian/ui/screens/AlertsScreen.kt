package com.example.modoguardian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.Event
import com.example.modoguardian.model.Prioridad
import com.example.modoguardian.ui.state.UiState

private enum class AlertFilter {
    ACTIVAS,
    CRITICAS,
    PENDIENTES,
    TODAS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    eventsState: UiState<List<Event>>,
    onBack: () -> Unit,
    onStatusChange: (String, EstadoEvento) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    var search by rememberSaveable {
        mutableStateOf("")
    }

    var filter by rememberSaveable {
        mutableStateOf(AlertFilter.ACTIVAS)
    }

    var selectedEventId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var pendingStatus by remember {
        mutableStateOf<EstadoEvento?>(null)
    }

    var showFalseAlarmInfo by remember {
        mutableStateOf(false)
    }

    val events = when (eventsState) {
        is UiState.Success -> eventsState.data
        else -> emptyList()
    }

    // Alertas de prioridad crítica o alta.
    val alerts = events.filter {
        it.prioridad == Prioridad.CRITICA ||
            it.prioridad == Prioridad.ALTA
    }

    val criticalCount = alerts.count {
        it.prioridad == Prioridad.CRITICA &&
            it.estado != EstadoEvento.RESUELTO
    }

    val pendingCount = alerts.count {
        it.estado == EstadoEvento.PENDIENTE
    }

    val activeCount = alerts.count {
        it.estado != EstadoEvento.RESUELTO
    }

    val resolvedCount = alerts.count {
        it.estado == EstadoEvento.RESUELTO
    }

    val filteredAlerts = alerts
        .filter { event ->
            val matchesSearch =
                event.titulo.contains(search, true) ||
                event.ubicacion.contains(search, true) ||
                event.descripcion.contains(search, true) ||
                event.id.contains(search, true)

            val matchesFilter = when (filter) {
                AlertFilter.ACTIVAS ->
                    event.estado != EstadoEvento.RESUELTO

                AlertFilter.CRITICAS ->
                    event.prioridad == Prioridad.CRITICA &&
                        event.estado != EstadoEvento.RESUELTO

                AlertFilter.PENDIENTES ->
                    event.estado == EstadoEvento.PENDIENTE

                AlertFilter.TODAS -> true
            }

            matchesSearch && matchesFilter
        }
        .sortedWith(
            compareBy<Event> {
                if (it.estado == EstadoEvento.RESUELTO)
                    1 else 0
            }.thenBy {
                if (it.prioridad == Prioridad.CRITICA)
                    0 else 1
            }.thenByDescending {
                it.fechaHora
            }
        )

    val selectedEvent = alerts.find {
        it.id == selectedEventId
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Alertas",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "INSERCODE · Centro de seguridad",
                            color = colors.onSurfaceVariant,
                            style =
                                MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // PANEL DE RESUMEN
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceContainer
                    ),
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 3.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(17.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "Centro de Alertas",
                                    color = colors.onSurface,
                                    style =
                                        MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    "INCIDENTES DE SEGURIDAD",
                                    color = colors.onSurfaceVariant,
                                    style =
                                        MaterialTheme.typography.labelSmall
                                )
                            }

                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            AlertMetricCard(
                                title = "CRÍTICAS",
                                value = "$criticalCount",
                                icon = Icons.Default.ErrorOutline,
                                accent = colors.error,
                                modifier = Modifier.weight(1f)
                            )

                            AlertMetricCard(
                                title = "PENDIENTES",
                                value = "$pendingCount",
                                icon = Icons.Default.Schedule,
                                accent = colors.tertiary,
                                modifier = Modifier.weight(1f)
                            )

                            AlertMetricCard(
                                title = "ACTIVAS",
                                value = "$activeCount",
                                icon = Icons.Default.NotificationsActive,
                                accent = colors.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(
                            color = colors.outlineVariant
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = colors.secondary,
                                modifier = Modifier.size(18.dp)
                            )

                            Text(
                                "$resolvedCount incidentes resueltos",
                                color = colors.onSurfaceVariant,
                                style =
                                    MaterialTheme.typography.bodySmall
                            )
                        }

                        Text(
                            "Datos locales de demostración. " +
                                "Sin conexión a una central real.",
                            color = colors.onSurfaceVariant,
                            style =
                                MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // BUSCADOR
            item {
                OutlinedTextField(
                    value = search,
                    onValueChange = {
                        search = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Buscar alerta, ubicación o código"
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // FILTROS
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected =
                            filter == AlertFilter.ACTIVAS,
                        onClick = {
                            filter = AlertFilter.ACTIVAS
                        },
                        label = {
                            Text("Activas")
                        }
                    )

                    FilterChip(
                        selected =
                            filter == AlertFilter.CRITICAS,
                        onClick = {
                            filter = AlertFilter.CRITICAS
                        },
                        label = {
                            Text("Críticas")
                        }
                    )

                    FilterChip(
                        selected =
                            filter == AlertFilter.PENDIENTES,
                        onClick = {
                            filter = AlertFilter.PENDIENTES
                        },
                        label = {
                            Text("Pendientes")
                        }
                    )

                    FilterChip(
                        selected =
                            filter == AlertFilter.TODAS,
                        onClick = {
                            filter = AlertFilter.TODAS
                        },
                        label = {
                            Text("Todas")
                        }
                    )
                }
            }

            // RESULTADOS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Incidentes",
                        color = colors.onBackground,
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "${filteredAlerts.size} resultados",
                        color = colors.onSurfaceVariant,
                        style =
                            MaterialTheme.typography.bodySmall
                    )
                }
            }

            when (eventsState) {

                UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = colors.primary
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor =
                                    colors.errorContainer
                            )
                        ) {
                            Text(
                                "No se pudieron cargar " +
                                    "las alertas: ${eventsState.message}",
                                modifier = Modifier.padding(18.dp),
                                color = colors.onErrorContainer
                            )
                        }
                    }
                }

                is UiState.Success -> {

                    if (filteredAlerts.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor =
                                        colors.surfaceContainer
                                ),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment =
                                        Alignment.CenterHorizontally,
                                    verticalArrangement =
                                        Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = colors.secondary,
                                        modifier = Modifier.size(38.dp)
                                    )

                                    Text(
                                        "No hay alertas para mostrar",
                                        color = colors.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        "Prueba con otros filtros " +
                                            "o revisa el historial.",
                                        color = colors.onSurfaceVariant,
                                        style =
                                            MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    } else {
                        items(
                            items = filteredAlerts,
                            key = { it.id }
                        ) { event ->

                            AlertEventCard(
                                event = event,
                                onClick = {
                                    selectedEventId = event.id
                                }
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "INSERCODE · Gestión local de alertas",
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.onSurfaceVariant,
                    style =
                        MaterialTheme.typography.labelSmall,
                    textAlign =
                        androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    // DETALLE DE ALERTA
    if (selectedEvent != null && pendingStatus == null &&
        !showFalseAlarmInfo
    ) {
        val event = selectedEvent

        AlertDialog(
            onDismissRequest = {
                selectedEventId = null
            },
            icon = {
                Icon(
                    Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = if (
                        event.prioridad == Prioridad.CRITICA
                    ) colors.error else colors.tertiary
                )
            },
            title = {
                Text(
                    if (event.prioridad == Prioridad.CRITICA)
                        "Alerta crítica"
                    else
                        "Alerta de prioridad alta"
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        event.titulo,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )

                    Text(event.descripcion)

                    HorizontalDivider()

                    AlertDetailLine(
                        "Código",
                        event.id
                    )

                    AlertDetailLine(
                        "Ubicación",
                        event.ubicacion
                    )

                    AlertDetailLine(
                        "Fecha",
                        event.fechaHora
                    )

                    AlertDetailLine(
                        "Estado",
                        event.estado.name.replace("_", " ")
                    )

                    HorizontalDivider()

                    if (event.estado == EstadoEvento.PENDIENTE) {
                        Button(
                            onClick = {
                                pendingStatus =
                                    EstadoEvento.EN_PROCESO
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Confirmar alerta")
                        }
                    }

                    if (event.estado != EstadoEvento.RESUELTO) {
                        Button(
                            onClick = {
                                pendingStatus =
                                    EstadoEvento.RESUELTO
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.secondary,
                                contentColor = colors.onSecondary
                            )
                        ) {
                            Text("Resolver incidente")
                        }

                        OutlinedButton(
                            onClick = {
                                showFalseAlarmInfo = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Falsa alarma")
                        }
                    } else {
                        Text(
                            "Incidente resuelto",
                            color = colors.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        "Las actualizaciones modifican " +
                            "el estado local del evento.",
                        color = colors.onSurfaceVariant,
                        style =
                            MaterialTheme.typography.labelSmall
                    )
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

    // CONFIRMAR CAMBIO DE ESTADO
    val action = pendingStatus
    val eventToUpdate = selectedEvent

    if (action != null && eventToUpdate != null) {
        AlertDialog(
            onDismissRequest = {
                pendingStatus = null
            },
            title = {
                Text("Confirmar acción")
            },
            text = {
                Text(
                    if (action == EstadoEvento.EN_PROCESO) {
                        "¿Deseas confirmar la alerta " +
                            "${eventToUpdate.id} y marcarla " +
                            "como en proceso?"
                    } else {
                        "¿Deseas marcar el incidente " +
                            "${eventToUpdate.id} como resuelto?"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onStatusChange(
                            eventToUpdate.id,
                            action
                        )

                        pendingStatus = null
                        selectedEventId = null
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

    // FALSA ALARMA: REQUIERE MOTIVO DE CIERRE
    if (showFalseAlarmInfo) {
        AlertDialog(
            onDismissRequest = {
                showFalseAlarmInfo = false
            },
            title = {
                Text("Clasificación de falsa alarma")
            },
            text = {
                Text(
                    "El modelo actual de eventos solo " +
                        "almacena el estado. Para registrar " +
                        "correctamente una falsa alarma, " +
                        "necesitamos agregar un motivo de " +
                        "cierre a Room.\n\n" +
                        "Por ahora puedes resolver el " +
                        "incidente, pero no quedará " +
                        "clasificado como falsa alarma."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFalseAlarmInfo = false
                    }
                ) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
private fun AlertMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .background(
                colors.surfaceContainerHighest,
                RoundedCornerShape(13.dp)
            )
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(19.dp)
        )

        Text(
            title,
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )

        Text(
            value,
            color = accent,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AlertEventCard(
    event: Event,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    val accent = when (event.prioridad) {
        Prioridad.CRITICA -> colors.error
        Prioridad.ALTA -> colors.tertiary
        Prioridad.MEDIA -> colors.primary
        Prioridad.BAJA -> colors.secondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceContainer
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        accent.copy(alpha = 0.13f),
                        RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = accent
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        event.prioridad.name,
                        color = accent,
                        fontWeight = FontWeight.Bold,
                        style =
                            MaterialTheme.typography.labelSmall
                    )

                    Text(
                        event.estado.name.replace("_", " "),
                        color = if (
                            event.estado == EstadoEvento.RESUELTO
                        ) colors.secondary
                        else colors.onSurfaceVariant,
                        style =
                            MaterialTheme.typography.labelSmall
                    )
                }

                Text(
                    event.titulo,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    style =
                        MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    event.ubicacion,
                    color = colors.onSurfaceVariant,
                    style =
                        MaterialTheme.typography.bodySmall
                )

                Text(
                    event.fechaHora,
                    color = colors.onSurfaceVariant,
                    style =
                        MaterialTheme.typography.labelSmall
                )

                HorizontalDivider(
                    color = colors.outlineVariant
                )

                Text(
                    "Ver detalles y acciones",
                    color = colors.primary,
                    style =
                        MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun AlertDetailLine(
    label: String,
    value: String
) {
    val colors = MaterialTheme.colorScheme

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
