package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Metrica(
    var id: String? = null,
    var pacienteId: String? = null,
    var tipo: String? = null,
    var valor: Double = 0.0,
    var valorSecundario: Double = 0.0,
    var comentario: String? = null,
    var timestamp: Long = 0L
)
