
package com.example.modoguardian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modoguardian.data.GuardianRepository
import com.example.modoguardian.model.Role
import com.example.modoguardian.model.User
import com.example.modoguardian.navigation.Screen
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

    private val _usersState =
        MutableStateFlow<UiState<List<User>>>(UiState.Loading)

    val usersState: StateFlow<UiState<List<User>>> =
        _usersState.asStateFlow()

    init {
        cargarUsuarios()
    }

    fun cargarUsuarios() {
        viewModelScope.launch {
            _usersState.value = UiState.Loading

            try {
                val usuarios = repository.obtenerUsuarios()
                _usersState.value = UiState.Success(usuarios)

                // No iniciar sesión automáticamente.
            } catch (e: Exception) {
                _usersState.value = UiState.Error(
                    e.message ?: "Error al cargar usuarios"
                )
            }
        }
    }

    // Identifica al usuario después de validar el Login.
    suspend fun iniciarSesionDemo(ruta: String): Boolean {
        val rol = when (ruta) {
            Screen.HomeAdmin.route -> Role.ADMINISTRADOR
            Screen.HomeSupervisor.route -> Role.SUPERVISOR
            Screen.HomeOperador.route -> Role.OPERADOR
            else -> return false
        }

        return try {
            val usuario = repository.obtenerUsuarios()
                .firstOrNull { it.rol == rol }

            _currentUser.value = usuario
            usuario != null
        } catch (e: Exception) {
            _currentUser.value = null
            false
        }
    }

    fun seleccionarUsuario(usuario: User) {
        _currentUser.value = usuario
    }

    fun actualizarFotoPerfil(uriString: String) {
        _currentUser.value = _currentUser.value?.copy(
            fotoPerfilUri = uriString
        )
    }

    fun cerrarSesion() {
        _currentUser.value = null
    }
}
