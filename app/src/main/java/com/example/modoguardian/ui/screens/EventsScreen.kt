package com.example.modoguardian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.*
import com.example.modoguardian.ui.state.UiState

private val historyBg = Color(0xFF0B1426)
private val historyCard = Color(0xFF1A2943)
private val historyBlue = Color(0xFF2864E8)
private val historyGray = Color(0xFF9BAAC3)
private val historyRed = Color(0xFFFF5252)
private val historyGreen = Color(0xFF20C997)
private val historyYellow = Color(0xFFFFC857)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    currentUser: User?,
    eventsState: UiState<List<Event>>,
    onEventClick: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    var priorityFilter by remember {
        mutableStateOf("Todos")
    }
    var statusFilter by remember {
        mutableStateOf("Todos")
    }

    val events = when (eventsState) {
        is UiState.Success -> eventsState.data
        else -> emptyList()
    }

    val filteredEvents = remember(
        events, search, priorityFilter, statusFilter
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
        }.sortedByDescending { it.fechaHora }
    }

    val total = events.size
    val critical = events.count {
        it.prioridad == Prioridad.CRITICA &&
        it.estado != EstadoEvento.RESUELTO
    }
    val pending = events.count {
        it.estado == EstadoEvento.PENDIENTE
    }

    Scaffold(
        containerColor = historyBg,
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
                            color = historyGray,
                            style =
                                MaterialTheme.typography.labelSmall
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
                                contentDescription =
                                    "Administración",
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
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = historyBg,
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
            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = historyCard
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "RESUMEN DE EVENTOS",
                            color = historyGray,
                            style =
                                MaterialTheme.typography.labelSmall
                        )

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            HistoryMetric(
                                "REGISTRADOS",
                                "$total",
                                historyBlue,
                                Modifier.weight(1f)
                            )

                            HistoryMetric(
                                "CRÍTICAS",
                                "$critical",
                                historyRed,
                                Modifier.weight(1f)
                            )

                            HistoryMetric(
                                "PENDIENTES",
                                "$pending",
                                historyYellow,
                                Modifier.weight(1f)
                            )
                        }

                        Text(
                            "Información del historial disponible",
                            color = historyGray,
                            style =
                                MaterialTheme.typography.bodySmall
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

            item {
                Text(
                    "Filtrar por prioridad",
                    color = historyGray,
                    style =
                        MaterialTheme.typography.labelMedium
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Todos", "CRITICA", "ALTA"
                    ).forEach { option ->
                        FilterChip(
                            selected =
                                priorityFilter == option,
                            onClick = {
                                priorityFilter = option
                            },
                            label = {
                                Text(
                                    if (option == "CRITICA")
                                        "Críticas"
                                    else if (option == "ALTA")
                                        "Altas"
                                    else "Todos"
                                )
                            }
                        )
                    }
                }
            }

            item {
                Text(
                    "Filtrar por estado",
                    color = historyGray,
                    style =
                        MaterialTheme.typography.labelMedium
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Todos",
                        "PENDIENTE",
                        "EN_PROCESO",
                        "RESUELTO"
                    ).forEach { option ->
                        FilterChip(
                            selected =
                                statusFilter == option,
                            onClick = {
                                statusFilter = option
                            },
                            label = {
                                Text(
                                    when (option) {
                                        "PENDIENTE" -> "Pend."
                                        "EN_PROCESO" -> "Proceso"
                                        "RESUELTO" -> "Resuelto"
                                        else -> "Todos"
                                    },
                                    style = MaterialTheme
                                        .typography.labelSmall
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
                        Arrangement.SpaceBetween
                ) {
                    Text(
                        "Eventos registrados",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${filteredEvents.size} resultados",
                        color = historyGray
                    )
                }
            }

            when (eventsState) {
                UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            contentAlignment =
                                Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = historyBlue
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    item {
                        Text(
                            "Error: ${eventsState.message}",
                            color = historyRed
                        )
                    }
                }

                is UiState.Success -> {

                    if (filteredEvents.isEmpty()) {
                        item {
                            Card(
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor =
                                            historyCard
                                    )
                            ) {
                                Text(
                                    "No hay eventos que " +
                                        "coincidan con los filtros.",
                                    modifier =
                                        Modifier.padding(20.dp),
                                    color = historyGray
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
                                    color = historyGray,
                                    style = MaterialTheme
                                        .typography.titleSmall
                                )
                            }

                            items(
                                dayEvents,
                                key = { it.id }
                            ) { event ->

                                val priorityColor =
                                    when (event.prioridad) {
                                        Prioridad.CRITICA ->
                                            historyRed
                                        Prioridad.ALTA ->
                                            historyYellow
                                        Prioridad.MEDIA ->
                                            historyBlue
                                        Prioridad.BAJA ->
                                            historyGreen
                                    }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onEventClick(event.id)
                                        },
                                    shape =
                                        RoundedCornerShape(16.dp),
                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor =
                                                historyCard
                                        )
                                ) {
                                    Column(
                                        modifier =
                                            Modifier.padding(16.dp),
                                        verticalArrangement =
                                            Arrangement.spacedBy(8.dp)
                                    ) {

                                        Row(
                                            modifier =
                                                Modifier.fillMaxWidth(),
                                            horizontalArrangement =
                                                Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                event.prioridad.name,
                                                color = priorityColor,
                                                style =
                                                    MaterialTheme
                                                        .typography
                                                        .labelSmall
                                            )

                                            Text(
                                                event.estado.name
                                                    .replace("_", " "),
                                                color = if (
                                                    event.estado ==
                                                    EstadoEvento.RESUELTO
                                                ) historyGreen
                                                else historyGray,
                                                style =
                                                    MaterialTheme
                                                        .typography
                                                        .labelSmall
                                            )
                                        }

                                        Text(
                                            event.titulo,
                                            color = Color.White,
                                            fontWeight =
                                                FontWeight.Bold
                                        )

                                        Text(
                                            event.ubicacion,
                                            color = historyGray
                                        )

                                        Text(
                                            event.fechaHora,
                                            color = historyGray,
                                            style =
                                                MaterialTheme
                                                    .typography.bodySmall
                                        )

                                        Text(
                                            "Ver detalle",
                                            color = historyBlue,
                                            style =
                                                MaterialTheme
                                                    .typography.labelMedium
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
private fun HistoryMetric(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                historyBg,
                RoundedCornerShape(12.dp)
            )
            .padding(10.dp)
    ) {
        Text(
            title,
            color = historyGray,
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
