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
import androidx.compose.ui.unit.dp

data class DemoCamera(
    val id: String,
    val name: String,
    val zone: String,
    val online: Boolean
)

val demoCameras = listOf(
    DemoCamera(
        "CAM-01", "Entrada Principal", "Zona Sur", true
    ),
    DemoCamera(
        "CAM-04", "Perímetro Norte", "Zona Sur", true
    ),
    DemoCamera(
        "CAM-07", "Bodega A", "Zona Sur", false
    ),
    DemoCamera(
        "CAM-02", "Estacionamiento", "Zona Sur", true
    )
)

private val cameraBackground = Color(0xFF0B1426)
private val cameraSurface = Color(0xFF192740)
private val cameraBlue = Color(0xFF2864E8)
private val cameraGreen = Color(0xFF20C997)
private val cameraRed = Color(0xFFFF5252)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamerasScreen(
    onBack: () -> Unit,
    onCameraClick: (String) -> Unit
) {
    Scaffold(
        containerColor = cameraBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text("Mis Cámaras")
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cameraBackground,
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {
                Text(
                    "MONITOREO DE SEGURIDAD",
                    color = cameraBlue,
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "${demoCameras.count { it.online }} de " +
                        "${demoCameras.size} cámaras en línea",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Vista de demostración · Sin video real",
                    color = Color.LightGray
                )
            }

            items(demoCameras, key = { it.id }) { camera ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onCameraClick(camera.id)
                        },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cameraSurface
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(155.dp)
                                .background(
                                    cameraBackground,
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {

                            Column(
                                horizontalAlignment =
                                    Alignment.CenterHorizontally
                            ) {
                                Text(
                                    if (camera.online)
                                        "▣"
                                    else
                                        "⊘",
                                    color = if (camera.online)
                                        cameraBlue
                                    else
                                        cameraRed,
                                    style = MaterialTheme.typography
                                        .headlineLarge
                                )

                                Text(
                                    if (camera.online)
                                        "VISTA PREVIA DEMO"
                                    else
                                        "SIN CONEXIÓN",
                                    color = Color.LightGray
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column {
                                Text(
                                    camera.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    "${camera.id} · ${camera.zone}",
                                    color = Color.LightGray
                                )
                            }

                            Text(
                                if (camera.online)
                                    "● En línea"
                                else
                                    "● Offline",
                                color = if (camera.online)
                                    cameraGreen
                                else
                                    cameraRed
                            )
                        }

                        Text(
                            "Ver detalle",
                            color = cameraBlue
                        )
                    }
                }
            }
        }
    }
}
