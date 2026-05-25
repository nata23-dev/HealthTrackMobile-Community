package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Cita(
    var id: String? = null,
    var pacienteId: String? = null,
    var pacienteNombre: String? = null,
    var medicoId: String? = null,
    var medicoNombre: String? = null,
    var fechaHora: Long = 0L, // Epoch timestamp in milliseconds
    var estado: String? = "PENDIENTE" // PENDIENTE o COMPLETADA
)
