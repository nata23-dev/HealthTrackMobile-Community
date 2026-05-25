package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.Date
import java.util.UUID

@IgnoreExtraProperties
data class Notificacion(
    var id: String = UUID.randomUUID().toString(),
    var usuarioId: String? = null,
    var titulo: String? = null,
    var mensaje: String? = null,
    var fechaCreacion: Date = Date(),
    var leida: Boolean = false,
    var tipo: String? = null // "RECORDATORIO" | "ALERTA" | "SISTEMA"
)
