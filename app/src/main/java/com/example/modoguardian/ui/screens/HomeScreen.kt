package com.example.modoguardian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.*
import com.example.modoguardian.ui.state.UiState

private val Fondo = Color(0xFF0B1426)
private val Superficie = Color(0xFF17243C)
private val Azul = Color(0xFF2864E8)
private val Verde = Color(0xFF20C997)
private val Rojo = Color(0xFFFF5252)
private val Gris = Color(0xFF9BAAC3)

private data class CamaraDemo(
    val nombre: String,
    val zona: String,
    val conectada: Boolean
)

private val camarasDemo = listOf(
    CamaraDemo("Entrada Principal", "Zona Sur", true),
    CamaraDemo("Perímetro Norte", "Zona Sur", true),
    CamaraDemo("Bodega A", "Zona Sur", false),
    CamaraDemo("Estacionamiento", "Zona Sur", true)
)

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
    val eventos = (eventsState as? UiState.Success)?.data
        ?: emptyList()

    val criticas = eventos.count {
        it.prioridad == Prioridad.CRITICA &&
        it.estado != EstadoEvento.RESUELTO
    }

    val pendientes = eventos.count {
        it.estado == EstadoEvento.PENDIENTE
    }

    val activas = camarasDemo.count { it.conectada }
    val porcentaje = activas * 100 / camarasDemo.size

    Scaffold(
        containerColor = Fondo,
        bottomBar = {
            NavigationBar(containerColor = Superficie) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Text("⌂") },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onOpenCameras,
                    icon = { Text("▣") },
                    label = { Text("Cámaras") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onOpenEvents,
                    icon = { Text("☷") },
                    label = { Text("Eventos") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onOpenAlerts,
                    icon = { Text("!") },
                    label = { Text("Alertas") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onOpenProfile,
                    icon = { Text("◉") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Hola, ${user?.nombre ?: "Guardián"}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "INSERCODE · Panel de seguridad",
                            color = Gris
                        )
                    }

                    TextButton(onClick = onOpenAlerts) {
                        Text("Alertas ($criticas)")
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Superficie
                    ),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Text(
                            "Estado General",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "INSERCODE · GUARDIÁN OS",
                            color = Gris
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Indicador(
                                "TOTAL",
                                "${camarasDemo.size}",
                                Azul,
                                Modifier.weight(1f)
                            )
                            Indicador(
                                "ACTIVAS",
                                "$activas",
                                Verde,
                                Modifier.weight(1f)
                            )
                            Indicador(
                                "ATENCIÓN",
                                "${camarasDemo.size - activas}",
                                Rojo,
                                Modifier.weight(1f)
                            )
                        }

                        Text("Capacidad operativa", color = Gris)

                        LinearProgressIndicator(
                            progress = { porcentaje / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = Azul,
                            trackColor = Fondo
                        )

                        Text(
                            "$porcentaje% de cámaras disponibles",
                            color = Color.White
                        )

                        Text(
                            "Cámaras de demostración",
                            color = Gris
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Cámaras Asignadas",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Ver todas",
                        modifier = Modifier.clickable {
                            onOpenCameras()
                        },
                        color = Azul
                    )
                }
            }

            items(camarasDemo.chunked(2)) { fila ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    fila.forEach { camara ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenCameras() },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Superficie
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(75.dp)
                                        .background(
                                            Fondo,
                                            RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (camara.conectada)
                                            "CÁMARA · DEMO"
                                        else "SIN CONEXIÓN",
                                        color = Gris
                                    )
                                }

                                Text(
                                    camara.nombre,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(camara.zona, color = Gris)

                                Text(
                                    if (camara.conectada)
                                        "● En línea"
                                    else "● Desconectada",
                                    color = if (camara.conectada)
                                        Verde else Rojo
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Eventos recientes",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Ver historial",
                        modifier = Modifier.clickable {
                            onOpenEvents()
                        },
                        color = Azul
                    )
                }
            }

            when (eventsState) {
                is UiState.Loading -> item {
                    CircularProgressIndicator(color = Azul)
                }

                is UiState.Error -> item {
                    Text(
                        "No se pudieron cargar los eventos",
                        color = Rojo
                    )
                }

                is UiState.Success -> {
                    if (eventos.isEmpty()) {
                        item {
                            Text(
                                "No hay eventos registrados",
                                color = Gris
                            )
                        }
                    } else {
                        items(eventos.take(4), key = { it.id }) { evento ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onOpenEvent(evento.id)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Superficie
                                ),
                                shape = RoundedCornerShape(15.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        evento.titulo,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(5.dp))
                                    Text(
                                        evento.ubicacion,
                                        color = Gris
                                    )
                                    Spacer(Modifier.height(5.dp))
                                    Text(
                                        "${evento.prioridad} · ${evento.estado}",
                                        color = if (
                                            evento.prioridad ==
                                            Prioridad.CRITICA
                                        ) Rojo else Verde
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onOpenAlerts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Azul
                    )
                ) {
                    Text("Gestionar alertas ($pendientes pendientes)")
                }
            }
        }
    }
}

@Composable
private fun Indicador(
    titulo: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                Fondo,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            titulo,
            color = Gris,
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            valor,
            color = color,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
