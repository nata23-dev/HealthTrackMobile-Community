package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class MedicoPacienteVinculo(
    var medicoId: String? = null,
    var pacienteId: String? = null,
    var fechaVinculacion: Long = 0L
)
