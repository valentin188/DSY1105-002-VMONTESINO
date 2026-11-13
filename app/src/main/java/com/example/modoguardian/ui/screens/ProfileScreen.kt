package com.example.modoguardian.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.User
import com.example.modoguardian.ui.components.ImagenInteligente
import com.example.modoguardian.ui.utils.EvidenceStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val profileBg = Color(0xFF0B1426)
private val profileCard = Color(0xFF1D2A40)
private val profileBlue = Color(0xFF2864E8)
private val profileGray = Color(0xFF9BAAC3)
private val profileGreen = Color(0xFF20C997)
private val profileRed = Color(0xFFF04452)
private val profileDivider = Color(0xFF304058)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User?,
    onBack: () -> Unit,
    onPhotoUpdated: (String) -> Unit,
    onLogout: () -> Unit,
    onOpenCameras: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val preferences = remember {
        context.getSharedPreferences(
            "insercode_profile_preferences",
            0
        )
    }

    var notifications by remember(user?.id) {
        mutableStateOf(
            preferences.getBoolean(
                "${user?.id}_notifications",
                true
            )
        )
    }

    var criticalAlerts by remember(user?.id) {
        mutableStateOf(
            preferences.getBoolean(
                "${user?.id}_critical_alerts",
                true
            )
        )
    }

    var darkMode by remember(user?.id) {
        mutableStateOf(
            preferences.getBoolean(
                "${user?.id}_dark_mode",
                true
            )
        )
    }

    var photoUri by remember(
        user?.id,
        user?.fotoPerfilUri
    ) {
        mutableStateOf(
            user?.fotoPerfilUri?.let(Uri::parse)
        )
    }

    var savingPhoto by remember {
        mutableStateOf(false)
    }

    var confirmLogout by remember {
        mutableStateOf(false)
    }

    var infoDialog by remember {
        mutableStateOf<Pair<String, String>?>(null)
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { selectedUri ->

        if (selectedUri != null && user != null) {
            scope.launch {
                savingPhoto = true

                try {
                    val savedUri = withContext(
                        Dispatchers.IO
                    ) {
                        EvidenceStorage.save(
                            context,
                            selectedUri
                        )
                    }

                    photoUri = Uri.parse(savedUri)
                    onPhotoUpdated(savedUri)

                    Toast.makeText(
                        context,
                        "Fotografía actualizada",
                        Toast.LENGTH_SHORT
                    ).show()

                } catch (error: Exception) {
                    Toast.makeText(
                        context,
                        "No se pudo guardar la fotografía",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    savingPhoto = false
                }
            }
        }
    }

    Scaffold(
        containerColor = profileBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Mi Perfil",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Cuenta y preferencias",
                            color = profileGray,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = profileBg,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // PERFIL DEL GUARDIA
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = profileCard
                ),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    ImagenInteligente(
                        fotoUri = photoUri
                    )

                    Text(
                        user?.nombre ?: "Guardián",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        user?.email ?: "Sin correo registrado",
                        color = profileGray,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = profileGreen,
                            modifier = Modifier.size(17.dp)
                        )

                        Text(
                            user?.rol?.name ?: "SIN ROL",
                            color = profileGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            photoPicker.launch("image/*")
                        },
                        enabled = user != null && !savingPhoto
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (savingPhoto)
                                "Guardando..."
                            else
                                "Cambiar fotografía"
                        )
                    }

                    Text(
                        "Cuenta de demostración · INSERCODE",
                        color = profileGray,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // ESTADO DE LA CUENTA
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = profileCard
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatistic(
                        "4",
                        "Cámaras demo",
                        profileBlue
                    )

                    ProfileStatistic(
                        "Local",
                        "Sesión",
                        profileGreen
                    )

                    ProfileStatistic(
                        "Demo",
                        "Seguridad",
                        profileBlue
                    )
                }
            }

            // MI CUENTA
            ProfileSectionTitle("MI CUENTA")

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = profileCard
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column {

                    ProfileActionRow(
                        icon = Icons.Default.Videocam,
                        title = "Mis cámaras asignadas",
                        subtitle = "Cámaras de demostración",
                        onClick = {
                            if (onOpenCameras != null) {
                                onOpenCameras()
                            } else {
                                infoDialog = Pair(
                                    "Mis cámaras",
                                    "Las cámaras se encuentran en " +
                                        "la sección Cámaras del sistema."
                                )
                            }
                        }
                    )

                    ProfileDivider()

                    ProfileActionRow(
                        icon = Icons.Default.History,
                        title = "Historial de turnos",
                        subtitle = "Eventos y registros",
                        onClick = {
                            infoDialog = Pair(
                                "Historial",
                                "Puedes consultar los eventos " +
                                    "registrados en la sección Eventos. " +
                                    "El historial independiente por " +
                                    "turnos todavía no está implementado."
                            )
                        }
                    )

                    ProfileDivider()

                    ProfileActionRow(
                        icon = Icons.Default.Badge,
                        title = "Credencial de guardia",
                        subtitle = "Información del usuario",
                        onClick = {
                            infoDialog = Pair(
                                "Credencial",
                                "Usuario: ${user?.nombre ?: "No informado"}\n" +
                                    "Rol: ${user?.rol?.name ?: "No informado"}\n\n" +
                                    "No existe una credencial oficial " +
                                    "verificada en este prototipo."
                            )
                        }
                    )
                }
            }

            // SEGURIDAD
            ProfileSectionTitle("SEGURIDAD")

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = profileCard
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column {

                    ProfileActionRow(
                        icon = Icons.Default.Key,
                        title = "Cambiar contraseña",
                        subtitle = "Credenciales de demostración",
                        onClick = {
                            infoDialog = Pair(
                                "Cambiar contraseña",
                                "Esta aplicación utiliza usuarios " +
                                    "de prueba. Para modificar " +
                                    "contraseñas de forma segura " +
                                    "necesitamos autenticación " +
                                    "mediante un servidor."
                            )
                        }
                    )

                    ProfileDivider()

                    ProfileActionRow(
                        icon = Icons.Default.Devices,
                        title = "Sesiones activas",
                        subtitle = "Sesión actual de demostración",
                        onClick = {
                            infoDialog = Pair(
                                "Sesiones activas",
                                "INSERCODE mantiene una sesión " +
                                    "local durante su ejecución. " +
                                    "No existe administración de " +
                                    "sesiones remotas."
                            )
                        }
                    )

                    ProfileDivider()

                    ProfileActionRow(
                        icon = Icons.Default.VerifiedUser,
                        title = "Verificación en 2 pasos",
                        subtitle = "No configurada",
                        onClick = {
                            infoDialog = Pair(
                                "Verificación en 2 pasos",
                                "La autenticación en dos pasos " +
                                    "requiere un servicio de " +
                                    "identidad externo. Todavía " +
                                    "no está disponible."
                            )
                        }
                    )

                    ProfileDivider()

                    ProfileSwitchRow(
                        icon = Icons.Default.Notifications,
                        title = "Preferencia de notificaciones",
                        subtitle = "Configuración local, sin servicio push",
                        checked = notifications,
                        onCheckedChange = {
                            notifications = it
                            preferences.edit()
                                .putBoolean(
                                    "${user?.id}_notifications",
                                    it
                                )
                                .apply()
                        }
                    )

                    ProfileDivider()

                    ProfileSwitchRow(
                        icon = Icons.Default.Warning,
                        title = "Preferencia de alertas críticas",
                        subtitle = "No modifica protocolos de seguridad",
                        checked = criticalAlerts,
                        onCheckedChange = {
                            criticalAlerts = it
                            preferences.edit()
                                .putBoolean(
                                    "${user?.id}_critical_alerts",
                                    it
                                )
                                .apply()
                        }
                    )
                }
            }

            // APLICACIÓN
            ProfileSectionTitle("APLICACIÓN")

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = profileCard
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column {

                    ProfileSwitchRow(
                        icon = Icons.Default.DarkMode,
                        title = "Preferencia de modo oscuro",
                        subtitle = "Guardada; tema global pendiente",
                        checked = darkMode,
                        onCheckedChange = {
                            darkMode = it
                            preferences.edit()
                                .putBoolean(
                                    "${user?.id}_dark_mode",
                                    it
                                )
                                .apply()
                        }
                    )

                    ProfileDivider()

                    ProfileActionRow(
                        icon = Icons.Default.HelpOutline,
                        title = "Ayuda y soporte",
                        subtitle = "Guía de uso de INSERCODE",
                        onClick = {
                            infoDialog = Pair(
                                "Ayuda INSERCODE",
                                "Desde Inicio puedes revisar " +
                                    "el estado general.\n\n" +
                                    "En Cámaras puedes consultar " +
                                    "dispositivos simulados.\n\n" +
                                    "En Alertas puedes gestionar " +
                                    "incidentes.\n\n" +
                                    "En Eventos puedes consultar " +
                                    "el historial y exportar un PDF."
                            )
                        }
                    )

                    ProfileDivider()

                    ProfileActionRow(
                        icon = Icons.Default.Description,
                        title = "Privacidad y seguridad",
                        subtitle = "Información de almacenamiento",
                        onClick = {
                            infoDialog = Pair(
                                "Privacidad",
                                "Los eventos se guardan " +
                                    "localmente mediante Room.\n\n" +
                                    "Las evidencias seleccionadas " +
                                    "se copian al almacenamiento " +
                                    "interno de Android.\n\n" +
                                    "El prototipo no envía " +
                                    "información a una central " +
                                    "de vigilancia real."
                            )
                        }
                    )
                }
            }

            // CERRAR SESIÓN
            OutlinedButton(
                onClick = {
                    confirmLogout = true
                },
                enabled = user != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = profileRed
                ),
                shape = RoundedCornerShape(17.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    "Cerrar sesión",
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                "INSERCODE · Prototipo académico",
                modifier = Modifier.fillMaxWidth(),
                color = profileGray,
                style = MaterialTheme.typography.labelSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))
        }
    }

    // DIÁLOGOS INFORMATIVOS
    infoDialog?.let { information ->
        AlertDialog(
            onDismissRequest = {
                infoDialog = null
            },
            title = {
                Text(information.first)
            },
            text = {
                Text(information.second)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        infoDialog = null
                    }
                ) {
                    Text("Entendido")
                }
            }
        )
    }

    // CONFIRMAR CIERRE DE SESIÓN
    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = {
                confirmLogout = false
            },
            title = {
                Text("Cerrar sesión")
            },
            text = {
                Text(
                    "¿Estás seguro de que deseas " +
                        "salir de INSERCODE?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmLogout = false
                        onLogout()
                    }
                ) {
                    Text(
                        "Cerrar sesión",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmLogout = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ProfileSectionTitle(
    title: String
) {
    Text(
        title,
        color = profileGray,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
private fun ProfileStatistic(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            value,
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            label,
            color = profileGray,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun ProfileActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    Color(0xFF2B3B54),
                    RoundedCornerShape(13.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFFD0DDF2)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                title,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )

            Text(
                subtitle,
                color = profileGray,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = profileGray
        )
    }
}

@Composable
private fun ProfileSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    Color(0xFF2B3B54),
                    RoundedCornerShape(13.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                title,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )

            Text(
                subtitle,
                color = profileGray,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = profileBlue
            )
        )
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(
        color = profileDivider,
        thickness = 0.5.dp
    )
}
