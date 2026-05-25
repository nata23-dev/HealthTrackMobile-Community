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
import kotlinx.coroutines.withTimeoutOrNull

class AgregarMetricaViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun guardarMetricas(
        userId: String,
        metricas: List<Metrica>,
        onSuccess: () -> Unit
    ) {
        _isSaving.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    metricas.forEach { metrica ->
                        metrica.pacienteId = userId
                        metrica.timestamp = System.currentTimeMillis()
                        
                        val task = db.collection("metricas").add(metrica)
                        // Intentamos esperar al servidor por un breve momento (UX óptima)
                        try {
                            withTimeoutOrNull(2000) {
                                task.await()
                            }
                        } catch (e: Exception) {
                            // Si falla (ej. offline), Firestore maneja la re-sincronización automáticamente
                        }
                    }
                }
                _isSaving.value = false
                onSuccess()
            } catch (e: Exception) {
                _isSaving.value = false
                _error.value = e.message ?: "Error al guardar los registros"
            }
        }
    }
}
