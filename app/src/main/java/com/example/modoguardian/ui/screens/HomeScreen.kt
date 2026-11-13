package com.example.modoguardian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import com.example.modoguardian.model.*
import com.example.modoguardian.ui.state.UiState

@Composable
fun HomeScreen(
    user: User?,
    eventsState: UiState<List<Event>>,
    onOpenEvents: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenCameras: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenEvent: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val azul = colors.primary
    val verde = colors.secondary
    val rojo = colors.error
    val amarillo = colors.tertiary
    val gris = colors.onSurfaceVariant
    val camaras = demoCameras
    val totalCamaras = camaras.size
    val camarasActivas = camaras.count { it.online }
    val camarasInactivas = totalCamaras - camarasActivas
    val disponibilidad = if (totalCamaras > 0) {
        camarasActivas.toFloat() / totalCamaras
    } else {
        0f
    }

    val eventos = when (eventsState) {
        is UiState.Success -> eventsState.data
        else -> emptyList()
    }

    val alertasCriticas = eventos.count {
        it.prioridad == Prioridad.CRITICA &&
            it.estado != EstadoEvento.RESUELTO
    }

    val eventosPendientes = eventos.count {
        it.estado == EstadoEvento.PENDIENTE
    }

    val eventosRecientes = eventos
        .sortedByDescending { it.fechaHora }
        .take(4)

    Scaffold(
        containerColor = colors.background
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 18.dp,
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            //Encabezado
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "Hola, ${user?.nombre ?: "Guardián"}",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.onBackground,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Circle,
                                contentDescription = null,
                                tint = verde,
                                modifier = Modifier.size(9.dp)
                            )

                            Text(
                                "Panel de monitoreo · Demo",
                                color = gris,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenProfile
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Abrir perfil",
                            tint = azul,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            //Panel de estado general
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 3.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(17.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Estado General",
                                    color = colors.onSurface,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    "INSERCODE · SEGURIDAD INTEGRAL",
                                    color = gris,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = azul,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HomeMetric(
                                title = "TOTAL",
                                value = "$totalCamaras",
                                icon = Icons.Default.Videocam,
                                accent = azul,
                                modifier = Modifier.weight(1f)
                            )

                            HomeMetric(
                                title = "ACTIVAS",
                                value = "$camarasActivas",
                                icon = Icons.Default.CheckCircle,
                                accent = verde,
                                modifier = Modifier.weight(1f)
                            )

                            HomeMetric(
                                title = "ATENCIÓN",
                                value = "$camarasInactivas",
                                icon = Icons.Default.Warning,
                                accent = amarillo,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "DISPONIBILIDAD DE CÁMARAS",
                                color = gris,
                                style = MaterialTheme.typography.labelSmall
                            )

                            Text(
                                "${(disponibilidad * 100).toInt()}%",
                                color = colors.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LinearProgressIndicator(
                            progress = { disponibilidad },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp),
                            color = azul,
                            trackColor = colors.surfaceVariant
                        )

                        Text(
                            "Datos de cámaras simulados",
                            color = gris,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            //Resumen de alertas
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeSummaryCard(
                        icon = Icons.Default.ErrorOutline,
                        title = "Críticas",
                        value = "$alertasCriticas",
                        accent = rojo,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenAlerts
                    )

                    HomeSummaryCard(
                        icon = Icons.Default.Schedule,
                        title = "Pendientes",
                        value = "$eventosPendientes",
                        accent = amarillo,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenEvents
                    )
                }
            }

            //Cámaras asignadas
            item {
                HomeSectionHeader(
                    title = "Cámaras Asignadas",
                    action = "Ver todas",
                    onAction = onOpenCameras
                )
            }

            items(
                items = camaras.chunked(2)
            ) { fila ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    fila.forEach { camara ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onOpenCameras()
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = colors.surfaceContainer
                            ),
                            shape = RoundedCornerShape(17.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(108.dp)
                                        .background(
                                            colors.surfaceContainerHighest
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment =
                                            Alignment.CenterHorizontally,
                                        verticalArrangement =
                                            Arrangement.spacedBy(7.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (camara.online)
                                                Icons.Default.Videocam
                                            else
                                                Icons.Default.VideocamOff,
                                            contentDescription = null,
                                            tint = if (camara.online)
                                                azul else gris,
                                            modifier = Modifier.size(31.dp)
                                        )

                                        Text(
                                            if (camara.online)
                                                "VISTA DEMO"
                                            else
                                                "SIN CONEXIÓN",
                                            color = gris,
                                            style =
                                                MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.padding(
                                        start = 12.dp,
                                        end = 12.dp,
                                        bottom = 14.dp
                                    ),
                                    verticalArrangement =
                                        Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        camara.name,
                                        color = colors.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        style =
                                            MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        camara.zone,
                                        color = gris,
                                        style =
                                            MaterialTheme.typography.labelSmall
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement =
                                            Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Circle,
                                            contentDescription = null,
                                            modifier = Modifier.size(8.dp),
                                            tint = if (camara.online)
                                                verde else rojo
                                        )

                                        Text(
                                            if (camara.online)
                                                "En línea"
                                            else
                                                "Sin conexión",
                                            color = if (camara.online)
                                                verde else rojo,
                                            style =
                                                MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (fila.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

            //Eventos recientes
            item {
                HomeSectionHeader(
                    title = "Eventos en vivo",
                    action = "Ver historial",
                    onAction = onOpenEvents
                )
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
                                color = azul
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    item {
                        Text(
                            "No se pudieron cargar los eventos",
                            color = rojo
                        )
                    }
                }

                is UiState.Success -> {
                    if (eventosRecientes.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = colors.surfaceContainer
                                )
                            ) {
                                Text(
                                    "No hay eventos registrados.",
                                    modifier = Modifier.padding(20.dp),
                                    color = gris
                                )
                            }
                        }
                    } else {
                        items(
                            items = eventosRecientes,
                            key = { it.id }
                        ) { evento ->

                            val accent = when (evento.prioridad) {
                                Prioridad.CRITICA -> rojo
                                Prioridad.ALTA -> amarillo
                                Prioridad.MEDIA -> azul
                                Prioridad.BAJA -> verde
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onOpenEvent(evento.id)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor =
                                        colors.surfaceContainer
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(15.dp),
                                    horizontalArrangement =
                                        Arrangement.spacedBy(13.dp),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .size(45.dp)
                                            .background(
                                                accent.copy(alpha = 0.13f),
                                                RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = accent
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement =
                                            Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(
                                            evento.titulo,
                                            color = colors.onSurface,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            evento.ubicacion,
                                            color = gris,
                                            style =
                                                MaterialTheme.typography.bodySmall
                                        )

                                        Text(
                                            evento.fechaHora,
                                            color = gris,
                                            style =
                                                MaterialTheme.typography.labelSmall
                                        )

                                        Text(
                                            evento.estado.name.replace(
                                                "_",
                                                " "
                                            ),
                                            color = accent,
                                            style =
                                                MaterialTheme.typography.labelSmall
                                        )
                                    }

                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = "Ver evento",
                                        tint = gris
                                    )
                                }
                            }
                        }
                    }
                }
            }

            //Acción destacada
            item {
                Button(
                    onClick = onOpenAlerts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = azul
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.ReportProblem,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        "Gestionar alertas",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Text(
                    "INSERCODE · Prototipo académico",
                    modifier = Modifier.fillMaxWidth(),
                    color = gris,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign =
                        androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HomeMetric(
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
private fun HomeSummaryCard(
    icon: ImageVector,
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier.clickable(
            onClick = onClick
        ),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = accent
            )

            Column {
                Text(
                    value,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    title,
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    action: String,
    onAction: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            color = colors.onBackground,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        TextButton(onClick = onAction) {
            Text(action)
        }
    }
}
