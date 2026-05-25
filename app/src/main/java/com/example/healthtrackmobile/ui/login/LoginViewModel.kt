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

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val usuario: Usuario) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(
    private val authService: AuthService = AuthService()
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: CharSequence) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { LoginUiState.Error("El correo y la contraseña son obligatorios.") }
            return
        }

        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            authService.login(email, password)
                .onSuccess { usuario ->
                    _uiState.value = LoginUiState.Success(usuario)
                }
                .onFailure { exception ->
                    val errorMsg = when (exception.message) {
                        "CUENTA_INACTIVA" -> "Tu cuenta está pendiente de validación por el administrador."
                        else -> exception.message ?: "Usuario no encontrado o contraseña incorrecta"
                    }
                    _uiState.value = LoginUiState.Error(errorMsg)
                }
        }
    }

    fun clearError() {
        if (_uiState.value is LoginUiState.Error) {
            _uiState.value = LoginUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
