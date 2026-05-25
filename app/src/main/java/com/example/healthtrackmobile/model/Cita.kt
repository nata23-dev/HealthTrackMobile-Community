package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Cita(
    var id: String? = null,
    
    @get:PropertyName("paciente_id")
    @set:PropertyName("paciente_id")
    @PropertyName("paciente_id")
    var pacienteId: String? = null,
    
    @get:PropertyName("paciente_nombre")
    @set:PropertyName("paciente_nombre")
    @PropertyName("paciente_nombre")
    var pacienteNombre: String? = null,
    
    @get:PropertyName("medico_id")
    @set:PropertyName("medico_id")
    @PropertyName("medico_id")
    var medicoId: String? = null,
    
    @get:PropertyName("medico_nombre")
    @set:PropertyName("medico_nombre")
    @PropertyName("medico_nombre")
    var medicoNombre: String? = null,
    
    @get:PropertyName("fecha_hora")
    @set:PropertyName("fecha_hora")
    @PropertyName("fecha_hora")
    var fechaHora: Long = 0L, // Epoch timestamp in milliseconds
    
    var estado: String? = "PENDIENTE" // PENDIENTE o COMPLETADA
)
