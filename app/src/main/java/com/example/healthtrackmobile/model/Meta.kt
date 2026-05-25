package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Meta(
    var id: String? = null,
    
    @get:PropertyName("paciente_id")
    @set:PropertyName("paciente_id")
    @PropertyName("paciente_id")
    var pacienteId: String? = null,
    
    var titulo: String? = null,
    
    @get:PropertyName("tipo_metrica")
    @set:PropertyName("tipo_metrica")
    @PropertyName("tipo_metrica")
    var tipoMetrica: String? = null,
    
    @get:PropertyName("objetivo_numerico")
    @set:PropertyName("objetivo_numerico")
    @PropertyName("objetivo_numerico")
    var objetivoNumerico: Double = 0.0,
    
    @get:PropertyName("valor_inicial")
    @set:PropertyName("valor_inicial")
    @PropertyName("valor_inicial")
    var valorInicial: Double = 0.0,
    
    @get:PropertyName("valor_actual")
    @set:PropertyName("valor_actual")
    @PropertyName("valor_actual")
    var valorActual: Double = 0.0,
    
    @get:PropertyName("progreso_actual")
    @set:PropertyName("progreso_actual")
    @PropertyName("progreso_actual")
    var progresoActual: Double = 0.0,
    
    var estado: String = "ACTIVA",
    var prioridad: Int = 1,
    
    @get:PropertyName("fecha_cumplimiento")
    @set:PropertyName("fecha_cumplimiento")
    @PropertyName("fecha_cumplimiento")
    var fechaCumplimiento: Long = 0L,
    
    var unidad: String? = null,
    
    @get:PropertyName("objetivo_secundario")
    @set:PropertyName("objetivo_secundario")
    @PropertyName("objetivo_secundario")
    var objetivoSecundario: Double = 0.0,
    
    @get:PropertyName("valor_inicial_secundario")
    @set:PropertyName("valor_inicial_secundario")
    @PropertyName("valor_inicial_secundario")
    var valorInicialSecundario: Double = 0.0,
    
    @get:PropertyName("valor_actual_secundario")
    @set:PropertyName("valor_actual_secundario")
    @PropertyName("valor_actual_secundario")
    var valorActualSecundario: Double = 0.0
)
