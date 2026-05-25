package com.example.healthtrackmobile.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtrackmobile.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Immutable
data class DashboardState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val sugerenciaIA: Recomendacion? = null,
    val metricasCriticas: ImmutableList<Metrica> = persistentListOf(),
    val recomendaciones: ImmutableList<Recomendacion> = persistentListOf(),
    val logros: ImmutableList<HistorialLogro> = persistentListOf(),
    val ultimaGlucosa: Metrica? = null,
    val ultimaPresion: Metrica? = null,
    val ultimaFrecuencia: Metrica? = null,
    val ultimoPeso: Metrica? = null
)

class DashboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    fun cargarDatosDashboard(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val data = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    // 1. Cargar todas las métricas del paciente para filtrar en memoria
                    // Eliminamos orderBy para evitar errores de índice faltante en Firestore
                    val metricasSnapshot = db.collection("metricas")
                        .whereEqualTo("pacienteId", userId)
                        .get()
                        .await()

                    val listMetricas = metricasSnapshot.toObjects(Metrica::class.java)
                    
                    // Aseguramos que los IDs se asignen correctamente
                    metricasSnapshot.documents.forEachIndexed { index, doc ->
                        if (index < listMetricas.size) {
                            listMetricas[index].id = doc.id
                        }
                    }

                    // Ordenamos por timestamp descendente para obtener los más recientes primero
                    val sortedMetricas = listMetricas.sortedByDescending { it.timestamp }

                    // 2. Cargar recomendaciones
                    val recSnapshot = db.collection("recomendaciones")
                        .whereEqualTo("pacienteId", userId)
                        .get()
                        .await()
                    val listRec = recSnapshot.toObjects(Recomendacion::class.java)
                        .sortedByDescending { it.fechaEnvio }

                    // 3. Cargar logros
                    val logrosSnapshot = db.collection("historial_logros")
                        .whereEqualTo("pacienteId", userId)
                        .get()
                        .await()
                    val listLogros = logrosSnapshot.toObjects(HistorialLogro::class.java)
                        .sortedByDescending { it.timestamp }

                    // 4. Módulo de Prevención IA
                    val perfilDoc = db.collection("perfiles_pacientes").document(userId).get().await()
                    val perfil = perfilDoc.toObject(PerfilPaciente::class.java)
                    
                    val engine = com.example.healthtrackmobile.service.RecommendationEngine()
                    val clima = engine.getClimaActual(perfil?.direccion, null)
                    val usuarioDoc = db.collection("usuarios").document(userId).get().await()
                    val usuario = usuarioDoc.toObject(Usuario::class.java)
                    
                    val sugerenciasIA = engine.generarSugerenciasIAPaciente(usuario, sortedMetricas, clima)
                    
                    Triple(sortedMetricas, listRec, listLogros) to sugerenciasIA.firstOrNull()
                }

                val (lists, sugerenciaIA) = data
                val (sortedMetricas, listRec, listLogros) = lists

                _state.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        sugerenciaIA = sugerenciaIA,
                        metricasCriticas = sortedMetricas.toImmutableList(),
                        recomendaciones = listRec.toImmutableList(),
                        logros = listLogros.toImmutableList(),
                        ultimaGlucosa = sortedMetricas.firstOrNull { m -> 
                            val t = m.tipo?.uppercase()?.trim()
                            t == "GLUCOSA" || t == "GLUCOSE"
                        },
                        ultimaPresion = sortedMetricas.firstOrNull { m -> 
                            val t = m.tipo?.uppercase()?.trim()
                            t == "PRESION" || t == "PRESSURE" || t == "TENSIÓN" || t == "PRESION_ARTERIAL"
                        },
                        ultimaFrecuencia = sortedMetricas.firstOrNull { m -> 
                            val t = m.tipo?.uppercase()?.trim()
                            t == "FRECUENCIA" || t == "HEART_RATE" || t == "RITMO" || t == "FRECUENCIA_CARDIACA"
                        },
                        ultimoPeso = sortedMetricas.firstOrNull { m -> 
                            val t = m.tipo?.uppercase()?.trim()
                            t == "PESO" || t == "WEIGHT"
                        }
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar datos"
                    )
                }
            }
        }
    }
}
