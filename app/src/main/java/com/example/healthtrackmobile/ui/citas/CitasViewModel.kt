package com.example.healthtrackmobile.ui.citas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtrackmobile.model.Cita
import com.example.healthtrackmobile.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface CitasUiState {
    data object Loading : CitasUiState
    data class Success(
        val citas: List<Cita>,
        val medicos: List<Usuario>
    ) : CitasUiState
    data class Error(val message: String) : CitasUiState
}

sealed interface AgendarState {
    data object Idle : AgendarState
    data object Loading : AgendarState
    data object Success : AgendarState
    data class Error(val message: String) : AgendarState
}

class CitasViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow<CitasUiState>(CitasUiState.Loading)
    val uiState: StateFlow<CitasUiState> = _uiState.asStateFlow()

    private val _agendarState = MutableStateFlow<AgendarState>(AgendarState.Idle)
    val agendarState: StateFlow<AgendarState> = _agendarState.asStateFlow()

    fun cargarDatos(userId: String) {
        _uiState.value = CitasUiState.Loading
        viewModelScope.launch {
            try {
                // 1. Cargar citas del paciente
                val citasSnap = db.collection("citas_medicas")
                    .whereEqualTo("pacienteId", userId)
                    .get()
                    .await()
                val citas = citasSnap.documents.mapNotNull { doc ->
                    doc.toObject(Cita::class.java)?.apply { id = doc.id }
                }.sortedBy { it.fechaHora }

                // 2. Cargar IDs de médicos vinculados
                val vinculosSnap = db.collection("medico_pacientes")
                    .whereEqualTo("pacienteId", userId)
                    .get()
                    .await()
                val medicoIds = vinculosSnap.documents.mapNotNull { it.getString("medicoId") }

                // 3. Obtener detalles de cada médico asignado
                val medicos = mutableListOf<Usuario>()
                for (medicoId in medicoIds) {
                    val userDoc = db.collection("usuarios").document(medicoId).get().await()
                    if (userDoc.exists()) {
                        userDoc.toObject(Usuario::class.java)?.let { user ->
                            user.id = userDoc.id
                            medicos.add(user)
                        }
                    }
                }

                _uiState.value = CitasUiState.Success(citas = citas, medicos = medicos)
            } catch (e: Exception) {
                _uiState.value = CitasUiState.Error(
                    e.message ?: "Error al cargar la información de citas médicas."
                )
            }
        }
    }

    fun agendarCita(
        pacienteId: String,
        pacienteNombre: String,
        medico: Usuario,
        fechaHoraEpoch: Long
    ) {
        if (fechaHoraEpoch < System.currentTimeMillis()) {
            _agendarState.value = AgendarState.Error("No puedes programar una cita en el pasado.")
            return
        }

        _agendarState.value = AgendarState.Loading
        viewModelScope.launch {
            try {
                val docRef = db.collection("citas_medicas").document()
                val nuevaCita = Cita(
                    id = docRef.id,
                    pacienteId = pacienteId,
                    pacienteNombre = pacienteNombre,
                    medicoId = medico.id,
                    medicoNombre = medico.nombre,
                    fechaHora = fechaHoraEpoch,
                    estado = "PENDIENTE"
                )

                docRef.set(nuevaCita).await()
                _agendarState.value = AgendarState.Success
                // Recargar datos tras agendar
                cargarDatos(pacienteId)
            } catch (e: Exception) {
                _agendarState.value = AgendarState.Error(
                    e.message ?: "Error al agendar la cita médica. Inténtalo de nuevo."
                )
            }
        }
    }

    fun cancelarCita(pacienteId: String, citaId: String) {
        viewModelScope.launch {
            try {
                db.collection("citas_medicas").document(citaId).delete().await()
                // Recargar datos tras cancelar
                cargarDatos(pacienteId)
            } catch (e: Exception) {
                // Notificar error de cancelación si es necesario, o recargar para limpiar estado
                cargarDatos(pacienteId)
            }
        }
    }

    fun resetAgendarState() {
        _agendarState.value = AgendarState.Idle
    }
}
