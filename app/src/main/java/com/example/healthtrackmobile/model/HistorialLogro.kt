package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class HistorialLogro(
    var id: String? = null,
    var pacienteId: String? = null,
    var titulo: String? = null,
    var descripcion: String? = null,
    var timestamp: Long = 0L
)
