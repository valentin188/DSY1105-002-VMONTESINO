package com.example.modoguardian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.modoguardian.data.GuardianRepository
import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.Event
import com.example.modoguardian.model.MotivoCierre
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
    private val _saving =
        MutableStateFlow(false)
    val saving: StateFlow<Boolean> =
        _saving.asStateFlow()
    private val _operationMessage =
        MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> =
        _operationMessage.asStateFlow()
    private var observationJob: Job? = null

    init {
        cargarEventos()
    }

    fun cargarEventos() {
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            _eventsState.value = UiState.Loading
            try {
                repository.inicializarEventos()
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

    fun seleccionarEventoPorId(
        id: String
    ) {
        val eventos =
            (_eventsState.value as? UiState.Success)
                ?.data

        _selectedEvent.value =
            eventos?.find {
                it.id == id
            }
    }

    fun cambiarEstadoEvento(
        eventoId: String,
        nuevoEstado: EstadoEvento,
        fotoUri: String? = null,
        motivoCierre: MotivoCierre? = null
    ) {
        if (_saving.value) {
            _operationError.value =
                "Hay una actualización en curso"
            return
        }

        viewModelScope.launch {
            _saving.value = true
            _operationError.value = null
            _operationMessage.value = null

            try {
                val actualizado =
                    repository.actualizarEstadoEvento(
                        eventoId = eventoId,
                        nuevoEstado = nuevoEstado,
                        fotoUri = fotoUri,
                        motivoCierre = motivoCierre
                    )

                if (actualizado) {
                    _operationMessage.value =
                        when {
                            motivoCierre ==
                                MotivoCierre.FALSA_ALARMA ->
                                "Falsa alarma registrada correctamente"
                            motivoCierre ==
                                MotivoCierre.INCIDENTE_SOLUCIONADO ->
                                "Incidente resuelto correctamente"
                            nuevoEstado ==
                                EstadoEvento.EN_PROCESO ->
                                "Alerta confirmada correctamente"
                            nuevoEstado ==
                                EstadoEvento.PENDIENTE ->
                                "Evento marcado como pendiente"
                            else ->
                                "Estado actualizado correctamente"
                        }

                } else {
                    _operationError.value =
                        "No fue posible actualizar el evento. " +
                            "Verifica que exista y que el " +
                            "motivo de cierre sea válido."
                }

            } catch (error: Exception) {
                _operationError.value =
                    error.message
                        ?: "No se pudo actualizar el evento"
            } finally {
                _saving.value = false
            }
        }
    }

    fun confirmarAlerta(
        eventoId: String
    ) {
        cambiarEstadoEvento(
            eventoId = eventoId,
            nuevoEstado = EstadoEvento.EN_PROCESO
        )
    }

    fun resolverIncidente(
        eventoId: String,
        fotoUri: String? = null
    ) {
        cambiarEstadoEvento(
            eventoId = eventoId,
            nuevoEstado = EstadoEvento.RESUELTO,
            fotoUri = fotoUri,
            motivoCierre =
                MotivoCierre.INCIDENTE_SOLUCIONADO
        )
    }

    fun registrarFalsaAlarma(
        eventoId: String,
        fotoUri: String? = null
    ) {
        cambiarEstadoEvento(
            eventoId = eventoId,
            nuevoEstado = EstadoEvento.RESUELTO,
            fotoUri = fotoUri,
            motivoCierre = MotivoCierre.FALSA_ALARMA
        )
    }

    fun limpiarError() {
        _operationError.value = null
    }

    fun limpiarMensaje() {
        _operationMessage.value = null
    }

    fun limpiarSeleccion() {
        _selectedEvent.value = null
    }

    override fun onCleared() {
        observationJob?.cancel()
        super.onCleared()
    }
}
