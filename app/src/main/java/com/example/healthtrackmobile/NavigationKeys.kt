package com.example.healthtrackmobile

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Login : NavKey
@Serializable data class Main(val userId: String, val userName: String) : NavKey
@Serializable data class AgregarMetrica(val userId: String) : NavKey
@Serializable data class PerfilClinico(val userId: String) : NavKey
@Serializable data object DirectorioMedico : NavKey
@Serializable data class CitasMedicas(val userId: String, val userName: String) : NavKey


