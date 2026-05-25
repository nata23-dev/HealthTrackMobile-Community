package com.example.healthtrackmobile.ui.medicamentos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtrackmobile.model.RecordatorioMedicamento
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class MedicamentosState(
    val isLoading: Boolean = false,
    val recordatorios: List<RecordatorioMedicamento> = emptyList(),
    val error: String? = null
)

class MedicamentosViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _state = MutableStateFlow(MedicamentosState())
    val state: StateFlow<MedicamentosState> = _state.asStateFlow()

    fun cargarRecordatorios(userId: String) {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val list = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val snapshot = db.collection("recordatorios_medicamentos")
                        .whereEqualTo("paciente_id", userId)
                        .whereEqualTo("estado", "activo")
                        .get()
                        .await()
                    
                    val recordatorios = snapshot.toObjects(RecordatorioMedicamento::class.java)
                    snapshot.documents.forEachIndexed { index, doc ->
                        if (index < recordatorios.size) recordatorios[index].id = doc.id
                    }
                    recordatorios
                }
                _state.value = MedicamentosState(recordatorios = list)
            } catch (e: Exception) {
                _state.value = MedicamentosState(error = e.message)
            }
        }
    }

    fun agregarRecordatorio(context: android.content.Context, recordatorio: RecordatorioMedicamento) {
        viewModelScope.launch {
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    // Usar recordatorio.id (UUID) como ID del documento para evitar duplicidades de alarma
                    db.collection("recordatorios_medicamentos").document(recordatorio.id)
                        .set(recordatorio).await()
                    // Programar alarma local
                    com.example.healthtrackmobile.receiver.ReminderScheduler.scheduleReminder(context, recordatorio)
                }
                cargarRecordatorios(recordatorio.pacienteId ?: "")
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun eliminarRecordatorio(context: android.content.Context, reminderId: String, userId: String) {
        viewModelScope.launch {
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    // Cambiar el estado del recordatorio a inactivo en Firestore
                    db.collection("recordatorios_medicamentos").document(reminderId)
                        .update("estado", "inactivo").await()
                    // Cancelar la alarma exacta
                    com.example.healthtrackmobile.receiver.ReminderScheduler.cancelReminder(context, reminderId)
                }
                cargarRecordatorios(userId)
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}
