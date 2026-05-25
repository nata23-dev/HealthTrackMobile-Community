package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.UUID

@IgnoreExtraProperties
data class ClinicaHospital(
    var id: String? = null,
    var nombre: String? = null,
    var direccion: String? = null,
    var ciudad: String? = null,
    var telefono: String? = null,
    var emailContacto: String? = null,
    var estado: String? = "activo"
) {
    // Constructor secundario para inicializar con valores por defecto
    constructor(nombre: String, direccion: String, ciudad: String, telefono: String, emailContacto: String) : this(
        id = UUID.randomUUID().toString(),
        nombre = nombre,
        direccion = direccion,
        ciudad = ciudad,
        telefono = telefono,
        emailContacto = emailContacto,
        estado = "activo"
    )
}
