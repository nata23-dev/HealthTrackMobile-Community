package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Recomendacion(
    var id: String? = null,
    
    @get:PropertyName("medico_id")
    @set:PropertyName("medico_id")
    @PropertyName("medico_id")
    var medicoId: String? = null,
    
    @get:PropertyName("paciente_id")
    @set:PropertyName("paciente_id")
    @PropertyName("paciente_id")
    var pacienteId: String? = null,
    
    var mensaje: String? = null,
    var prioridad: String? = null, // "ALTA" | "MEDIA" | "BAJA"
    
    @get:PropertyName("fecha_envio")
    @set:PropertyName("fecha_envio")
    @PropertyName("fecha_envio")
    var fechaEnvio: Long = 0L,
    
    @get:Exclude
    var leida: Boolean = false,
    
    @get:Exclude
    @get:PropertyName("medico_nombre")
    @set:PropertyName("medico_nombre")
    @PropertyName("medico_nombre")
    var medicoNombre: String? = null
)
