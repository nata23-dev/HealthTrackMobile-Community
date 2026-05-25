package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.UUID

@IgnoreExtraProperties
data class RecordatorioMedicamento(
    var id: String = UUID.randomUUID().toString(),
    var pacienteId: String? = null,
    var medicamento: String? = null,
    var dosis: String? = null,
    var frecuencia: String? = null,
    var horaRecordatorio: String? = null, // "HH:mm"
    var fechaInicio: String? = null, // "yyyy-MM-dd"
    var fechaFin: String? = null, // "yyyy-MM-dd"
    var estado: String = "activo"
)
