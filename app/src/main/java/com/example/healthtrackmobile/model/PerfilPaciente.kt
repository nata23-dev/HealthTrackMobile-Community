package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@IgnoreExtraProperties
data class PerfilPaciente(
    var id: String? = null,
    
    @get:PropertyName("grupo_sanguineo")
    @set:PropertyName("grupo_sanguineo")
    @PropertyName("grupo_sanguineo")
    var grupoSanguineo: String? = null,
    
    var alergias: String? = null,
    
    @get:PropertyName("fecha_nacimiento")
    @set:PropertyName("fecha_nacimiento")
    @PropertyName("fecha_nacimiento")
    var fechaNacimiento: String? = null, // ISO-8601: "YYYY-MM-DD"
    
    var direccion: String? = null,
    
    @get:PropertyName("estatura_inicial")
    @set:PropertyName("estatura_inicial")
    @PropertyName("estatura_inicial")
    var estatura: Double = 0.0, // en cm, ej: 170.5
    
    @get:PropertyName("antecedentes_cronicos")
    @set:PropertyName("antecedentes_cronicos")
    @PropertyName("antecedentes_cronicos")
    var antecedentes: String? = null,
    
    @get:PropertyName("peso_inicial")
    @set:PropertyName("peso_inicial")
    @PropertyName("peso_inicial")
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
