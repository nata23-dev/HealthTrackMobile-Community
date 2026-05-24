package com.example.healthtrackmobile.ui.prevencion

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class PrevencionState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val clima: ClimaResponse? = null,
    val alertas: List<AlertaSanitariaResponse> = emptyList(),
    val sugerencias: List<Recomendacion> = emptyList(),
    val ciudadUsada: String = ""
)

class PrevencionViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _state = MutableStateFlow(PrevencionState())
    val state: StateFlow<PrevencionState> = _state.asStateFlow()

    fun cargarDatosPrevencion(userId: String) {
        _state.value = PrevencionState(isLoading = true, error = null)
        viewModelScope.launch {
            try {
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
                    .whereEqualTo("pacienteId", userId)
                    .get()
                    .await()
                val listMetricas = metricasSnapshot.toObjects(Metrica::class.java)
                metricasSnapshot.documents.forEachIndexed { index, doc ->
                    if (index < listMetricas.size) {
                        listMetricas[index].id = doc.id
                    }
                }

                val direccion = perfil?.direccion

                // 4. Invocar el motor de recomendaciones en el hilo de E/S (Dispatchers.IO)
                val (clima, alertas, sugerencias) = withContext(Dispatchers.IO) {
                    val engine = RecommendationEngine()
                    val climaResult = engine.getClimaActual(direccion, null)
                    val alertasResult = engine.obtenerAlertasActivas(climaResult.ciudad.ifBlank { "Mexico" })
                    val sugerenciasResult = engine.generarSugerenciasIAPaciente(usuario, listMetricas, climaResult)
                    Triple(climaResult, alertasResult, sugerenciasResult)
                }

                _state.value = PrevencionState(
                    isLoading = false,
                    error = null,
                    clima = clima,
                    alertas = alertas,
                    sugerencias = sugerencias,
                    ciudadUsada = clima.ciudad.ifBlank { direccion ?: "Celaya" }
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
