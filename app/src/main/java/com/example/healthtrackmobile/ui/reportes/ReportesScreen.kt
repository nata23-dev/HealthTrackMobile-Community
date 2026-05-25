package com.example.healthtrackmobile.ui.reportes

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtrackmobile.theme.*
import com.example.healthtrackmobile.util.WhatsAppShareUtils
import com.example.healthtrackmobile.util.PdfGeneratorUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: ReportesViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        viewModel.cargarDatosReporte(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes Generales", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Guinda4T)
            )
        },
        containerColor = Fondo4T
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Cabecera Institucional
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("GOBIERNO DE MÉXICO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Guinda4T, letterSpacing = 1.sp)
                Text("Resumen Clínico Ejecutivo", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Guinda4T)
                Text("Reporte emitido desde: Celaya, Gto.", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Dorado4T))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Guinda4T)
                }
            } else {
                Card(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("Últimos Registros", fontWeight = FontWeight.Bold, color = Guinda4T)
                        }
                        
                        if (state.metricasRecientes.isEmpty()) {
                            item {
                                Text("No hay métricas registradas recientemente.", color = Color.Gray, fontSize = 14.sp)
                            }
                        } else {
                            items(state.metricasRecientes) { metrica ->
                                val tipo = metrica.tipo?.uppercase() ?: "MÉTRICA"
                                val valor = if (tipo == "PRESION") "${metrica.valor.toInt()}/${metrica.valorSecundario.toInt()} mmHg" 
                                            else "${metrica.valor} ${when (tipo) {
                                                "GLUCOSA" -> "mg/dL"
                                                "FRECUENCIA", "FRECUENCIA_CARDIACA", "RITMO" -> "lpm"
                                                "PESO" -> "kg"
                                                else -> ""
                                            }}"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(tipo, fontSize = 14.sp)
                                    Text(valor, fontWeight = FontWeight.Bold, color = Guinda4T)
                                }
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            }
                        }

                        if (state.recomendaciones.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Indicaciones y Sugerencias IA", fontWeight = FontWeight.Bold, color = Guinda4T)
                            }
                            items(state.recomendaciones.take(3)) { rec ->
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(rec.mensaje ?: "", fontSize = 13.sp, color = Color.DarkGray)
                                    Text("Prioridad: ${rec.prioridad ?: "BAJA"}", fontSize = 11.sp, color = Dorado4T, fontWeight = FontWeight.Bold)
                                }
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    WhatsAppShareUtils.compartirPorWhatsApp(context, state.resumenMensaje)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Guinda4T),
                shape = RoundedCornerShape(8.dp),
                enabled = !state.isLoading && state.resumenMensaje.isNotEmpty()
            ) {
                Icon(Icons.Default.Share, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("ENVIAR REPORTE A MÉDICO", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    val success = PdfGeneratorUtil.generarReporteClinico(
                        context = context,
                        nombrePaciente = state.nombrePaciente,
                        folioHT = state.folioHT,
                        metricas = state.metricasRecientes,
                        recomendaciones = state.recomendaciones,
                        alertas = state.alertas
                    )
                    if (success) {
                        Toast.makeText(context, "Ficha clínica guardada en Descargas", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Error al generar la ficha clínica", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                border = BorderStroke(1.dp, Guinda4T),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Guinda4T),
                shape = RoundedCornerShape(8.dp),
                enabled = !state.isLoading && state.nombrePaciente.isNotEmpty()
            ) {
                Icon(Icons.Default.PictureAsPdf, null, tint = Guinda4T)
                Spacer(Modifier.width(8.dp))
                Text("DESCARGAR FICHA CLÍNICA PDF", color = Guinda4T, fontWeight = FontWeight.Bold)
            }
        }
    }
}
