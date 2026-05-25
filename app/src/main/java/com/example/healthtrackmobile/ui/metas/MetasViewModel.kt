package com.example.healthtrackmobile.ui.metas

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtrackmobile.model.Meta
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
data class MetasState(
    val isLoading: Boolean = false,
    val metas: ImmutableList<Meta> = persistentListOf(),
    val error: String? = null
)

class MetasViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _state = MutableStateFlow(MetasState())
    val state: StateFlow<MetasState> = _state.asStateFlow()

    fun cargarMetas(userId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    val snapshot = db.collection("metas")
                        .whereEqualTo("pacienteId", userId)
                        .get()
                        .await()
                    
                    val metas = snapshot.toObjects(Meta::class.java)
                    snapshot.documents.forEachIndexed { index, doc ->
                        if (index < metas.size) metas[index].id = doc.id
                    }
                    metas.sortedByDescending { it.fechaCumplimiento }
                }
                _state.value = MetasState(metas = list.toImmutableList())
            } catch (e: Exception) {
                _state.value = MetasState(error = e.message ?: "Error al cargar metas")
            }
        }
    }

    fun crearMeta(meta: Meta, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val ref = db.collection("metas").document()
                    meta.id = ref.id
                    ref.set(meta).await()
                }
                cargarMetas(meta.pacienteId ?: "")
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun actualizarEstadoMeta(metaId: String, nuevoEstado: String, pacienteId: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    db.collection("metas").document(metaId)
                        .update("estado", nuevoEstado)
                        .await()
                }
                cargarMetas(pacienteId)
            } catch (e: Exception) {
                // Silenciosamente ignorar o notificar error
            }
        }
    }

    fun eliminarMeta(metaId: String, pacienteId: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    db.collection("metas").document(metaId)
                        .delete()
                        .await()
                }
                cargarMetas(pacienteId)
            } catch (e: Exception) {
                // Silenciosamente ignorar o notificar error
            }
        }
    }
}
