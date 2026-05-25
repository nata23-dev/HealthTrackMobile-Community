package com.example.healthtrackmobile.ui.reportes

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtrackmobile.model.Metrica
import com.example.healthtrackmobile.model.PerfilPaciente
import com.example.healthtrackmobile.model.Recomendacion
import com.example.healthtrackmobile.model.Usuario
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
data class ReportesState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val nombrePaciente: String = "",
    val metricasRecientes: ImmutableList<Metrica> = persistentListOf(),
    val recomendaciones: ImmutableList<Recomendacion> = persistentListOf(),
    val alertas: ImmutableList<String> = persistentListOf(),
    val folioHT: String = "",
    val resumenMensaje: String = ""
)

private data class ReporteCargaResult(
    val nombre: String,
    val metricas: List<Metrica>,
    val recomendaciones: List<Recomendacion>,
    val alertas: List<String>,
    val folio: String,
    val mensaje: String
)

class ReportesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _state = MutableStateFlow(ReportesState())
    val state: StateFlow<ReportesState> = _state.asStateFlow()

    fun cargarDatosReporte(userId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    val usuarioDoc = db.collection("usuarios").document(userId).get().await()
                    val usuario = usuarioDoc.toObject(Usuario::class.java)
                    
                    val perfilDoc = db.collection("perfiles_pacientes").document(userId).get().await()
                    val perfil = perfilDoc.toObject(PerfilPaciente::class.java)

                    val metricasSnapshot = db.collection("metricas")
                        .whereEqualTo("pacienteId", userId)
                        .get()
                        .await()
                    
                    val metricas = metricasSnapshot.toObjects(Metrica::class.java)
                        .sortedByDescending { it.timestamp }
                    
                    // Cargar recomendaciones médicas de Firestore
                    val recSnapshot = db.collection("recomendaciones")
                        .whereEqualTo("pacienteId", userId)
                        .get()
                        .await()
                    val doctorRecs = recSnapshot.toObjects(Recomendacion::class.java)
                    
                    // Enriquecer con nombres de médicos
                    val medicosCache = mutableMapOf<String, String>()
                    for (rec in doctorRecs) {
                        val medId = rec.medicoId
                        if (!medId.isNullOrBlank()) {
                            val nombre = medicosCache.getOrPut(medId) {
                                try {
                                    val userDoc = db.collection("usuarios").document(medId).get().await()
                                    userDoc.getString("nombre") ?: "Médico Especialista"
                                } catch (e: Exception) {
                                    "Médico Especialista"
                                }
                            }
                            rec.medicoNombre = nombre
                        }
                    }
                    val sortedDoctorRecs = doctorRecs.sortedByDescending { it.fechaEnvio }

                    // Calcular sugerencias y alertas usando el RecommendationEngine
                    val engine = RecommendationEngine()
                    val climaResult = try {
                        engine.getClimaActual(perfil?.direccion, null)
                    } catch (e: Exception) {
                        null
                    }
                    val activeAlerts = try {
                        engine.obtenerAlertasActivas(climaResult?.ciudad?.ifBlank { "Mexico" } ?: "Mexico")
                    } catch (e: Exception) {
                        emptyList()
                    }
                    val iaSuggestions = try {
                        engine.generarSugerenciasIAPaciente(usuario, metricas, climaResult)
                    } catch (e: Exception) {
                        emptyList()
                    }

                    // Consolidar recomendaciones y alertas
                    val combinedRecs = (doctorRecs + iaSuggestions).sortedByDescending { it.fechaEnvio }
                    val alertStrings = activeAlerts.map { it.descripcion }

                    val mensaje = construirMensajeWhatsApp(usuario?.nombre ?: "Paciente", metricas)
                    val folio = userId.take(8)

                    ReporteCargaResult(
                        nombre = usuario?.nombre ?: "Paciente",
                        metricas = metricas,
                        recomendaciones = combinedRecs,
                        alertas = alertStrings,
                        folio = folio,
                        mensaje = mensaje
                    )
                }

                _state.value = ReportesState(
                    isLoading = false,
                    nombrePaciente = data.nombre,
                    metricasRecientes = data.metricas.take(10).toImmutableList(),
                    recomendaciones = data.recomendaciones.toImmutableList(),
                    alertas = data.alertas.toImmutableList(),
                    folioHT = data.folio,
                    resumenMensaje = data.mensaje
                )
            } catch (e: Exception) {
                _state.value = ReportesState(isLoading = false, error = e.message ?: "Error al generar reporte")
            }
        }
    }

    private fun construirMensajeWhatsApp(nombre: String, metricas: List<Metrica>): String {
        val sb = StringBuilder()
        sb.append("GOBIERNO DE MÉXICO - Reporte Clínico Digital\n")
        sb.append("Paciente: $nombre\n")
        sb.append("Emitido desde: Celaya, Gto.\n")
        sb.append("--------------------------------\n")
        
        if (metricas.isEmpty()) {
            sb.append("Sin registros recientes.")
        } else {
            metricas.take(5).forEach { m ->
                val tipo = m.tipo?.uppercase() ?: "MÉTRICA"
                val valor = if (tipo == "PRESION") "${m.valor.toInt()}/${m.valorSecundario.toInt()} mmHg" 
                            else "${m.valor} ${obtenerUnidad(tipo)}"
                sb.append("• $tipo: $valor\n")
            }
        }
        sb.append("--------------------------------\n")
        sb.append("Enviado vía HealthTrack Mobile.")
        return sb.toString()
    }

    private fun obtenerUnidad(tipo: String): String = when (tipo) {
        "GLUCOSA" -> "mg/dL"
        "FRECUENCIA", "FRECUENCIA_CARDIACA", "RITMO" -> "lpm"
        "PESO" -> "kg"
        else -> ""
    }
}
