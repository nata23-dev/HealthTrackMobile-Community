package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import java.util.Date
import java.util.UUID

@IgnoreExtraProperties
data class Notificacion(
    var id: String = UUID.randomUUID().toString(),
    
    @get:PropertyName("usuario_id")
    @set:PropertyName("usuario_id")
    @PropertyName("usuario_id")
    var usuarioId: String? = null,
    
    var titulo: String? = null,
    var mensaje: String? = null,
    
    @get:PropertyName("fecha_creacion")
    @set:PropertyName("fecha_creacion")
    @PropertyName("fecha_creacion")
    var fechaCreacion: Date = Date(),
    
    var leida: Boolean = false,
    var tipo: String? = null // "RECORDATORIO" | "ALERTA" | "SISTEMA"
)
