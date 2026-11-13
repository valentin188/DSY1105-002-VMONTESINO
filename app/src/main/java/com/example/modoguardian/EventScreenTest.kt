package com.example.modoguardian

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.Event
import com.example.modoguardian.model.Prioridad
import com.example.modoguardian.model.Role
import com.example.modoguardian.model.User
import com.example.modoguardian.ui.screens.EventsScreen
import com.example.modoguardian.ui.state.UiState
import org.junit.Rule
import org.junit.Test

class EventScreenTest {

    @get:Rule
    val composeRule =
        createAndroidComposeRule<ComponentActivity>()

    private val usuarioPrueba = User(
        id = "1",
        nombre = "Guardia de prueba",
        email = "operador@guardian.test",
        rol = Role.OPERADOR
    )

    private val eventoPrueba = Event(
        id = "EVT-001",
        titulo = "Movimiento sospechoso",
        descripcion = "Movimiento detectado en entrada",
        ubicacion = "Entrada principal",
        prioridad = Prioridad.CRITICA,
        estado = EstadoEvento.PENDIENTE,
        fechaHora = "2026-11-12 10:00"
    )

    private fun mostrarPantalla() {
        composeRule.setContent {
            EventsScreen(
                currentUser = usuarioPrueba,
                eventsState = UiState.Success(
                    listOf(eventoPrueba)
                ),
                onEventClick = {},
                onNavigateToProfile = {},
                onNavigateToAdmin = {}
            )
        }
    }

    @Test
    fun validarTituloHistorial() {
        mostrarPantalla()

        composeRule
            .onNodeWithText("Historial")
            .assertIsDisplayed()
    }

    @Test
    fun validarResumenDeEventos() {
        mostrarPantalla()

        composeRule
            .onNodeWithText("RESUMEN DE EVENTOS")
            .assertIsDisplayed()
    }

    @Test
    fun validarBuscador() {
        mostrarPantalla()

        composeRule
            .onNodeWithText(
                "Buscar evento, ubicación o código"
            )
            .assertIsDisplayed()
    }
}
