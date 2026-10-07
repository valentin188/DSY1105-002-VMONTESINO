package com.example.modoguardian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modoguardian.data.GuardianRepository
import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.Event
import com.example.modoguardian.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventsViewModel(
    private val repository: GuardianRepository = GuardianRepository()
) : ViewModel() {

    private val _eventsState = MutableStateFlow<UiState<List<Event>>>(UiState.Loading)
    val eventsState: StateFlow<UiState<List<Event>>> = _eventsState.asStateFlow()

    private val _selectedEvent = MutableStateFlow<Event?>(null)
    val selectedEvent: StateFlow<Event?> = _selectedEvent.asStateFlow()

    init {
        cargarEventos()
    }

    fun cargarEventos() {
        viewModelScope.launch {
            _eventsState.value = UiState.Loading
            try {
                val eventos = repository.obtenerEventos()
                _eventsState.value = UiState.Success(eventos)
            } catch (e: Exception) {
                _eventsState.value = UiState.Error(e.message ?: "Error al cargar eventos")
            }
        }
    }

    fun seleccionarEventoPorId(id: String) {
        val actual = (_eventsState.value as? UiState.Success)?.data
        _selectedEvent.value = actual?.find { it.id == id }
    }

    fun cambiarEstadoEvento(eventoId: String, nuevoEstado: EstadoEvento, fotoUri: String? = null) {
        viewModelScope.launch {
            val exito = repository.actualizarEstadoEvento(eventoId, nuevoEstado, fotoUri)
            if (exito) {
                cargarEventos()
                _selectedEvent.value = _selectedEvent.value?.copy(
                    estado = nuevoEstado,
                    evidenciaFotoUri = fotoUri ?: _selectedEvent.value?.evidenciaFotoUri
                )
            }
        }
    }
}