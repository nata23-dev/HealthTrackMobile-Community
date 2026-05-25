package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class MedicoPacienteVinculo(
    @get:PropertyName("medico_id")
    @set:PropertyName("medico_id")
    @PropertyName("medico_id")
    var medicoId: String? = null,
    
    @get:PropertyName("paciente_id")
    @set:PropertyName("paciente_id")
    @PropertyName("paciente_id")
    var pacienteId: String? = null,
    
    @get:PropertyName("fecha_vinculacion")
    @set:PropertyName("fecha_vinculacion")
    @PropertyName("fecha_vinculacion")
    var fechaVinculacion: Long = 0L
)
