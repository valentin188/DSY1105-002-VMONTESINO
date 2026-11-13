package com.example.modoguardian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modoguardian.data.GuardianRepository
import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.Event
import com.example.modoguardian.ui.state.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class EventsViewModel(
    private val repository: GuardianRepository = GuardianRepository()
) : ViewModel() {

    private val _eventsState =
        MutableStateFlow<UiState<List<Event>>>(
            UiState.Loading
        )

    val eventsState: StateFlow<UiState<List<Event>>> =
        _eventsState.asStateFlow()

    private val _selectedEvent =
        MutableStateFlow<Event?>(null)

    val selectedEvent: StateFlow<Event?> =
        _selectedEvent.asStateFlow()

    private val _operationError =
        MutableStateFlow<String?>(null)

    val operationError: StateFlow<String?> =
        _operationError.asStateFlow()

    private var observationJob: Job? = null

    init {
        cargarEventos()
    }

    fun cargarEventos() {

        observationJob?.cancel()

        observationJob = viewModelScope.launch {

            _eventsState.value = UiState.Loading

            try {

                // Crea datos iniciales solamente
                // cuando la base está vacía.
                repository.inicializarEventos()

                // Room notifica cada cambio.
                repository.observarEventos()
                    .catch { error ->
                        _eventsState.value =
                            UiState.Error(
                                error.message
                                    ?: "Error al observar eventos"
                            )
                    }
                    .collect { eventos ->

                        _eventsState.value =
                            UiState.Success(eventos)

                        // Mantener sincronizado
                        // el evento seleccionado.
                        val selectedId =
                            _selectedEvent.value?.id

                        if (selectedId != null) {
                            _selectedEvent.value =
                                eventos.find {
                                    it.id == selectedId
                                }
                        }
                    }

            } catch (error: Exception) {

                _eventsState.value =
                    UiState.Error(
                        error.message
                            ?: "No se pudieron cargar los eventos"
                    )
            }
        }
    }

    fun seleccionarEventoPorId(id: String) {

        val eventos =
            (_eventsState.value as? UiState.Success)
                ?.data

        _selectedEvent.value =
            eventos?.find { it.id == id }
    }

    fun cambiarEstadoEvento(
        eventoId: String,
        nuevoEstado: EstadoEvento,
        fotoUri: String? = null
    ) {

        viewModelScope.launch {

            _operationError.value = null

            try {

                val actualizado =
                    repository.actualizarEstadoEvento(
                        eventoId = eventoId,
                        nuevoEstado = nuevoEstado,
                        fotoUri = fotoUri
                    )

                if (!actualizado) {
                    _operationError.value =
                        "No se encontró el evento solicitado"
                }

                // No recargamos manualmente:
                // Room emite los cambios por Flow.

            } catch (error: Exception) {

                _operationError.value =
                    error.message
                        ?: "No se pudo actualizar el evento"
            }
        }
    }

    fun limpiarError() {
        _operationError.value = null
    }

    fun limpiarSeleccion() {
        _selectedEvent.value = null
    }

    override fun onCleared() {
        observationJob?.cancel()
        super.onCleared()
    }
}
