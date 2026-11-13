package com.example.modoguardian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class DemoCamera(
    val id: String,
    val name: String,
    val zone: String,
    val online: Boolean
)

val demoCameras = listOf(
    DemoCamera(
        id = "CAM-01",
        name = "Entrada Principal",
        zone = "Zona Sur",
        online = true
    ),
    DemoCamera(
        id = "CAM-04",
        name = "Perímetro Norte",
        zone = "Zona Sur",
        online = true
    ),
    DemoCamera(
        id = "CAM-07",
        name = "Bodega A",
        zone = "Zona Sur",
        online = false
    ),
    DemoCamera(
        id = "CAM-02",
        name = "Estacionamiento",
        zone = "Zona Sur",
        online = true
    )
)

private enum class CameraFilter {
    TODAS,
    EN_LINEA,
    SIN_CONEXION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamerasScreen(
    onBack: () -> Unit,
    onCameraClick: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var search by rememberSaveable {
        mutableStateOf("")
    }

    var filter by rememberSaveable {
        mutableStateOf(CameraFilter.TODAS)
    }

    val total = demoCameras.size
    val online = demoCameras.count { it.online }
    val offline = total - online
    val filteredCameras = remember(search, filter) {
        demoCameras.filter { camera ->
            val matchesSearch =
                camera.name.contains(search, true) ||
                camera.zone.contains(search, true) ||
                camera.id.contains(search, true)
            val matchesFilter = when (filter) {
                CameraFilter.TODAS -> true
                CameraFilter.EN_LINEA -> camera.online
                CameraFilter.SIN_CONEXION -> !camera.online
            }

            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Mis Cámaras",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "INSERCODE · Monitoreo",
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

            //Panel principal
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 3.dp
                    ),
                    shape = RoundedCornerShape(22.dp)
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
                            Column {
                                Text(
                                    "Estado de monitoreo",
                                    color = colors.onSurface,
                                    style =
                                        MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    "DISPOSITIVOS ASIGNADOS · DEMO",
                                    color = colors.onSurfaceVariant,
                                    style =
                                        MaterialTheme.typography.labelSmall
                                )
                            }

                            Icon(
                                Icons.Default.Videocam,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            CameraMetric(
                                title = "TOTAL",
                                value = total.toString(),
                                accent = colors.primary,
                                modifier = Modifier.weight(1f)
                            )

                            CameraMetric(
                                title = "EN LÍNEA",
                                value = online.toString(),
                                accent = colors.secondary,
                                modifier = Modifier.weight(1f)
                            )

                            CameraMetric(
                                title = "ATENCIÓN",
                                value = offline.toString(),
                                accent = colors.error,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {
                            Text(
                                "DISPONIBILIDAD",
                                color = colors.onSurfaceVariant,
                                style =
                                    MaterialTheme.typography.labelSmall
                            )

                            Text(
                                if (total > 0)
                                    "${online * 100 / total}%"
                                else
                                    "0%",
                                color = colors.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LinearProgressIndicator(
                            progress = {
                                if (total > 0)
                                    online.toFloat() / total
                                else
                                    0f
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp),
                            color = colors.primary,
                            trackColor = colors.surfaceVariant
                        )

                        Text(
                            "Los estados son simulados. " +
                                "No existe conexión con cámaras reales.",
                            color = colors.onSurfaceVariant,
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            //Buscador
            item {
                OutlinedTextField(
                    value = search,
                    onValueChange = {
                        search = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Buscar cámara, zona o código")
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

            //Filtros
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
                            filter == CameraFilter.TODAS,
                        onClick = {
                            filter = CameraFilter.TODAS
                        },
                        label = {
                            Text("Todas ($total)")
                        }
                    )

                    FilterChip(
                        selected =
                            filter == CameraFilter.EN_LINEA,
                        onClick = {
                            filter = CameraFilter.EN_LINEA
                        },
                        label = {
                            Text("En línea ($online)")
                        }
                    )

                    FilterChip(
                        selected =
                            filter == CameraFilter.SIN_CONEXION,
                        onClick = {
                            filter = CameraFilter.SIN_CONEXION
                        },
                        label = {
                            Text("Sin conexión ($offline)")
                        }
                    )
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
                        "Cámaras asignadas",
                        color = colors.onBackground,
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "${filteredCameras.size} resultados",
                        color = colors.onSurfaceVariant,
                        style =
                            MaterialTheme.typography.bodySmall
                    )
                }
            }

            //Lista de cámaras
            if (filteredCameras.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor =
                                colors.surfaceContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment =
                                Alignment.CenterHorizontally,
                            verticalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = colors.onSurfaceVariant
                            )

                            Text(
                                "No encontramos cámaras",
                                color = colors.onSurface,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "Prueba con otro nombre " +
                                    "o cambia los filtros.",
                                color = colors.onSurfaceVariant,
                                style =
                                    MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                items(
                    items = filteredCameras,
                    key = { it.id }
                ) { camera ->

                    CameraListCard(
                        camera = camera,
                        onClick = {
                            onCameraClick(camera.id)
                        }
                    )
                }
            }

            //Información final
            item {
                Text(
                    "INSERCODE · Cámaras de demostración",
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
}

@Composable
private fun CameraMetric(
    title: String,
    value: String,
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
            .padding(12.dp),
        verticalArrangement =
            Arrangement.spacedBy(7.dp)
    ) {
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
private fun CameraListCard(
    camera: DemoCamera,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val statusColor = if (camera.online)
        colors.secondary
    else
        colors.error

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceContainer
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column {

            //Áreaa de vista previa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        colors.surfaceContainerHighest
                    ),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (camera.online)
                            Icons.Default.Videocam
                        else
                            Icons.Default.VideocamOff,
                        contentDescription = null,
                        tint = if (camera.online)
                            colors.primary
                        else
                            colors.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )

                    Text(
                        if (camera.online)
                            "VISTA PREVIA · DEMO"
                        else
                            "CÁMARA DESCONECTADA",
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    color = colors.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        camera.id,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        color = colors.onSurface,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        if (camera.online)
                            "● EN LÍNEA"
                        else
                            "● OFFLINE",
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            //Datos de la cámara
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement =
                            Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            camera.name,
                            color = colors.onSurface,
                            fontWeight = FontWeight.Bold,
                            style =
                                MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            camera.zone,
                            color = colors.onSurfaceVariant,
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }

                    Icon(
                        if (camera.online)
                            Icons.Default.CheckCircle
                        else
                            Icons.Default.Warning,
                        contentDescription = null,
                        tint = statusColor
                    )
                }

                HorizontalDivider(
                    color = colors.outlineVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Circle,
                            contentDescription = null,
                            modifier = Modifier.size(9.dp),
                            tint = statusColor
                        )

                        Text(
                            if (camera.online)
                                "Disponible"
                            else
                                "Sin conexión",
                            color = statusColor,
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        "Ver detalle",
                        color = colors.primary,
                        fontWeight = FontWeight.SemiBold,
                        style =
                            MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
