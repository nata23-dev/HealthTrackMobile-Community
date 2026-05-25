package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class HistorialLogro(
    var id: String? = null,
    
    @get:PropertyName("paciente_id")
    @set:PropertyName("paciente_id")
    @PropertyName("paciente_id")
    var pacienteId: String? = null,
    
    var titulo: String? = null,
    var descripcion: String? = null,
    var timestamp: Long = 0L
)
