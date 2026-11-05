package com.example.modoguardian

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.modoguardian.model.Event
import com.example.modoguardian.model.Role
import com.example.modoguardian.model.User
import com.example.modoguardian.ui.screens.EventsScreen
import com.example.modoguardian.ui.state.UiState
import org.junit.Rule
import org.junit.Test

class EventScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun validar_que_el_titulo_se_muestre_en_pantalla() {
        val fakeUser = User(
            id = "1",
            nombre = "Test User",
            correo = "test@example.com",
            rol = Role.USUARIO
        )

        val fakeEvents = listOf(
            Event(
                id = "1",
                titulo = "Alarma Movimiento",
                descripcion = "Movimiento detectado en entrada",
                fechaHora = "2026-03-30 10:00"
            )
        )

        composeRule.setContent {
            EventsScreen(
                currentUser = fakeUser,
                eventsState = UiState.Success(fakeEvents),
                onEventClick = {},
                onNavigateToProfile = {},
                onNavigateToAdmin = {}
            )
        }

        // Valida que la TopAppBar con el texto exacto de tu Composable se muestre
        composeRule.onNodeWithText("Eventos y Alertas").assertIsDisplayed()
    }
}
