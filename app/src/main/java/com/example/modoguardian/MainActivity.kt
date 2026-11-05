package com.example.modoguardian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.example.modoguardian.data.local.AppDatabase
import com.example.modoguardian.ui.screens.EventsScreen
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.viewmodel.AuthViewModel
import com.example.modoguardian.viewmodel.EventsViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val eventsViewModel: EventsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicialización del Singleton de Room
        val database = AppDatabase.getDatabase(applicationContext)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ModoGuardianTheme {
                EventsScreen(
                    currentUser = authViewModel.currentUser.value,
                    eventsState = eventsViewModel.uiState.value,
                    onEventClick = { eventId -> },
                    onNavigateToProfile = { },
                    onNavigateToAdmin = { }
                )
            }
        }
    }
}
