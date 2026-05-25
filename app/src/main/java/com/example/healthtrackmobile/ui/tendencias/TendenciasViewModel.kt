package com.example.healthtrackmobile.ui.tendencias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtrackmobile.model.Metrica
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class TendenciasState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val glucosaMetricas: ImmutableList<Metrica> = persistentListOf(),
    val presionMetricas: ImmutableList<Metrica> = persistentListOf()
)

class TendenciasViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _state = MutableStateFlow(TendenciasState())
    val state: StateFlow<TendenciasState> = _state.asStateFlow()

    fun cargarMetricas(userId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val data = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val snapshot = db.collection("metricas")
                        .whereEqualTo("paciente_id", userId)
                        .get()
                        .await()
                    
                    val list = snapshot.toObjects(Metrica::class.java)
                    
                    val glucosa = list.filter { m ->
                        val t = m.tipo?.uppercase()?.trim()
                        t == "GLUCOSA" || t == "GLUCOSE"
                    }.sortedBy { it.timestamp }.takeLast(10)

                    val presion = list.filter { m ->
                        val t = m.tipo?.uppercase()?.trim()
                        t == "PRESION" || t == "PRESSURE" || t == "TENSIÓN" || t == "PRESION_ARTERIAL"
                    }.sortedBy { it.timestamp }.takeLast(10)

                    glucosa to presion
                }
                _state.value = TendenciasState(
                    isLoading = false,
                    glucosaMetricas = data.first.toImmutableList(),
                    presionMetricas = data.second.toImmutableList()
                )
            } catch (e: Exception) {
                _state.value = TendenciasState(
                    isLoading = false,
                    error = e.message ?: "Error al cargar tendencias de salud"
                )
            }
        }
    }
}
