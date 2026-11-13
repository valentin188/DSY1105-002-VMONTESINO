package com.example.modoguardian.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import com.example.modoguardian.model.*
import com.example.modoguardian.ui.state.UiState
import com.example.modoguardian.ui.utils.TurnReportPdf

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class FiltroCierre {
    TODOS,
    FALSAS_ALARMAS,
    SOLUCIONADOS,
    SIN_CLASIFICAR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    currentUser: User?,
    eventsState: UiState<List<Event>>,
    onEventClick: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme

    var search by rememberSaveable {
        mutableStateOf("")
    }

    var priorityFilter by rememberSaveable {
        mutableStateOf("Todos")
    }

    var statusFilter by rememberSaveable {
        mutableStateOf("Todos")
    }

    var closeFilter by rememberSaveable {
        mutableStateOf(FiltroCierre.TODOS)
    }

    var exporting by remember {
        mutableStateOf(false)
    }

    val events = when (eventsState) {
        is UiState.Success -> eventsState.data
        else -> emptyList()
    }

    val filteredEvents = events.filter { event ->
        val matchesSearch =
            event.titulo.contains(search, true) ||
            event.ubicacion.contains(search, true) ||
            event.descripcion.contains(search, true) ||
            event.id.contains(search, true)
        val matchesPriority =
            priorityFilter == "Todos" ||
            event.prioridad.name == priorityFilter
        val matchesStatus =
            statusFilter == "Todos" ||
            event.estado.name == statusFilter
        val matchesClose = when (closeFilter) {
            FiltroCierre.TODOS -> true
            FiltroCierre.FALSAS_ALARMAS ->
                event.motivoCierre ==
                    MotivoCierre.FALSA_ALARMA
            FiltroCierre.SOLUCIONADOS ->
                event.motivoCierre ==
                    MotivoCierre.INCIDENTE_SOLUCIONADO
            FiltroCierre.SIN_CLASIFICAR ->
                event.estado == EstadoEvento.RESUELTO &&
                    event.motivoCierre == null
        }

        matchesSearch &&
            matchesPriority &&
            matchesStatus &&
            matchesClose
    }.sortedByDescending {
        it.fechaHora
    }

    val latestUser by rememberUpdatedState(currentUser)
    val latestEvents by rememberUpdatedState(filteredEvents)
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            "application/pdf"
        )
    ) { uri ->

        if (uri != null) {
            exporting = true
            try {
                TurnReportPdf.generate(
                    context = context,
                    destination = uri,
                    user = latestUser,
                    events = latestEvents
                )

                Toast.makeText(
                    context,
                    "Reporte PDF guardado correctamente",
                    Toast.LENGTH_LONG
                ).show()

            } catch (error: Exception) {

                Toast.makeText(
                    context,
                    "No se pudo generar el PDF: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()

            } finally {
                exporting = false
            }
        }
    }

    val total = events.size
    val critical = events.count {
        it.prioridad == Prioridad.CRITICA &&
            it.estado != EstadoEvento.RESUELTO
    }

    val pending = events.count {
        it.estado == EstadoEvento.PENDIENTE
    }

    val resolved = events.count {
        it.estado == EstadoEvento.RESUELTO
    }

    val falseAlarms = events.count {
        it.motivoCierre == MotivoCierre.FALSA_ALARMA
    }

    val solved = events.count {
        it.motivoCierre ==
            MotivoCierre.INCIDENTE_SOLUCIONADO
    }

    val unclassified = events.count {
        it.estado == EstadoEvento.RESUELTO &&
            it.motivoCierre == null
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Historial de Eventos",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "INSERCODE · Registro de seguridad",
                            color = colors.onSurfaceVariant,
                            style =
                                MaterialTheme.typography.labelSmall
                        )
                    }
                },
                actions = {
                    if (currentUser?.rol == Role.ADMINISTRADOR) {
                        IconButton(
                            onClick = onNavigateToAdmin
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = "Administración"
                            )
                        }
                    }

                    IconButton(
                        onClick = onNavigateToProfile
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Mi perfil"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.onBackground,
                    actionIconContentColor = colors.onBackground
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
            item {
                Card(
                    shape = RoundedCornerShape(21.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(15.dp)
                    ) {
                        Text(
                            "RESUMEN DE EVENTOS",
                            color = colors.onSurface,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            HistoryMetric(
                                title = "TOTAL",
                                value = "$total",
                                icon = Icons.Default.Event,
                                accent = colors.primary,
                                modifier = Modifier.weight(1f)
                            )

                            HistoryMetric(
                                title = "CRÍTICAS",
                                value = "$critical",
                                icon = Icons.Default.Warning,
                                accent = colors.error,
                                modifier = Modifier.weight(1f)
                            )

                            HistoryMetric(
                                title = "PENDIENTES",
                                value = "$pending",
                                icon = Icons.Default.Schedule,
                                accent = colors.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(
                            color = colors.outlineVariant
                        )

                        Text(
                            "$resolved eventos con estado resuelto",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            "Falsas alarmas: $falseAlarms",
                            color = colors.secondary,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            "Incidentes solucionados: $solved",
                            color = colors.onSurface
                        )

                        if (unclassified > 0) {
                            Text(
                                "Cierres sin clasificar: $unclassified",
                                color = colors.tertiary
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceContainer
                    ),
                    shape = RoundedCornerShape(19.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.spacedBy(9.dp)
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = colors.primary
                            )

                            Text(
                                "REPORTE DE EVENTOS",
                                color = colors.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            "Exporta los registros que coinciden " +
                                "con los filtros seleccionados.",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            "${filteredEvents.size} eventos seleccionados",
                            color = colors.primary,
                            fontWeight = FontWeight.Medium
                        )

                        Button(
                            onClick = {
                                val timestamp = SimpleDateFormat(
                                    "yyyyMMdd_HHmm",
                                    Locale.US
                                ).format(Date())

                                pdfLauncher.launch(
                                    "INSERCODE_Reporte_$timestamp.pdf"
                                )
                            },
                            enabled =
                                eventsState is UiState.Success &&
                                    !exporting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(15.dp)
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null
                            )

                            Spacer(Modifier.width(10.dp))

                            Text(
                                if (exporting)
                                    "Generando reporte..."
                                else
                                    "Exportar reporte PDF"
                            )
                        }

                        Text(
                            "La clasificación de cierres se " +
                                "incorporará al contenido del PDF " +
                                "en el próximo paso.",
                            color = colors.onSurfaceVariant,
                            style =
                                MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Buscar evento, ubicación o código")
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

            item {
                Text(
                    "Prioridad",
                    color = colors.onSurface,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.horizontalScroll(
                        rememberScrollState()
                    ),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Todos",
                        "CRITICA",
                        "ALTA",
                        "MEDIA",
                        "BAJA"
                    ).forEach { option ->

                        FilterChip(
                            selected = priorityFilter == option,
                            onClick = { priorityFilter = option },
                            label = {
                                Text(
                                    when (option) {
                                        "CRITICA" -> "Crítica"
                                        "ALTA" -> "Alta"
                                        "MEDIA" -> "Media"
                                        "BAJA" -> "Baja"
                                        else -> "Todos"
                                    }
                                )
                            }
                        )
                    }
                }
            }

            item {
                Text(
                    "Estado",
                    color = colors.onSurface,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(6.dp))
                
                Row(
                    modifier = Modifier.horizontalScroll(
                        rememberScrollState()
                    ),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Todos",
                        "PENDIENTE",
                        "EN_PROCESO",
                        "RESUELTO"
                    ).forEach { option ->

                        FilterChip(
                            selected = statusFilter == option,
                            onClick = { statusFilter = option },
                            label = {
                                Text(
                                    when (option) {
                                        "PENDIENTE" -> "Pendiente"
                                        "EN_PROCESO" -> "En proceso"
                                        "RESUELTO" -> "Resuelto"
                                        else -> "Todos"
                                    }
                                )
                            }
                        )
                    }
                }
            }

            item {
                Text(
                    "Motivo de cierre",
                    color = colors.onSurface,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.horizontalScroll(
                        rememberScrollState()
                    ),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    FiltroCierre.entries.forEach { option ->
                        FilterChip(
                            selected = closeFilter == option,
                            onClick = {
                                closeFilter = option
                            },
                            label = {
                                Text(
                                    when (option) {
                                        FiltroCierre.TODOS ->
                                            "Todos"
                                        FiltroCierre.FALSAS_ALARMAS ->
                                            "Falsa alarma"
                                        FiltroCierre.SOLUCIONADOS ->
                                            "Solucionado"
                                        FiltroCierre.SIN_CLASIFICAR ->
                                            "Sin clasificar"
                                    }
                                )
                            }
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
                        "Eventos registrados",
                        color = colors.onBackground,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "${filteredEvents.size} resultados",
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            when (eventsState) {
                UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(26.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is UiState.Error -> {
                    item {
                        Text(
                            "No se pudieron cargar los eventos: " +
                                eventsState.message,
                            color = colors.error
                        )
                    }
                }

                is UiState.Success -> {
                    if (filteredEvents.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = colors.surfaceContainer
                                )
                            ) {
                                Text(
                                    "No hay eventos que coincidan " +
                                        "con los filtros.",
                                    modifier = Modifier.padding(24.dp),
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        val groups = filteredEvents.groupBy {
                            it.fechaHora.substringBefore(" ")
                        }

                        groups.forEach { (date, dayEvents) ->
                            item(key = "date-$date") {
                                Text(
                                    date,
                                    color = colors.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            items(
                                items = dayEvents,
                                key = { it.id }
                            ) { event ->
                                HistoryEventCard(
                                    event = event,
                                    onClick = {
                                        onEventClick(event.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryMetric(
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
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
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
private fun HistoryEventCard(
    event: Event,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val priorityColor = when (event.prioridad) {
        Prioridad.CRITICA -> colors.error
        Prioridad.ALTA -> colors.tertiary
        Prioridad.MEDIA -> colors.primary
        Prioridad.BAJA -> colors.secondary
    }

    val closeReason = when (event.motivoCierre) {
        MotivoCierre.FALSA_ALARMA ->
            "Falsa alarma"
        MotivoCierre.INCIDENTE_SOLUCIONADO ->
            "Incidente solucionado"
        null ->
            if (event.estado == EstadoEvento.RESUELTO)
                "Cierre sin clasificar"
            else
                null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceContainer
        ),
        shape = RoundedCornerShape(17.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    event.prioridad.name,
                    color = priorityColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall
                )

                Text(
                    event.estado.name.replace("_", " "),
                    color = if (event.estado == EstadoEvento.RESUELTO)
                        colors.secondary
                    else
                        colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Text(
                event.titulo,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                event.ubicacion,
                color = colors.onSurfaceVariant
            )

            Text(
                event.fechaHora,
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )

            if (closeReason != null) {
                Surface(
                    color = when (event.motivoCierre) {
                        MotivoCierre.FALSA_ALARMA ->
                            colors.tertiaryContainer

                        MotivoCierre.INCIDENTE_SOLUCIONADO ->
                            colors.secondaryContainer

                        null ->
                            colors.surfaceContainerHighest
                    },
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Text(
                        closeReason,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        color = when (event.motivoCierre) {
                            MotivoCierre.FALSA_ALARMA ->
                                colors.onTertiaryContainer

                            MotivoCierre.INCIDENTE_SOLUCIONADO ->
                                colors.onSecondaryContainer

                            null ->
                                colors.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(
                color = colors.outlineVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Ver detalle del incidente",
                    color = colors.primary,
                    style = MaterialTheme.typography.labelMedium
                )

                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant
                )
            }
        }
    }
}
