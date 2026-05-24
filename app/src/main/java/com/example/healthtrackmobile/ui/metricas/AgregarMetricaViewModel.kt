package com.example.healthtrackmobile.ui.metricas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtrackmobile.model.Metrica
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface AgregarMetricaUiState {
    data object Idle : AgregarMetricaUiState
    data object Loading : AgregarMetricaUiState
    data object Success : AgregarMetricaUiState
    data class Error(val message: String) : AgregarMetricaUiState
}

class AgregarMetricaViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow<AgregarMetricaUiState>(AgregarMetricaUiState.Idle)
    val uiState: StateFlow<AgregarMetricaUiState> = _uiState.asStateFlow()

    fun guardarMetrica(
        userId: String,
        tipo: String,
        valorStr: String,
        valorSecundarioStr: String,
        comentario: String
    ) {
        if (valorStr.isBlank()) {
            _uiState.value = AgregarMetricaUiState.Error("El valor de la medición es obligatorio.")
            return
        }

        val valor = valorStr.toDoubleOrNull()
        if (valor == null || valor <= 0) {
            _uiState.value = AgregarMetricaUiState.Error("El valor debe ser un número positivo.")
            return
        }

        var valorSecundario = 0.0
        if (tipo == "PRESION") {
            if (valorSecundarioStr.isBlank()) {
                _uiState.value = AgregarMetricaUiState.Error("La presión diastólica es obligatoria.")
                return
            }
            val valSec = valorSecundarioStr.toDoubleOrNull()
            if (valSec == null || valSec <= 0) {
                _uiState.value = AgregarMetricaUiState.Error("La presión diastólica debe ser un número positivo.")
                return
            }
            valorSecundario = valSec
        }

        _uiState.value = AgregarMetricaUiState.Loading

        viewModelScope.launch {
            try {
                val nuevaMetrica = Metrica(
                    pacienteId = userId,
                    tipo = tipo,
                    valor = valor,
                    valorSecundario = valorSecundario,
                    comentario = comentario.trim().ifBlank { null },
                    timestamp = System.currentTimeMillis()
                )

                // Guardar en Firestore (genera un ID aleatorio automáticamente)
                db.collection("metricas")
                    .add(nuevaMetrica)
                    .await()

                _uiState.value = AgregarMetricaUiState.Success
            } catch (e: Exception) {
                _uiState.value = AgregarMetricaUiState.Error(
                    e.message ?: "Ocurrió un error al guardar el registro en la base de datos."
                )
            }
        }
    }

    fun clearError() {
        if (_uiState.value is AgregarMetricaUiState.Error) {
            _uiState.value = AgregarMetricaUiState.Idle
        }
    }
}
