package com.example.healthtrackmobile.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtrackmobile.model.HistorialLogro
import com.example.healthtrackmobile.model.Metrica
import com.example.healthtrackmobile.model.Recomendacion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class DashboardState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val ultimaGlucosa: Metrica? = null,
    val ultimaPresion: Metrica? = null,
    val ultimaFrecuencia: Metrica? = null,
    val ultimoPeso: Metrica? = null,
    val recomendaciones: List<Recomendacion> = emptyList(),
    val logros: List<HistorialLogro> = emptyList()
)

class DashboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    fun cargarDatosDashboard(userId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                // 1. Cargar las últimas métricas
                val metricasSnapshot = db.collection("metricas")
                    .whereEqualTo("pacienteId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(30)
                    .get()
                    .await()

                val listMetricas = metricasSnapshot.toObjects(Metrica::class.java)
                
                // Mapear los IDs de los documentos a cada objeto Metrica
                metricasSnapshot.documents.forEachIndexed { index, doc ->
                    if (index < listMetricas.size) {
                        listMetricas[index].id = doc.id
                    }
                }

                val ultimaGlucosa = listMetricas.firstOrNull { it.tipo?.uppercase() == "GLUCOSA" }
                val ultimaPresion = listMetricas.firstOrNull { it.tipo?.uppercase() == "PRESION" }
                val ultimaFrecuencia = listMetricas.firstOrNull { it.tipo?.uppercase() == "FRECUENCIA_CARDIACA" }
                val ultimoPeso = listMetricas.firstOrNull { it.tipo?.uppercase() == "PESO" }

                // 2. Cargar recomendaciones del médico
                val recomendacionesSnapshot = db.collection("recomendaciones")
                    .whereEqualTo("pacienteId", userId)
                    .orderBy("fechaEnvio", Query.Direction.DESCENDING)
                    .get()
                    .await()
                val recomendaciones = recomendacionesSnapshot.toObjects(Recomendacion::class.java)
                
                recomendacionesSnapshot.documents.forEachIndexed { index, doc ->
                    if (index < recomendaciones.size) {
                        recomendaciones[index].id = doc.id
                    }
                }

                // 3. Cargar logros
                val logrosSnapshot = db.collection("historial_logros")
                    .whereEqualTo("pacienteId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
                val logros = logrosSnapshot.toObjects(HistorialLogro::class.java)
                
                logrosSnapshot.documents.forEachIndexed { index, doc ->
                    if (index < logros.size) {
                        logros[index].id = doc.id
                    }
                }

                _state.value = DashboardState(
                    isLoading = false,
                    error = null,
                    ultimaGlucosa = ultimaGlucosa,
                    ultimaPresion = ultimaPresion,
                    ultimaFrecuencia = ultimaFrecuencia,
                    ultimoPeso = ultimoPeso,
                    recomendaciones = recomendaciones,
                    logros = logros
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar los datos del dashboard"
                )
            }
        }
    }
}
