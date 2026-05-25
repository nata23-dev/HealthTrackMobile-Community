package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Usuario(
    var id: String? = null,
    @get:PropertyName("nombre")
    @set:PropertyName("nombre")
    @PropertyName("nombre")
    var nombre: String? = null,
    var correo: String? = null,
    var telefono: String? = null,
    var rol: String? = null, // "paciente" | "medico" | "admin"
    var password: String? = null,
    
    @get:PropertyName("activo")
    @set:PropertyName("activo")
    @PropertyName("activo")
    var activo: Boolean = true,
    
    var fechaRegistro: Long = 0L
)
