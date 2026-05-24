package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties
import java.time.LocalDate
import java.time.Period

@IgnoreExtraProperties
data class PerfilPaciente(
    var id: String? = null,
    var grupoSanguineo: String? = null,
    var alergias: String? = null,
    var fechaNacimiento: String? = null, // ISO-8601: "YYYY-MM-DD"
    var direccion: String? = null,
    var estatura: Double = 0.0, // en cm, ej: 170.5
    var antecedentes: String? = null
) {
    fun calcularEdad(): Int {
        if (fechaNacimiento.isNullOrBlank()) return 0
        return try {
            Period.between(LocalDate.parse(fechaNacimiento), LocalDate.now()).years
        } catch (e: Exception) {
            0
        }
    }
}
