package com.example.healthtrackmobile.ui.directorio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtrackmobile.model.ClinicaHospital
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface DirectorioUiState {
    data object Loading : DirectorioUiState
    data class Success(val clinicas: List<ClinicaHospital>) : DirectorioUiState
    data class Error(val message: String) : DirectorioUiState
}

class DirectorioMedicoViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow<DirectorioUiState>(DirectorioUiState.Loading)
    val uiState: StateFlow<DirectorioUiState> = _uiState.asStateFlow()

    fun cargarDirectorio() {
        _uiState.value = DirectorioUiState.Loading
        viewModelScope.launch {
            try {
                val snapshot = db.collection("clinicas_hospitales")
                    .get()
                    .await()

                val clinicas = snapshot.toObjects(ClinicaHospital::class.java)

                if (clinicas.isEmpty()) {
                    // Fallback a los datos locales si no hay nada en la base de datos
                    _uiState.value = DirectorioUiState.Success(obtenerDirectorioLocal())
                } else {
                    // Filtrar solo las clínicas activas
                    val activas = clinicas.filter { it.estado == "activo" }
                    _uiState.value = DirectorioUiState.Success(activas)
                }
            } catch (e: Exception) {
                // Si falla la red, también intentamos mostrar los datos locales como plan de contingencia
                _uiState.value = DirectorioUiState.Success(obtenerDirectorioLocal())
            }
        }
    }

    private fun obtenerDirectorioLocal(): List<ClinicaHospital> {
        return listOf(
            ClinicaHospital("Hospital General de Celaya", "Av. México-Japón #102, Cd. Industrial", "Celaya", "(461) 611 6000", "Segundo Nivel / Urgencias"),
            ClinicaHospital("Hospital MAC Celaya", "Eje Norponiente #200, Col. Villas del Bajío", "Celaya", "(461) 241 1000", "Alta Especialidad"),
            ClinicaHospital("Sanatorio Celaya", "Mutualismo #308, Centro", "Celaya", "(461) 612 0463", "Atención General"),
            ClinicaHospital("IMSS HGZ 4", "Av. Mutualismo #100, Centro", "Celaya", "(461) 612 0003", "Seguridad Social / General"),
            ClinicaHospital("Hospital San José", "Av. Constituyentes #110, El Mirador", "Celaya", "(461) 612 0897", "Atención Especializada")
        )
    }
}
