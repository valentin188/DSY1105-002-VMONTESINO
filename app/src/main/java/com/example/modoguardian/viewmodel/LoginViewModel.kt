package com.example.modoguardian.viewmodel

import android.util.Patterns
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

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

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

    fun validarFormulario(state: LoginUiState): Boolean {
        val emailValido = Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches()
        val passValida = state.password.length >= 6
        return emailValido && passValida
    }

    fun autenticar() {
        val state = _uiState.value
        if (state.isLoading) return

        // El botón permanece disponible; aquí se valida al presionarlo.
        if (!validarFormulario(state)) {
            _uiState.update {
                it.copy(
                    errores = LoginErrores(
                        emailError = if (!Patterns.EMAIL_ADDRESS.matcher(it.email.trim()).matches())
                            "Ingresa un correo válido" else null,
                        passwordError = if (it.password.length < 6)
                            "La contraseña debe tener al menos 6 caracteres" else null
                    )
                )
            }
            return
        }
                
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errores = LoginErrores()) }

            val email = _uiState.value.email.trim()
            val pass = _uiState.value.password

            val destino = when {
                email == "admin@guardian.test" && pass == "123456" ->
                    Pair("Admin", Screen.HomeAdmin.route)
                email == "supervisor@guardian.test" && pass == "123456" ->
                    Pair("Supervisor", Screen.HomeSupervisor.route)
                email == "operador@guardian.test" && pass == "123456" ->
                    Pair("Operador", Screen.HomeOperador.route)
                else -> null
            }

            if (destino != null) {
                _uiState.update {
                    it.copy(rolLogueado = destino.first, isLoading = false)
                }
                _navigationEvent.emit(NavigationEvent.NavigateTo(destino.second))
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errores = LoginErrores(generalError = "Credenciales incorrectas")
                    )
                }
            }
        }
    }

    // Regresa al formulario sin credenciales ni rol autenticado.
    fun cerrarSesion() {
        _uiState.value = LoginUiState()
    }
}
