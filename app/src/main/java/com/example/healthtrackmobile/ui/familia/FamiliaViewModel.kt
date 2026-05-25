package com.example.healthtrackmobile.ui.familia

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtrackmobile.model.Metrica
import com.example.healthtrackmobile.model.PerfilPaciente
import com.example.healthtrackmobile.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Immutable
data class FamiliaState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val resultadosBusqueda: List<Usuario> = emptyList(),
    val familiares: List<Usuario> = emptyList(),
    val familiarSeleccionado: Usuario? = null,
    val perfilSeleccionado: PerfilPaciente? = null,
    val ultimaGlucosa: Metrica? = null,
    val ultimaPresion: Metrica? = null,
    val ultimaFrecuencia: Metrica? = null,
    val ultimoPeso: Metrica? = null
)

class FamiliaViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _state = MutableStateFlow(FamiliaState())
    val state: StateFlow<FamiliaState> = _state.asStateFlow()

    fun cargarFamiliares(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // 1. Obtener vínculos familiares
                val vinculosSnapshot = db.collection("vinculos_familiares")
                    .whereEqualTo("pacienteId", userId)
                    .get()
                    .await()

                val listaFamiliares = mutableListOf<Usuario>()
                for (doc in vinculosSnapshot.documents) {
                    val familiarId = doc.getString("familiarId")
                    if (!familiarId.isNullOrBlank()) {
                        val userDoc = db.collection("usuarios").document(familiarId).get().await()
                        if (userDoc.exists()) {
                            val usuario = userDoc.toObject(Usuario::class.java)
                            if (usuario != null) {
                                usuario.id = userDoc.id
                                listaFamiliares.add(usuario)
                            }
                        }
                    }
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        familiares = listaFamiliares,
                        resultadosBusqueda = emptyList() // Limpiar búsqueda
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar familiares"
                    )
                }
            }
        }
    }

    fun buscarPacientes(userId: String, query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // 1. Obtener todos los pacientes activos
                val snapshot = db.collection("usuarios")
                    .whereEqualTo("rol", "paciente")
                    .get()
                    .await()

                // 2. Obtener IDs de familiares existentes para no listarlos
                val vinculosSnapshot = db.collection("vinculos_familiares")
                    .whereEqualTo("pacienteId", userId)
                    .get()
                    .await()

                val familiarIds = vinculosSnapshot.documents.mapNotNull { it.getString("familiarId") }

                val filter = query.trim().lowercase()
                val resultados = mutableListOf<Usuario>()
                
                for (doc in snapshot.documents) {
                    val u = doc.toObject(Usuario::class.java)
                    if (u != null) {
                        u.id = doc.id
                        val isSelf = u.id == userId
                        val isAlreadyFamiliar = familiarIds.contains(u.id)
                        val matchesName = u.nombre?.lowercase()?.contains(filter) == true
                        val isActivo = doc.getBoolean("activo") ?: true
                        
                        if (!isSelf && !isAlreadyFamiliar && matchesName && isActivo) {
                            resultados.add(u)
                        }
                    }
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        resultadosBusqueda = resultados
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al buscar pacientes"
                    )
                }
            }
        }
    }

    fun agregarFamiliar(userId: String, familiarId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val docId = "${userId}_${familiarId}"
                val vinculo = mapOf(
                    "pacienteId" to userId,
                    "familiarId" to familiarId,
                    "fechaVinculacion" to System.currentTimeMillis()
                )
                db.collection("vinculos_familiares").document(docId).set(vinculo).await()
                cargarFamiliares(userId)
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al agregar familiar"
                    )
                }
            }
        }
    }

    fun eliminarFamiliar(userId: String, familiarId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val docId = "${userId}_${familiarId}"
                db.collection("vinculos_familiares").document(docId).delete().await()
                
                if (_state.value.familiarSeleccionado?.id == familiarId) {
                    _state.update {
                        it.copy(
                            familiarSeleccionado = null,
                            perfilSeleccionado = null,
                            ultimaGlucosa = null,
                            ultimaPresion = null,
                            ultimaFrecuencia = null,
                            ultimoPeso = null
                        )
                    }
                }
                cargarFamiliares(userId)
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al eliminar familiar"
                    )
                }
            }
        }
    }

    fun seleccionarFamiliar(familiar: Usuario) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    familiarSeleccionado = familiar,
                    perfilSeleccionado = null,
                    ultimaGlucosa = null,
                    ultimaPresion = null,
                    ultimaFrecuencia = null,
                    ultimoPeso = null
                )
            }
            try {
                val familiarId = familiar.id ?: return@launch
                
                // 1. Cargar Perfil
                val perfilDoc = db.collection("perfiles_pacientes").document(familiarId).get().await()
                val perfil = perfilDoc.toObject(PerfilPaciente::class.java)

                // 2. Cargar Métricas
                val metricasSnapshot = db.collection("metricas")
                    .whereEqualTo("pacienteId", familiarId)
                    .get()
                    .await()

                val metricas = metricasSnapshot.toObjects(Metrica::class.java)
                val sortedMetricas = metricas.sortedByDescending { it.timestamp }

                val ultimaGlucosa = sortedMetricas.firstOrNull { m -> 
                    val t = m.tipo?.uppercase()?.trim()
                    t == "GLUCOSA" || t == "GLUCOSE"
                }
                val ultimaPresion = sortedMetricas.firstOrNull { m -> 
                    val t = m.tipo?.uppercase()?.trim()
                    t == "PRESION" || t == "PRESSURE" || t == "TENSIÓN" || t == "PRESION_ARTERIAL"
                }
                val ultimaFrecuencia = sortedMetricas.firstOrNull { m -> 
                    val t = m.tipo?.uppercase()?.trim()
                    t == "FRECUENCIA" || t == "HEART_RATE" || t == "RITMO" || t == "FRECUENCIA_CARDIACA"
                }
                val ultimoPeso = sortedMetricas.firstOrNull { m -> 
                    val t = m.tipo?.uppercase()?.trim()
                    t == "PESO" || t == "WEIGHT"
                }

                _state.update {
                    it.copy(
                        perfilSeleccionado = perfil,
                        ultimaGlucosa = ultimaGlucosa,
                        ultimaPresion = ultimaPresion,
                        ultimaFrecuencia = ultimaFrecuencia,
                        ultimoPeso = ultimoPeso
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message ?: "Error al cargar resumen médico del familiar"
                    )
                }
            }
        }
    }

    fun deseleccionarFamiliar() {
        _state.update {
            it.copy(
                familiarSeleccionado = null,
                perfilSeleccionado = null,
                ultimaGlucosa = null,
                ultimaPresion = null,
                ultimaFrecuencia = null,
                ultimoPeso = null
            )
        }
    }
}
