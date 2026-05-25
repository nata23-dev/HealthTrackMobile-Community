package com.example.healthtrackmobile.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtrackmobile.model.PerfilPaciente
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed interface PerfilClinicoUiState {
    data object Idle : PerfilClinicoUiState
    data object Loading : PerfilClinicoUiState
    data class Success(val perfil: PerfilPaciente) : PerfilClinicoUiState
    data object SavedSuccess : PerfilClinicoUiState
    data class Error(val message: String) : PerfilClinicoUiState
}

class PerfilClinicoViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow<PerfilClinicoUiState>(PerfilClinicoUiState.Idle)
    val uiState: StateFlow<PerfilClinicoUiState> = _uiState.asStateFlow()

    fun cargarPerfil(userId: String) {
        _uiState.value = PerfilClinicoUiState.Loading
        viewModelScope.launch {
            try {
                val doc = withContext(Dispatchers.IO) {
                    db.collection("perfiles_pacientes")
                        .document(userId)
                        .get()
                        .await()
                }

                if (doc.exists()) {
                    val perfil = doc.toObject(PerfilPaciente::class.java) ?: PerfilPaciente(id = userId)
                    perfil.id = doc.id
                    _uiState.value = PerfilClinicoUiState.Success(perfil)
                } else {
                    _uiState.value = PerfilClinicoUiState.Success(PerfilPaciente(id = userId))
                }
            } catch (e: Exception) {
                _uiState.value = PerfilClinicoUiState.Error(
                    e.message ?: "Error al obtener la ficha clínica desde el servidor."
                )
            }
        }
    }

    fun guardarPerfil(
        userId: String,
        grupoSanguineo: String,
        alergias: String,
        fechaNacimiento: String,
        direccion: String,
        estaturaStr: String,
        antecedentes: String
    ) {
        val estatura = estaturaStr.toDoubleOrNull()
        if (estaturaStr.isNotBlank() && (estatura == null || estatura <= 0)) {
            _uiState.value = PerfilClinicoUiState.Error("La estatura debe ser un número positivo.")
            return
        }

        if (fechaNacimiento.isNotBlank()) {
            val dateRegex = """^\d{4}-\d{2}-\d{2}$""".toRegex()
            if (!dateRegex.matches(fechaNacimiento)) {
                _uiState.value = PerfilClinicoUiState.Error("La fecha de nacimiento debe tener el formato YYYY-MM-DD (ej: 1990-05-24).")
                return
            }
            try {
                java.time.LocalDate.parse(fechaNacimiento)
            } catch (e: Exception) {
                _uiState.value = PerfilClinicoUiState.Error("La fecha de nacimiento no es una fecha válida.")
                return
            }
        }

        _uiState.value = PerfilClinicoUiState.Loading

        viewModelScope.launch {
            try {
                val datos = hashMapOf(
                    "grupoSanguineo" to grupoSanguineo.trim().ifBlank { null },
                    "alergias" to alergias.trim().ifBlank { "Ninguna" },
                    "fechaNacimiento" to fechaNacimiento.trim().ifBlank { null },
                    "direccion" to direccion.trim().ifBlank { null },
                    "estatura" to (estatura ?: 0.0),
                    "antecedentes" to antecedentes.trim().ifBlank { "Ninguna" }
                )

                withContext(Dispatchers.IO) {
                    db.collection("perfiles_pacientes")
                        .document(userId)
                        .set(datos, SetOptions.merge())
                        .await()
                }

                _uiState.value = PerfilClinicoUiState.SavedSuccess
            } catch (e: Exception) {
                _uiState.value = PerfilClinicoUiState.Error(
                    e.message ?: "Error al guardar los datos de tu ficha clínica."
                )
            }
        }
    }

    fun clearState() {
        _uiState.value = PerfilClinicoUiState.Idle
    }
}
