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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

private val bg = Color(0xFF0B1426)
private val cardBg = Color(0xFF1A2943)
private val blue = Color(0xFF2864E8)
private val gray = Color(0xFF9BAAC3)
private val red = Color(0xFFFF5252)
private val green = Color(0xFF20C997)
private val yellow = Color(0xFFFFC857)

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

    var search by remember {
        mutableStateOf("")
    }

    var priorityFilter by remember {
        mutableStateOf("Todos")
    }

    var statusFilter by remember {
        mutableStateOf("Todos")
    }

    var exporting by remember {
        mutableStateOf(false)
    }

    val events = when (eventsState) {
        is UiState.Success -> eventsState.data
        else -> emptyList()
    }

    val filteredEvents = remember(
        events,
        search,
        priorityFilter,
        statusFilter
    ) {
        events.filter { event ->

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

            matchesSearch &&
                matchesPriority &&
                matchesStatus

        }.sortedByDescending {
            it.fechaHora
        }
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
                    "Error al generar PDF: ${error.message}",
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

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Historial",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "INSERCODE · Registro de seguridad",
                            color = gray,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                actions = {

                    if (
                        currentUser?.rol ==
                        Role.ADMINISTRADOR
                    ) {
                        IconButton(
                            onClick = onNavigateToAdmin
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = "Administración",
                                tint = Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = onNavigateToProfile
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Mi perfil",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bg,
                    titleContentColor = Color.White
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

            // RESUMEN GENERAL
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = cardBg
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        Text(
                            "RESUMEN DE EVENTOS",
                            color = gray,
                            style = MaterialTheme.typography.labelMedium
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            EventMetric(
                                title = "TOTAL",
                                value = "$total",
                                color = blue,
                                modifier = Modifier.weight(1f)
                            )

                            EventMetric(
                                title = "CRÍTICAS",
                                value = "$critical",
                                color = red,
                                modifier = Modifier.weight(1f)
                            )

                            EventMetric(
                                title = "PENDIENTES",
                                value = "$pending",
                                color = yellow,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text(
                            "$resolved eventos resueltos",
                            color = green
                        )
                    }
                }
            }

            // EXPORTACIÓN PDF
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardBg
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            "REPORTE DE TURNO",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Exporta los eventos que coincidan " +
                                "con los filtros seleccionados.",
                            color = gray,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            "${filteredEvents.size} eventos para exportar",
                            color = blue,
                            fontWeight = FontWeight.Medium
                        )

                        Button(
                            onClick = {

                                val timestamp =
                                    SimpleDateFormat(
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = blue
                            )
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
                            "Buscar evento, ubicación o código"
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

            // PRIORIDAD
            item {
                Text(
                    "Filtrar por prioridad",
                    color = gray
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.horizontalScroll(
                        rememberScrollState()
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            onClick = {
                                priorityFilter = option
                            },
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

            // ESTADO
            item {
                Text(
                    "Filtrar por estado",
                    color = gray
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.horizontalScroll(
                        rememberScrollState()
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Todos",
                        "PENDIENTE",
                        "EN_PROCESO",
                        "RESUELTO"
                    ).forEach { option ->

                        FilterChip(
                            selected = statusFilter == option,
                            onClick = {
                                statusFilter = option
                            },
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

            // RESULTADOS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Eventos registrados",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "${filteredEvents.size} resultados",
                        color = gray
                    )
                }
            }

            when (eventsState) {

                UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = blue
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    item {
                        Text(
                            "No se pudieron cargar los eventos: " +
                                eventsState.message,
                            color = red
                        )
                    }
                }

                is UiState.Success -> {

                    if (filteredEvents.isEmpty()) {

                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = cardBg
                                )
                            ) {
                                Text(
                                    "No hay eventos que coincidan " +
                                        "con los filtros seleccionados.",
                                    modifier = Modifier.padding(24.dp),
                                    color = gray
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
                                    color = gray,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            items(
                                dayEvents,
                                key = { it.id }
                            ) { event ->

                                val priorityColor = when (
                                    event.prioridad
                                ) {
                                    Prioridad.CRITICA -> red
                                    Prioridad.ALTA -> yellow
                                    Prioridad.MEDIA -> blue
                                    Prioridad.BAJA -> green
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onEventClick(event.id)
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = cardBg
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {

                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement =
                                            Arrangement.spacedBy(8.dp)
                                    ) {

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement =
                                                Arrangement.SpaceBetween
                                        ) {

                                            Text(
                                                event.prioridad.name,
                                                color = priorityColor,
                                                fontWeight = FontWeight.Bold,
                                                style =
                                                    MaterialTheme.typography.labelSmall
                                            )

                                            Text(
                                                event.estado.name.replace(
                                                    "_",
                                                    " "
                                                ),
                                                color = if (
                                                    event.estado ==
                                                    EstadoEvento.RESUELTO
                                                ) green else gray,
                                                style =
                                                    MaterialTheme.typography.labelSmall
                                            )
                                        }

                                        Text(
                                            event.titulo,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            event.ubicacion,
                                            color = gray
                                        )

                                        Text(
                                            event.fechaHora,
                                            color = gray,
                                            style =
                                                MaterialTheme.typography.bodySmall
                                        )

                                        HorizontalDivider(
                                            color = bg
                                        )

                                        Text(
                                            "Ver detalle del incidente",
                                            color = blue,
                                            style =
                                                MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventMetric(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                bg,
                RoundedCornerShape(12.dp)
            )
            .padding(10.dp)
    ) {

        Text(
            title,
            color = gray,
            style = MaterialTheme.typography.labelSmall
        )

        Text(
            value,
            color = color,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
