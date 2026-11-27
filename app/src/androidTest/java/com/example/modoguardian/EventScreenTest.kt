package com.example.modoguardian

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.modoguardian.ui.screens.EventsScreen
import org.junit.Rule
import org.junit.Test

class EventScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun validar_que_el_titulo_se_muestre_en_pantalla() {
        composeRule.setContent {
            EventsScreen()
        }
        composeRule.onNodeWithText("Eventos").assertIsDisplayed()
    }
}
