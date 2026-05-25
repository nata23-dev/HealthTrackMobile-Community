package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import java.util.UUID

@IgnoreExtraProperties
data class RecordatorioMedicamento(
    var id: String = UUID.randomUUID().toString(),
    
    @get:PropertyName("paciente_id")
    @set:PropertyName("paciente_id")
    @PropertyName("paciente_id")
    var pacienteId: String? = null,
    
    var medicamento: String? = null,
    var dosis: String? = null,
    var frecuencia: String? = null,
    
    @get:PropertyName("hora_recordatorio")
    @set:PropertyName("hora_recordatorio")
    @PropertyName("hora_recordatorio")
    var horaRecordatorio: String? = null, // "HH:mm"
    
    @get:PropertyName("fecha_inicio")
    @set:PropertyName("fecha_inicio")
    @PropertyName("fecha_inicio")
    var fechaInicio: String? = null, // "yyyy-MM-dd"
    
    @get:PropertyName("fecha_fin")
    @set:PropertyName("fecha_fin")
    @PropertyName("fecha_fin")
    var fechaFin: String? = null, // "yyyy-MM-dd"
    
    var estado: String = "activo"
)
