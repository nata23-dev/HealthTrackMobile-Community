package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.Exclude

@IgnoreExtraProperties
data class Recomendacion(
    var id: String? = null,
    var medicoId: String? = null,
    var pacienteId: String? = null,
    var mensaje: String? = null,
    var prioridad: String? = null, // "ALTA" | "MEDIA" | "BAJA"
    var fechaEnvio: Long = 0L,
    
    @get:Exclude
    var leida: Boolean = false,
    
    @get:Exclude
    var medicoNombre: String? = null
)
