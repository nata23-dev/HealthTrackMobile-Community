package com.example.healthtrackmobile.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtrackmobile.model.*
import com.google.firebase.firestore.FirebaseFirestore
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
import com.example.healthtrackmobile.service.ClimaResponse
import com.example.healthtrackmobile.service.AlertaSanitariaResponse

@Immutable
data class DashboardState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val sugerenciaIA: Recomendacion? = null,
    val metricasCriticas: ImmutableList<Metrica> = persistentListOf(),
    val recomendaciones: ImmutableList<Recomendacion> = persistentListOf(),
    val logros: ImmutableList<HistorialLogro> = persistentListOf(),
    val notificaciones: ImmutableList<Notificacion> = persistentListOf(),
    val notificacionesNoLeidas: Int = 0,
    val ultimaGlucosa: Metrica? = null,
    val ultimaPresion: Metrica? = null,
    val ultimaFrecuencia: Metrica? = null,
    val ultimoPeso: Metrica? = null,
    val perfil: PerfilPaciente? = null,
    val climaActual: ClimaResponse? = null,
    val alertasActivas: ImmutableList<AlertaSanitariaResponse> = persistentListOf(),
    val metaActiva: Meta? = null
)

private data class DashboardCargaResult(
    val metricas: List<Metrica>,
    val recomendaciones: List<Recomendacion>,
    val logros: List<HistorialLogro>,
    val notificaciones: List<Notificacion>,
    val sugerenciaIA: Recomendacion?,
    val perfil: PerfilPaciente?,
    val climaActual: ClimaResponse?,
    val alertasActivas: List<AlertaSanitariaResponse>,
    val metaActiva: Meta?
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

                    val sortedMetricas = listMetricas.sortedByDescending { it.timestamp }

                    // 2. Cargar recomendaciones
                    val recSnapshot = db.collection("recomendaciones")
                        .whereEqualTo("pacienteId", userId)
                        .get()
                        .await()
                    val listRec = recSnapshot.toObjects(Recomendacion::class.java)

                    // Enriquecer con nombres de médicos
                    val medicosCache = mutableMapOf<String, String>()
                    for (rec in listRec) {
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
                    val sortedRec = listRec.sortedByDescending { it.fechaEnvio }

                    // 3. Cargar logros
                    val logrosSnapshot = db.collection("historial_logros")
                        .whereEqualTo("pacienteId", userId)
                        .get()
                        .await()
                    val listLogros = logrosSnapshot.toObjects(HistorialLogro::class.java)
                        .sortedByDescending { it.timestamp }

                    // 4. Cargar notificaciones
                    val notifSnapshot = db.collection("notificaciones")
                        .whereEqualTo("usuarioId", userId)
                        .get()
                        .await()
                    val listNotif = notifSnapshot.toObjects(Notificacion::class.java)
                        .sortedByDescending { it.fechaCreacion }

                    // 5. Cargar perfil clínico
                    val perfilDoc = db.collection("perfiles_pacientes").document(userId).get().await()
                    val perfil = perfilDoc.toObject(PerfilPaciente::class.java)
                    
                    // 6. Cargar clima y alertas ambientales
                    val engine = com.example.healthtrackmobile.service.RecommendationEngine()
                    val clima = engine.getClimaActual(perfil?.direccion, null)
                    val alertas = engine.obtenerAlertasActivas(perfil?.direccion)
                    
                    // 7. Cargar metas y seleccionar la meta activa principal
                    val metasSnapshot = db.collection("metas")
                        .whereEqualTo("pacienteId", userId)
                        .get()
                        .await()
                    val listMetas = metasSnapshot.toObjects(Meta::class.java)
                    metasSnapshot.documents.forEachIndexed { index, doc ->
                        if (index < listMetas.size) {
                            listMetas[index].id = doc.id
                        }
                    }
                    val metaActiva = listMetas.filter { m ->
                        m.estado.uppercase().trim() == "ACTIVA"
                    }.minByOrNull { it.prioridad }

                    // 8. Sugerencia Preventiva IA
                    val usuarioDoc = db.collection("usuarios").document(userId).get().await()
                    val usuario = usuarioDoc.toObject(Usuario::class.java)
                    val sugerenciasIA = engine.generarSugerenciasIAPaciente(usuario, sortedMetricas, clima)
                    
                    DashboardCargaResult(
                        metricas = sortedMetricas,
                        recomendaciones = sortedRec,
                        logros = listLogros,
                        notificaciones = listNotif,
                        sugerenciaIA = sugerenciasIA.firstOrNull(),
                        perfil = perfil,
                        climaActual = clima,
                        alertasActivas = alertas,
                        metaActiva = metaActiva
                    )
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        sugerenciaIA = data.sugerenciaIA,
                        metricasCriticas = data.metricas.toImmutableList(),
                        recomendaciones = data.recomendaciones.toImmutableList(),
                        logros = data.logros.toImmutableList(),
                        notificaciones = data.notificaciones.toImmutableList(),
                        notificacionesNoLeidas = data.notificaciones.count { n -> !n.leida },
                        ultimaGlucosa = data.metricas.firstOrNull { m -> 
                            val t = m.tipo?.uppercase()?.trim()
                            t == "GLUCOSA" || t == "GLUCOSE"
                        },
                        ultimaPresion = data.metricas.firstOrNull { m -> 
                            val t = m.tipo?.uppercase()?.trim()
                            t == "PRESION" || t == "PRESSURE" || t == "TENSIÓN" || t == "PRESION_ARTERIAL"
                        },
                        ultimaFrecuencia = data.metricas.firstOrNull { m -> 
                            val t = m.tipo?.uppercase()?.trim()
                            t == "FRECUENCIA" || t == "HEART_RATE" || t == "RITMO" || t == "FRECUENCIA_CARDIACA"
                        },
                        ultimoPeso = data.metricas.firstOrNull { m -> 
                            val t = m.tipo?.uppercase()?.trim()
                            t == "PESO" || t == "WEIGHT"
                        },
                        perfil = data.perfil,
                        climaActual = data.climaActual,
                        alertasActivas = data.alertasActivas.toImmutableList(),
                        metaActiva = data.metaActiva
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

    fun marcarNotificacionesComoLeidas(userId: String) {
        viewModelScope.launch {
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val snapshot = db.collection("notificaciones")
                        .whereEqualTo("usuarioId", userId)
                        .whereEqualTo("leida", false)
                        .get()
                        .await()
                    
                    if (!snapshot.isEmpty) {
                        val batch = db.batch()
                        snapshot.documents.forEach { doc ->
                            batch.update(doc.reference, "leida", true)
                        }
                        batch.commit().await()
                    }
                }
                _state.update { state ->
                    state.copy(
                        notificaciones = state.notificaciones.map { it.copy(leida = true) }.toImmutableList(),
                        notificacionesNoLeidas = 0
                    )
                }
            } catch (e: Exception) {
                // Silenciosamente ignorar error de actualización de UI
            }
        }
    }
}
