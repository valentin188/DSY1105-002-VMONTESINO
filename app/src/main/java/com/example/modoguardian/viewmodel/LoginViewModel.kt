package com.example.modoguardian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modoguardian.model.LoginErrores
import com.example.modoguardian.model.LoginUiState
import com.example.modoguardian.navigation.NavigationEvent
import com.example.modoguardian.navigation.Screen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    // Estado observable de la interfaz
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Flujo de eventos de navegación único
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    fun onEmailChange(nuevoEmail: String) {
        _uiState.update { current ->
            val updated = current.copy(
                email = nuevoEmail,
                errores = current.errores.copy(emailError = null, generalError = null)
            )
            updated.copy(isLoginEnabled = validarFormulario(updated))
        }
    }

    fun onPasswordChange(nuevaPass: String) {
        _uiState.update { current ->
            val updated = current.copy(
                password = nuevaPass,
                errores = current.errores.copy(passwordError = null, generalError = null)
            )
            updated.copy(isLoginEnabled = validarFormulario(updated))
        }
    }

    /**
     * Valida que el email tenga formato correcto y la contraseña mínimo 6 caracteres.
     */
    fun validarFormulario(state: LoginUiState): Boolean {
        val emailValido = android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()
        val passValida = state.password.length >= 6
        return emailValido && passValida
    }

    /**
     * Autenticación con los 3 usuarios de prueba requeridos por la pauta.
     */
    fun autenticar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val email = _uiState.value.email.trim()
            val pass = _uiState.value.password.trim()

            when {
                email == "admin@guardian.test" && pass == "123456" -> {
                    _uiState.update { it.copy(rolLogueado = "Admin", isLoading = false) }
                    _navigationEvent.emit(NavigationEvent.NavigateTo(Screen.HomeAdmin.route))
                }
                email == "supervisor@guardian.test" && pass == "123456" -> {
                    _uiState.update { it.copy(rolLogueado = "Supervisor", isLoading = false) }
                    _navigationEvent.emit(NavigationEvent.NavigateTo(Screen.HomeSupervisor.route))
                }
                email == "operador@guardian.test" && pass == "123456" -> {
                    _uiState.update { it.copy(rolLogueado = "Operador", isLoading = false) }
                    _navigationEvent.emit(NavigationEvent.NavigateTo(Screen.HomeOperador.route))
                }
                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errores = LoginErrores(generalError = "Credenciales incorrectas")
                        )
                    }
                }
            }
        }
    }
}