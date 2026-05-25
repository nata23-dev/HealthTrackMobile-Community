package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@IgnoreExtraProperties
data class PerfilPaciente(
    var id: String? = null,
    var grupoSanguineo: String? = null,
    var alergias: String? = null,
    var fechaNacimiento: String? = null, // ISO-8601: "YYYY-MM-DD"
    var direccion: String? = null,
    var estatura: Double = 0.0, // en cm, ej: 170.5
    var antecedentes: String? = null,
    var pesoInicial: Double = 0.0
) {
    fun calcularEdad(): Int {
        val dob = fechaNacimiento ?: return 0
        if (dob.isBlank()) return 0
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val birthDate = sdf.parse(dob) ?: return 0
            val today = Calendar.getInstance()
            val birth = Calendar.getInstance()
            birth.time = birthDate
            
            var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age
        } catch (e: Exception) {
            0
        }
    }
}
