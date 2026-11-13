package com.example.modoguardian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val monitorDark = Color(0xFF07101C)
private val monitorGreen = Color(0xFF20C997)
private val monitorRed = Color(0xFFF04452)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraDetailScreen(
    cameraId: String,
    onBack: () -> Unit
) {
    val camera = demoCameras.find {
        it.id == cameraId
    }

    val colors = MaterialTheme.colorScheme

    var recording by rememberSaveable(cameraId) {
        mutableStateOf(false)
    }

    var microphone by rememberSaveable(cameraId) {
        mutableStateOf(false)
    }

    var infrared by rememberSaveable(cameraId) {
        mutableStateOf(false)
    }

    var flashlight by rememberSaveable(cameraId) {
        mutableStateOf(false)
    }

    var expanded by rememberSaveable(cameraId) {
        mutableStateOf(false)
    }

    var message by rememberSaveable(cameraId) {
        mutableStateOf("")
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            camera?.name ?: "Detalle de cámara",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "INSERCODE · Monitor de seguridad",
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

        if (camera == null) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.VideocamOff,
                        contentDescription = null,
                        tint = colors.error,
                        modifier = Modifier.size(50.dp)
                    )

                    Text(
                        "Cámara no encontrada",
                        color = colors.onBackground,
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedButton(onClick = onBack) {
                        Text("Volver")
                    }
                }
            }

        } else {

            val online = camera.online

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                // IDENTIFICACIÓN DE CÁMARA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            camera.id,
                            color = colors.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            camera.zone,
                            color = colors.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (online)
                            colors.secondaryContainer
                        else
                            colors.errorContainer
                    ) {
                        Text(
                            if (online)
                                "● EN LÍNEA"
                            else
                                "● SIN CONEXIÓN",
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 8.dp
                            ),
                            color = if (online)
                                colors.onSecondaryContainer
                            else
                                colors.onErrorContainer,
                            style =
                                MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // MONITOR DE VIGILANCIA
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = monitorDark
                    )
                ) {
                    Column {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                camera.id + " · DEMO",
                                color = monitorGreen,
                                style =
                                    MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                if (recording && online)
                                    "● REC SIMULADA"
                                else if (online)
                                    "● VISTA DEMO"
                                else
                                    "● OFFLINE",
                                color = if (recording && online)
                                    monitorRed
                                else if (online)
                                    monitorGreen
                                else
                                    Color.LightGray,
                                style =
                                    MaterialTheme.typography.labelSmall
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    if (expanded) 380.dp
                                    else 240.dp
                                )
                                .background(
                                    if (infrared && online)
                                        Color(0xFF102C24)
                                    else
                                        monitorDark
                                ),
                            contentAlignment = Alignment.Center
                        ) {

                            // MARCO VISUAL DEL MONITOR
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp)
                                    .border(
                                        width = 1.dp,
                                        color = monitorGreen.copy(
                                            alpha = 0.25f
                                        ),
                                        shape =
                                            RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {

                                Column(
                                    horizontalAlignment =
                                        Alignment.CenterHorizontally,
                                    verticalArrangement =
                                        Arrangement.spacedBy(14.dp)
                                ) {
                                    Icon(
                                        if (online)
                                            Icons.Default.Videocam
                                        else
                                            Icons.Default.VideocamOff,
                                        contentDescription = null,
                                        tint = if (online)
                                            monitorGreen
                                        else
                                            monitorRed,
                                        modifier = Modifier.size(52.dp)
                                    )

                                    Text(
                                        if (online)
                                            "MONITOR DE DEMOSTRACIÓN"
                                        else
                                            "SIN SEÑAL",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )

                                    Text(
                                        if (online)
                                            "No hay transmisión real"
                                        else
                                            "Dispositivo desconectado",
                                        color = Color.LightGray,
                                        style =
                                            MaterialTheme.typography.bodySmall
                                    )

                                    if (infrared && online) {
                                        Text(
                                            "VISIÓN NOCTURNA · DEMO",
                                            color = monitorGreen,
                                            style =
                                                MaterialTheme.typography.labelSmall
                                        )
                                    }

                                    if (flashlight && online) {
                                        Text(
                                            "LUZ · DEMO",
                                            color = Color.Yellow,
                                            style =
                                                MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF050B15))
                                .padding(14.dp),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (recording && online)
                                    "● GRABACIÓN SIMULADA"
                                else
                                    "● MONITOR LOCAL",
                                color = if (recording && online)
                                    monitorRed
                                else
                                    Color.LightGray,
                                style =
                                    MaterialTheme.typography.labelSmall
                            )

                            Text(
                                if (infrared && online)
                                    "IR · DEMO"
                                else
                                    "VIDEO NO DISPONIBLE",
                                color = monitorGreen,
                                style =
                                    MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                // CONTROLES
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceContainer
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(16.dp)
                    ) {

                        Text(
                            "CONTROLES DE CÁMARA",
                            color = colors.onSurface,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            CameraAction(
                                icon = Icons.Default.PhotoCamera,
                                label = "Foto",
                                active = false,
                                enabled = online,
                                modifier = Modifier.weight(1f)
                            ) {
                                message =
                                    "Vista de demostración: " +
                                        "no se capturó ninguna imagen."
                            }

                            CameraAction(
                                icon = if (recording)
                                    Icons.Default.StopCircle
                                else
                                    Icons.Default.FiberManualRecord,
                                label = if (recording)
                                    "Parar"
                                else
                                    "Grabar",
                                active = recording,
                                enabled = online,
                                modifier = Modifier.weight(1f)
                            ) {
                                recording = !recording

                                message = if (recording)
                                    "Grabación simulada activada."
                                else
                                    "Grabación simulada detenida."
                            }

                            CameraAction(
                                icon = if (microphone)
                                    Icons.Default.Mic
                                else
                                    Icons.Default.MicOff,
                                label = "Hablar",
                                active = microphone,
                                enabled = online,
                                modifier = Modifier.weight(1f)
                            ) {
                                microphone = !microphone

                                message = if (microphone)
                                    "Audio demo activado. " +
                                        "No se transmite voz."
                                else
                                    "Audio demo desactivado."
                            }

                            CameraAction(
                                icon = Icons.Default.NightsStay,
                                label = "IR",
                                active = infrared,
                                enabled = online,
                                modifier = Modifier.weight(1f)
                            ) {
                                infrared = !infrared

                                message = if (infrared)
                                    "Visión nocturna simulada."
                                else
                                    "Modo nocturno desactivado."
                            }

                            CameraAction(
                                icon = Icons.Default.FlashlightOn,
                                label = "Luz",
                                active = flashlight,
                                enabled = online,
                                modifier = Modifier.weight(1f)
                            ) {
                                flashlight = !flashlight

                                message = if (flashlight)
                                    "Luz de demostración activada."
                                else
                                    "Luz de demostración desactivada."
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                expanded = !expanded
                            },
                            enabled = online,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                if (expanded)
                                    Icons.Default.FullscreenExit
                                else
                                    Icons.Default.Fullscreen,
                                contentDescription = null
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                if (expanded)
                                    "Restaurar monitor"
                                else
                                    "Ampliar monitor"
                            )
                        }

                        if (message.isNotBlank()) {
                            Surface(
                                color = colors.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    message,
                                    modifier = Modifier.padding(12.dp),
                                    color = colors.onPrimaryContainer,
                                    style =
                                        MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // ESTADO DEL DISPOSITIVO
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceContainer
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            "ESTADO DEL DISPOSITIVO",
                            color = colors.onSurface,
                            fontWeight = FontWeight.Bold
                        )

                        DeviceInfo(
                            icon = Icons.Default.PowerSettingsNew,
                            title = "Estado",
                            value = if (online)
                                "En línea · Demo"
                            else
                                "Desconectada",
                            accent = if (online)
                                colors.secondary
                            else
                                colors.error
                        )

                        HorizontalDivider(
                            color = colors.outlineVariant
                        )

                        DeviceInfo(
                            icon = Icons.Default.Wifi,
                            title = "Señal",
                            value = if (online)
                                "Disponible (simulada)"
                            else
                                "Sin conexión",
                            accent = colors.primary
                        )

                        HorizontalDivider(
                            color = colors.outlineVariant
                        )

                        DeviceInfo(
                            icon = Icons.Default.Storage,
                            title = "Grabación",
                            value = "Sin almacenamiento de video",
                            accent = colors.tertiary
                        )
                    }
                }

                Text(
                    "INSERCODE · Módulo de cámaras en demostración",
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.onSurfaceVariant,
                    style =
                        MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CameraAction(
    icon: ImageVector,
    label: String,
    active: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {

        FilledIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(50.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (active)
                    colors.primary
                else
                    colors.surfaceContainerHighest,
                contentColor = if (active)
                    colors.onPrimary
                else
                    colors.onSurface
            )
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(23.dp)
            )
        }

        Text(
            label,
            color = if (enabled)
                colors.onSurface
            else
                colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DeviceInfo(
    icon: ImageVector,
    title: String,
    value: String,
    accent: Color
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(24.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                title,
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
}
