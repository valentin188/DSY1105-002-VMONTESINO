package com.example.modoguardian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val detailBg = Color(0xFF0B1426)
private val detailSurface = Color(0xFF192740)
private val detailBlue = Color(0xFF2864E8)
private val detailGreen = Color(0xFF20C997)
private val detailRed = Color(0xFFFF5252)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraDetailScreen(
    cameraId: String,
    onBack: () -> Unit
) {
    val camera = demoCameras.find { it.id == cameraId }

    var recording by remember(cameraId) {
        mutableStateOf(false)
    }

    var microphone by remember(cameraId) {
        mutableStateOf(false)
    }

    var infrared by remember(cameraId) {
        mutableStateOf(false)
    }

    var flashlight by remember(cameraId) {
        mutableStateOf(false)
    }

    var fullscreen by remember(cameraId) {
        mutableStateOf(false)
    }

    var message by remember(cameraId) {
        mutableStateOf("")
    }

    Scaffold(
        containerColor = detailBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(camera?.name ?: "Cámara")
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = detailBg,
                    titleContentColor = Color.White
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
                Text(
                    "Cámara no encontrada",
                    color = Color.White
                )
            }
        } else {

            val enabled = camera.online

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            camera.id,
                            color = detailBlue
                        )

                        Text(
                            camera.zone,
                            color = Color.LightGray
                        )
                    }

                    Text(
                        if (camera.online)
                            "● EN LÍNEA"
                        else
                            "● SIN CONEXIÓN",
                        color = if (camera.online)
                            detailGreen else detailRed
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF07101C)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (infrared)
                                    Color(0xFF10291F)
                                else
                                    Color(0xFF07101C)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally,
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                if (enabled) "▣" else "⊘",
                                color = if (enabled)
                                    detailGreen else detailRed,
                                style = MaterialTheme.typography
                                    .displayMedium
                            )

                            Text(
                                if (!enabled)
                                    "CÁMARA DESCONECTADA"
                                else if (fullscreen)
                                    "VISTA AMPLIADA · DEMO"
                                else
                                    "VISTA DE CÁMARA · DEMO",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                if (enabled)
                                    "Sin transmisión de video real"
                                else
                                    "Controles no disponibles",
                                color = Color.LightGray
                            )

                            if (recording && enabled) {
                                Text(
                                    "● REC SIMULADA",
                                    color = detailRed
                                )
                            }

                            if (infrared && enabled) {
                                Text(
                                    "IR · DEMO",
                                    color = detailGreen
                                )
                            }

                            if (flashlight && enabled) {
                                Text(
                                    "LUZ ACTIVADA · DEMO",
                                    color = Color.Yellow
                                )
                            }
                        }
                    }
                }

                Text(
                    "CONTROLES DE DEMOSTRACIÓN",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.labelMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    CameraControl(
                        label = "Foto",
                        active = false,
                        enabled = enabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        message =
                            "Captura simulada. No se guardó una imagen."
                    }

                    CameraControl(
                        label = if (recording) "Parar" else "Grabar",
                        active = recording,
                        enabled = enabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        recording = !recording
                        message = if (recording)
                            "Grabación de demostración iniciada"
                        else
                            "Grabación de demostración detenida"
                    }

                    CameraControl(
                        label = "Hablar",
                        active = microphone,
                        enabled = enabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        microphone = !microphone
                        message = if (microphone)
                            "Micrófono demo activado"
                        else
                            "Micrófono demo desactivado"
                    }

                    CameraControl(
                        label = "IR",
                        active = infrared,
                        enabled = enabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        infrared = !infrared
                        message = if (infrared)
                            "Vista nocturna demo activada"
                        else
                            "Vista nocturna demo desactivada"
                    }

                    CameraControl(
                        label = "Luz",
                        active = flashlight,
                        enabled = enabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        flashlight = !flashlight
                        message = if (flashlight)
                            "Luz demo activada"
                        else
                            "Luz demo desactivada"
                    }
                }

                OutlinedButton(
                    onClick = { fullscreen = !fullscreen },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled
                ) {
                    Text(
                        if (fullscreen)
                            "Restaurar vista"
                        else
                            "Ampliar vista previa"
                    )
                }

                if (message.isNotBlank()) {
                    Text(
                        message,
                        color = detailGreen,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = detailSurface
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "ESTADO DEL DISPOSITIVO",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Estado: " +
                                if (enabled) "En línea"
                                else "Desconectada",
                            color = if (enabled)
                                detailGreen else detailRed
                        )

                        Text(
                            "Señal: " +
                                if (enabled)
                                    "Disponible (simulada)"
                                else
                                    "Sin conexión",
                            color = Color.LightGray
                        )

                        Text(
                            "Almacenamiento: No conectado",
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraControl(
    label: String,
    active: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier.height(48.dp),
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (active)
                detailRed else detailSurface
        )
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
