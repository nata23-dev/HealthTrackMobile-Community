package com.example.healthtrackmobile.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtrackmobile.model.Usuario
import com.example.healthtrackmobile.service.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface RegisterUiState {
    data object Idle : RegisterUiState
    data object Loading : RegisterUiState
    data class Success(val usuario: Usuario) : RegisterUiState
    data class Error(val message: String) : RegisterUiState
}

class RegisterViewModel(
    private val authService: AuthService = AuthService()
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun registrar(nombre: String, email: String, passwordRaw: String, passwordConfirm: String) {
        if (nombre.isBlank() || email.isBlank() || passwordRaw.isBlank() || passwordConfirm.isBlank()) {
            _uiState.update { RegisterUiState.Error("Todos los campos son obligatorios.") }
            return
        }

        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$")
        if (!emailRegex.matches(email.trim())) {
            _uiState.update { RegisterUiState.Error("El correo no tiene un formato válido.") }
            return
        }

        if (passwordRaw != passwordConfirm) {
            _uiState.update { RegisterUiState.Error("La confirmación de la contraseña no coincide.") }
            return
        }

        if (passwordRaw.length < 8) {
            _uiState.update { RegisterUiState.Error("La contraseña debe tener al menos 8 caracteres.") }
            return
        }

        _uiState.value = RegisterUiState.Loading

        viewModelScope.launch {
            authService.register(nombre, email, passwordRaw)
                .onSuccess { usuario ->
                    _uiState.value = RegisterUiState.Success(usuario)
                }
                .onFailure { exception ->
                    _uiState.value = RegisterUiState.Error(exception.message ?: "Error al registrar el usuario.")
                }
        }
    }

    fun clearError() {
        if (_uiState.value is RegisterUiState.Error) {
            _uiState.value = RegisterUiState.Idle
        }
    }
}
