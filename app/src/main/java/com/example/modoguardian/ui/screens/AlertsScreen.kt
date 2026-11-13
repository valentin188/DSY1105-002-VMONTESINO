package com.example.modoguardian.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.Event
import com.example.modoguardian.model.MotivoCierre
import com.example.modoguardian.model.Prioridad
import com.example.modoguardian.ui.state.UiState

private enum class FiltroAlerta {
    ACTIVAS,
    CRITICAS,
    PENDIENTES,
    TODAS
}

private enum class AccionAlerta {
    CONFIRMAR,
    RESOLVER,
    FALSA_ALARMA
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    eventsState: UiState<List<Event>>,
    onBack: () -> Unit,
    onStatusChange: (String, EstadoEvento) -> Unit,
    onCloseWithReason: ((String, MotivoCierre) -> Unit)? = null,
    saving: Boolean = false,
    operationError: String? = null,
    operationMessage: String? = null
) {
    val colors = MaterialTheme.colorScheme
    var busqueda by rememberSaveable {
        mutableStateOf("")
    }

    var filtro by rememberSaveable {
        mutableStateOf(FiltroAlerta.ACTIVAS)
    }

    var eventoSeleccionadoId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var accionPendiente by remember {
        mutableStateOf<AccionAlerta?>(null)
    }

    val eventos = when (eventsState) {
        is UiState.Success -> eventsState.data
        else -> emptyList()
    }

    val alertas = eventos.filter {
        it.prioridad == Prioridad.CRITICA ||
            it.prioridad == Prioridad.ALTA
    }

    val activas = alertas.count {
        it.estado != EstadoEvento.RESUELTO
    }

    val criticas = alertas.count {
        it.prioridad == Prioridad.CRITICA &&
            it.estado != EstadoEvento.RESUELTO
    }

    val pendientes = alertas.count {
        it.estado == EstadoEvento.PENDIENTE
    }

    val falsasAlarmas = alertas.count {
        it.motivoCierre == MotivoCierre.FALSA_ALARMA
    }

    val alertasFiltradas = alertas
        .filter { evento ->

            val coincideTexto =
                evento.titulo.contains(busqueda, true) ||
                evento.descripcion.contains(busqueda, true) ||
                evento.ubicacion.contains(busqueda, true) ||
                evento.id.contains(busqueda, true)

            val coincideFiltro = when (filtro) {
                FiltroAlerta.ACTIVAS ->
                    evento.estado != EstadoEvento.RESUELTO

                FiltroAlerta.CRITICAS ->
                    evento.prioridad == Prioridad.CRITICA &&
                        evento.estado != EstadoEvento.RESUELTO

                FiltroAlerta.PENDIENTES ->
                    evento.estado == EstadoEvento.PENDIENTE

                FiltroAlerta.TODAS -> true
            }

            coincideTexto && coincideFiltro
        }
        .sortedWith(
            compareBy<Event> {
                if (it.estado == EstadoEvento.RESUELTO) 1 else 0
            }.thenBy {
                if (it.prioridad == Prioridad.CRITICA) 0 else 1
            }.thenByDescending {
                it.fechaHora
            }
        )

    val eventoSeleccionado = alertas.find {
        it.id == eventoSeleccionadoId
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Centro de alertas",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "INSERCODE · Seguridad",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant
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
                    navigationIconContentColor = colors.onBackground
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
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = colors.primary
                            )

                            Text(
                                "Resumen de incidentes",
                                color = colors.onSurface,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IndicadorAlerta(
                                "ACTIVAS",
                                activas.toString(),
                                colors.primary,
                                Modifier.weight(1f)
                            )

                            IndicadorAlerta(
                                "CRÍTICAS",
                                criticas.toString(),
                                colors.error,
                                Modifier.weight(1f)
                            )

                            IndicadorAlerta(
                                "PENDIENTES",
                                pendientes.toString(),
                                colors.tertiary,
                                Modifier.weight(1f)
                            )
                        }

                        Text(
                            "Falsas alarmas registradas: $falsasAlarmas",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            "Registros locales de demostración",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            if (saving) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (!operationError.isNullOrBlank()) {
                item {
                    Text(
                        operationError,
                        color = colors.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (!operationMessage.isNullOrBlank()) {
                item {
                    Text(
                        operationMessage,
                        color = colors.secondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = {
                        busqueda = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Buscar alerta o ubicación")
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(15.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.horizontalScroll(
                        rememberScrollState()
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filtro == FiltroAlerta.ACTIVAS,
                        onClick = {
                            filtro = FiltroAlerta.ACTIVAS
                        },
                        label = { Text("Activas") }
                    )

                    FilterChip(
                        selected = filtro == FiltroAlerta.CRITICAS,
                        onClick = {
                            filtro = FiltroAlerta.CRITICAS
                        },
                        label = { Text("Críticas") }
                    )

                    FilterChip(
                        selected = filtro == FiltroAlerta.PENDIENTES,
                        onClick = {
                            filtro = FiltroAlerta.PENDIENTES
                        },
                        label = { Text("Pendientes") }
                    )

                    FilterChip(
                        selected = filtro == FiltroAlerta.TODAS,
                        onClick = {
                            filtro = FiltroAlerta.TODAS
                        },
                        label = { Text("Todas") }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Incidentes",
                        color = colors.onBackground,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "${alertasFiltradas.size} resultados",
                        color = colors.onSurfaceVariant
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
                            CircularProgressIndicator()
                        }
                    }
                }

                is UiState.Error -> {
                    item {
                        Text(
                            "No se pudieron cargar las alertas: " +
                                eventsState.message,
                            color = colors.error
                        )
                    }
                }

                is UiState.Success -> {

                    if (alertasFiltradas.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = colors.surfaceContainer
                                )
                            ) {
                                Text(
                                    "No hay alertas para este filtro.",
                                    modifier = Modifier.padding(24.dp),
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(
                            items = alertasFiltradas,
                            key = { it.id }
                        ) { evento ->

                            val colorPrioridad =
                                if (evento.prioridad == Prioridad.CRITICA)
                                    colors.error
                                else
                                    colors.tertiary

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        eventoSeleccionadoId = evento.id
                                    },
                                shape = RoundedCornerShape(17.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = colors.surfaceContainer
                                )
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
                                            evento.prioridad.name,
                                            color = colorPrioridad,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            evento.estado.name.replace(
                                                "_", " "
                                            ),
                                            color = colors.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        evento.titulo,
                                        color = colors.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        evento.ubicacion,
                                        color = colors.onSurfaceVariant
                                    )

                                    Text(
                                        evento.fechaHora,
                                        color = colors.onSurfaceVariant,
                                        style =
                                            MaterialTheme.typography.bodySmall
                                    )

                                    if (evento.motivoCierre != null) {
                                        Text(
                                            when (evento.motivoCierre) {
                                                MotivoCierre.FALSA_ALARMA ->
                                                    "Cierre: falsa alarma"

                                                MotivoCierre.INCIDENTE_SOLUCIONADO ->
                                                    "Cierre: incidente solucionado"
                                            },
                                            color = colors.secondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    HorizontalDivider()

                                    Text(
                                        "Ver detalles y acciones",
                                        color = colors.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DETALLE DEL INCIDENTE
    if (eventoSeleccionado != null && accionPendiente == null) {
        val evento = eventoSeleccionado

        AlertDialog(
            onDismissRequest = {
                eventoSeleccionadoId = null
            },
            title = {
                Text("Detalle de alerta")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        evento.titulo,
                        fontWeight = FontWeight.Bold
                    )

                    Text(evento.descripcion)

                    Text("Ubicación: ${evento.ubicacion}")
                    Text("Fecha: ${evento.fechaHora}")
                    Text("Estado: ${evento.estado.name.replace("_", " ")}")

                    if (evento.estado == EstadoEvento.RESUELTO) {
                        Text(
                            when (evento.motivoCierre) {
                                MotivoCierre.FALSA_ALARMA ->
                                    "Motivo: falsa alarma"

                                MotivoCierre.INCIDENTE_SOLUCIONADO ->
                                    "Motivo: incidente solucionado"

                                null ->
                                    "Motivo de cierre no registrado"
                            },
                            color = colors.secondary
                        )
                    } else {

                        if (evento.estado == EstadoEvento.PENDIENTE) {
                            Button(
                                onClick = {
                                    accionPendiente =
                                        AccionAlerta.CONFIRMAR
                                },
                                enabled = !saving,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Confirmar alerta")
                            }
                        }

                        Button(
                            onClick = {
                                accionPendiente =
                                    AccionAlerta.RESOLVER
                            },
                            enabled = !saving &&
                                onCloseWithReason != null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Resolver incidente")
                        }

                        OutlinedButton(
                            onClick = {
                                accionPendiente =
                                    AccionAlerta.FALSA_ALARMA
                            },
                            enabled = !saving &&
                                onCloseWithReason != null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Marcar falsa alarma")
                        }

                        if (onCloseWithReason == null) {
                            Text(
                                "La clasificación de cierres se " +
                                    "habilitará al conectar MainActivity.",
                                color = colors.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        eventoSeleccionadoId = null
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }

    // CONFIRMACIÓN DE LA ACCIÓN
    val evento = eventoSeleccionado
    val accion = accionPendiente

    if (evento != null && accion != null) {
        val titulo = when (accion) {
            AccionAlerta.CONFIRMAR -> "Confirmar alerta"
            AccionAlerta.RESOLVER -> "Resolver incidente"
            AccionAlerta.FALSA_ALARMA -> "Registrar falsa alarma"
        }

        AlertDialog(
            onDismissRequest = {
                accionPendiente = null
            },
            title = {
                Text(titulo)
            },
            text = {
                Text(
                    when (accion) {
                        AccionAlerta.CONFIRMAR ->
                            "La alerta pasará a EN PROCESO."

                        AccionAlerta.RESOLVER ->
                            "El incidente quedará RESUELTO " +
                                "con motivo INCIDENTE SOLUCIONADO."

                        AccionAlerta.FALSA_ALARMA ->
                            "El incidente quedará RESUELTO " +
                                "con motivo FALSA ALARMA."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !saving,
                    onClick = {
                        when (accion) {
                            AccionAlerta.CONFIRMAR -> {
                                onStatusChange(
                                    evento.id,
                                    EstadoEvento.EN_PROCESO
                                )
                            }

                            AccionAlerta.RESOLVER -> {
                                onCloseWithReason?.invoke(
                                    evento.id,
                                    MotivoCierre.INCIDENTE_SOLUCIONADO
                                )
                            }

                            AccionAlerta.FALSA_ALARMA -> {
                                onCloseWithReason?.invoke(
                                    evento.id,
                                    MotivoCierre.FALSA_ALARMA
                                )
                            }
                        }

                        accionPendiente = null
                        eventoSeleccionadoId = null
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        accionPendiente = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun IndicadorAlerta(
    titulo: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            titulo,
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )

        Text(
            valor,
            color = color,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
