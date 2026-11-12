package com.example.modoguardian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.example.modoguardian.ui.screens.EventsScreen
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.viewmodel.AuthViewModel
import com.example.modoguardian.viewmodel.EventsViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val eventsViewModel: EventsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val currentUser by authViewModel.currentUser.collectAsState()
            val eventsState by eventsViewModel.eventsState.collectAsState()

            ModoGuardianTheme {
                EventsScreen(
                    currentUser = currentUser,
                    eventsState = eventsState,
                    onEventClick = { eventId ->
                        eventsViewModel.seleccionarEventoPorId(eventId)
                    },
                    onNavigateToProfile = {
                    },
                    onNavigateToAdmin = {
                    }
                )
            }
        }
    }
}
