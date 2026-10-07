package com.example.modoguardian.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.Event
import com.example.modoguardian.model.Prioridad

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorPrioridad = when (event.prioridad) {
        Prioridad.CRITICA -> MaterialTheme.colorScheme.error
        Prioridad.ALTA -> Color(0xFFFF9800)
        Prioridad.MEDIA -> Color(0xFFFFC107)
        Prioridad.BAJA -> Color(0xFF4CAF50)
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = event.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = { },
                    label = { Text(event.prioridad.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = colorPrioridad.copy(alpha = 0.15f),
                        labelColor = colorPrioridad
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.ubicacion,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                SuggestionChip(
                    onClick = { },
                    label = { Text(event.estado.name.replace("_", " ")) }
                )
            }
        }
    }
}