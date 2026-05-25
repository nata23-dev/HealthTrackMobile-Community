package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Recomendacion(
    var id: String? = null,
    
    @get:PropertyName("medicoId")
    @set:PropertyName("medicoId")
    @PropertyName("medicoId")
    var medicoId: String? = null,
    
    @get:PropertyName("pacienteId")
    @set:PropertyName("pacienteId")
    @PropertyName("pacienteId")
    var pacienteId: String? = null,
    
    var mensaje: String? = null,
    var prioridad: String? = null, // "ALTA" | "MEDIA" | "BAJA"
    
    @get:PropertyName("fechaEnvio")
    @set:PropertyName("fechaEnvio")
    @PropertyName("fechaEnvio")
    var fechaEnvio: Long = 0L,
    
    var leida: Boolean = false,
    
    @get:Exclude
    @get:PropertyName("medico_nombre")
    @set:PropertyName("medico_nombre")
    @PropertyName("medico_nombre")
    var medicoNombre: String? = null
)
