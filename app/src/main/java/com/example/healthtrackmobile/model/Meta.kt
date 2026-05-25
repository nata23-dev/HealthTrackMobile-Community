package com.example.healthtrackmobile.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Meta(
    var id: String? = null,
    var pacienteId: String? = null,
    var titulo: String? = null,
    var tipoMetrica: String? = null,
    var objetivoNumerico: Double = 0.0,
    var valorInicial: Double = 0.0,
    var valorActual: Double = 0.0,
    var progresoActual: Double = 0.0,
    var estado: String = "activo",
    var prioridad: Int = 1,
    var fechaCumplimiento: Long = 0L,
    var unidad: String? = null,
    var objetivoSecundario: Double = 0.0,
    var valorInicialSecundario: Double = 0.0,
    var valorActualSecundario: Double = 0.0
)
