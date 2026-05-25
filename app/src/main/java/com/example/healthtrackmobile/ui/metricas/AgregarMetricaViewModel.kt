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

    private val _metricas = MutableStateFlow<List<Metrica>>(emptyList())
    val metricas: StateFlow<List<Metrica>> = _metricas.asStateFlow()

    private val _isLoadingHistorial = MutableStateFlow(false)
    val isLoadingHistorial: StateFlow<Boolean> = _isLoadingHistorial.asStateFlow()

    fun cargarMetricas(userId: String) {
        _isLoadingHistorial.value = true
        viewModelScope.launch {
            try {
                val snapshot = db.collection("metricas")
                    .whereEqualTo("pacienteId", userId)
                    .get()
                    .await()
                val list = snapshot.toObjects(Metrica::class.java)
                snapshot.documents.forEachIndexed { index, doc ->
                    if (index < list.size) {
                        list[index].id = doc.id
                    }
                }
                _metricas.value = list.sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar historial de métricas"
            } finally {
                _isLoadingHistorial.value = false
            }
        }
    }

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

                        // Sincronizar dinámicamente con metas activas del paciente
                        actualizarMetaActiva(
                            userId = userId,
                            tipoMetrica = metrica.tipo ?: "",
                            nuevoValor = metrica.valor,
                            nuevoValorSecundario = metrica.valorSecundario
                        )
                    }
                }
                _isSaving.value = false
                cargarMetricas(userId)
                onSuccess()
            } catch (e: Exception) {
                _isSaving.value = false
                _error.value = e.message ?: "Error al guardar los registros"
            }
        }
    }

    private suspend fun actualizarMetaActiva(
        userId: String,
        tipoMetrica: String,
        nuevoValor: Double,
        nuevoValorSecundario: Double
    ) {
        try {
            // Se asume ejecución en Dispatchers.IO
            val querySnapshot = db.collection("metas")
                .whereEqualTo("pacienteId", userId)
                .whereEqualTo("tipoMetrica", tipoMetrica)
                .get()
                .await()

            for (doc in querySnapshot.documents) {
                val estado = doc.getString("estado") ?: ""
                if (estado.uppercase().trim() == "ACTIVA") {
                    val updates = mutableMapOf<String, Any>()
                    updates["valorActual"] = nuevoValor
                    if (tipoMetrica == "PRESION") {
                        updates["valorActualSecundario"] = nuevoValorSecundario
                    }
                    db.collection("metas").document(doc.id).update(updates).await()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AgregarMetricaViewModel", "Error al sincronizar con meta activa: ${e.message}", e)
        }
    }
}
