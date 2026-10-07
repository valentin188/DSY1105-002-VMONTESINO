package com.example.modoguardian.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.Event
import com.example.modoguardian.model.Role
import com.example.modoguardian.model.User
import com.example.modoguardian.ui.components.EventCard
import com.example.modoguardian.ui.state.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    currentUser: User?,
    eventsState: UiState<List<Event>>,
    onEventClick: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eventos y Alertas") },
                actions = {
                    if (currentUser?.rol == Role.ADMINISTRADOR) {
                        IconButton(onClick = onNavigateToAdmin) {
                            Icon(Icons.Default.Shield, contentDescription = "Panel Admin")
                        }
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (eventsState) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UiState.Error -> Text(
                    text = "Error: ${eventsState.message}",
                    modifier = Modifier.align(Alignment.Center)
                )
                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(eventsState.data) { event ->
                            EventCard(
                                event = event,
                                onClick = { onEventClick(event.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}