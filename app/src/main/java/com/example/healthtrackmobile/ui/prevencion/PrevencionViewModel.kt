package com.example.healthtrackmobile.ui.prevencion

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtrackmobile.model.Metrica
import com.example.healthtrackmobile.model.PerfilPaciente
import com.example.healthtrackmobile.model.Recomendacion
import com.example.healthtrackmobile.model.Usuario
import com.example.healthtrackmobile.service.AlertaSanitariaResponse
import com.example.healthtrackmobile.service.ClimaResponse
import com.example.healthtrackmobile.service.RecommendationEngine
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Immutable
data class PrevencionState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val clima: ClimaResponse? = null,
    val alertas: ImmutableList<AlertaSanitariaResponse> = persistentListOf(),
    val sugerencias: ImmutableList<Recomendacion> = persistentListOf(),
    val ciudadUsada: String = "",
    val isOfflineMode: Boolean = false
)

class PrevencionViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _state = MutableStateFlow(PrevencionState())
    val state: StateFlow<PrevencionState> = _state.asStateFlow()

    fun cargarDatosPrevencion(userId: String) {
        _state.value = PrevencionState(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                // Mover todas las llamadas a base de datos y red a Dispatchers.IO
                val results = withContext(Dispatchers.IO) {
                    // 1. Cargar datos del usuario de Firestore
                    val usuarioDoc = db.collection("usuarios")
                        .document(userId)
                        .get()
                        .await()
                    val usuario = usuarioDoc.toObject(Usuario::class.java)?.apply {
                        id = usuarioDoc.id
                    }

                    // 2. Cargar perfil clínico para obtener la dirección (ciudad)
                    val perfilDoc = db.collection("perfiles_pacientes")
                        .document(userId)
                        .get()
                        .await()
                    val perfil = perfilDoc.toObject(PerfilPaciente::class.java)?.apply {
                        id = perfilDoc.id
                    }

                    // 3. Cargar las métricas de salud del paciente
                    val metricasSnapshot = db.collection("metricas")
                        .whereEqualTo("paciente_id", userId)
                        .get()
                        .await()
                    val listMetricas = metricasSnapshot.toObjects(Metrica::class.java)
                    metricasSnapshot.documents.forEachIndexed { index, doc ->
                        if (index < listMetricas.size) {
                            listMetricas[index].id = doc.id
                        }
                    }

                    val direccion = perfil?.direccion

                    // 4. Invocar el motor de recomendaciones
                    var offlineMode = false
                    val engine = RecommendationEngine()
                    val (clima, alertas, sugerencias) = try {
                        val climaResult = engine.getClimaActual(direccion, null)
                        val alertasResult = engine.obtenerAlertasActivas(climaResult.ciudad.ifBlank { "Mexico" })
                        val sugerenciasResult = engine.generarSugerenciasIAPaciente(usuario, listMetricas, climaResult)
                        Triple(climaResult, alertasResult, sugerenciasResult)
                    } catch (e: Exception) {
                        offlineMode = true
                        val climaFallback = ClimaResponse(ciudad = direccion ?: "Local", disponible = false)
                        val alertasFallback = engine.obtenerAlertasActivas(direccion ?: "Mexico")
                        val sugerenciasFallback = engine.generarSugerenciasIAPaciente(usuario, listMetricas, null)
                        Triple(climaFallback, alertasFallback, sugerenciasFallback)
                    }
                    
                    Quadruple(clima, alertas, sugerencias, offlineMode, direccion)
                }

                val (clima, alertas, sugerencias, offlineMode, direccion) = results

                _state.value = PrevencionState(
                    isLoading = false,
                    error = null,
                    clima = clima,
                    alertas = alertas.toImmutableList(),
                    sugerencias = sugerencias.toImmutableList(),
                    ciudadUsada = clima.ciudad.ifBlank { direccion ?: "Celaya" },
                    isOfflineMode = offlineMode
                )
            } catch (e: Exception) {
                _state.value = PrevencionState(
                    isLoading = false,
                    error = e.message ?: "Error al procesar los datos de prevención IA"
                )
            }
        }
    }
}

data class Quadruple<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)
