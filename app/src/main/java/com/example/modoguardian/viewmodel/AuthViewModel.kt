package com.example.modoguardian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modoguardian.data.GuardianRepository
import com.example.modoguardian.model.User
import com.example.modoguardian.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: GuardianRepository = GuardianRepository()
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _usersState = MutableStateFlow<UiState<List<User>>>(UiState.Loading)
    val usersState: StateFlow<UiState<List<User>>> = _usersState.asStateFlow()

    init {
        cargarUsuarios()
    }

    fun cargarUsuarios() {
        viewModelScope.launch {
            _usersState.value = UiState.Loading
            try {
                val usuarios = repository.obtenerUsuarios()
                _usersState.value = UiState.Success(usuarios)
                if (usuarios.isNotEmpty() && _currentUser.value == null) {
                    _currentUser.value = usuarios.first()
                }
            } catch (e: Exception) {
                _usersState.value = UiState.Error(e.message ?: "Error al cargar usuarios")
            }
        }
    }

    fun seleccionarUsuario(usuario: User) {
        _currentUser.value = usuario
    }

    fun actualizarFotoPerfil(uriString: String) {
        _currentUser.value = _currentUser.value?.copy(fotoPerfilUri = uriString)
    }
}