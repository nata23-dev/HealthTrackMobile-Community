package com.example.healthtrackmobile

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Login : NavKey
@Serializable data class Main(val userId: String, val userName: String) : NavKey
@Serializable data class AgregarMetricas(val userId: String) : NavKey
@Serializable data class PerfilClinico(val userId: String) : NavKey
@Serializable data object DirectorioMedico : NavKey
@Serializable data class CitasMedicas(val userId: String, val userName: String) : NavKey
@Serializable data class PrevencionIA(val userId: String) : NavKey
@Serializable data object Onboarding : NavKey
@Serializable data class ConfiguracionInicial(val userId: String) : NavKey
@Serializable data class MisMedicamentos(val userId: String) : NavKey
@Serializable data class MetasDeSalud(val userId: String) : NavKey
@Serializable data class ReportesGenerales(val userId: String) : NavKey
@Serializable data class TendenciasDeSalud(val userId: String) : NavKey

// Nuevas claves para Navegación Mobile-First
@Serializable data object InicioTab : NavKey
@Serializable data object MetricasTab : NavKey
@Serializable data object CitasTab : NavKey
@Serializable data object PerfilTab : NavKey


