package com.example.healthtrackmobile.ui.prevencion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtrackmobile.model.Recomendacion
import com.example.healthtrackmobile.service.AlertaSanitariaResponse
import com.example.healthtrackmobile.service.ClimaResponse
import com.example.healthtrackmobile.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrevencionScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PrevencionViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Cargar datos al iniciar la pantalla
    LaunchedEffect(userId) {
        viewModel.cargarDatosPrevencion(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "GOBIERNO DE MÉXICO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Dorado4T,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Prevención IA",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarDatosPrevencion(userId) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Guinda4T
                )
            )
        },
        containerColor = Fondo4T,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Guinda4T)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analizando contexto clínico y ambiental...",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else if (state.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.error ?: "Error al generar el reporte de prevención",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.cargarDatosPrevencion(userId) },
                            colors = ButtonDefaults.buttonColors(containerColor = Guinda4T)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                // Cabecera de Sección
                item {
                    Column {
                        Text(
                            text = "Análisis Clínico y Ambiental IA",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Guinda4T
                        )
                        Text(
                            text = "Recomendaciones personalizadas basadas en su estado y el entorno",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Dorado4T)
                        )
                    }
                }

                // 1. Tarjeta de Clima y Calidad de Aire
                state.clima?.let { clima ->
                    item {
                        WeatherCard(clima = clima)
                    }
                }

                // 2. Sección de Alertas Sanitarias
                item {
                    Text(
                        text = "Alertas Sanitarias Regionales",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Guinda4T,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (state.alertas.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Sin Alertas",
                                    tint = VerdeSalud4T,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Sin alertas sanitarias o epidemiológicas vigentes en su región.",
                                    color = Color.DarkGray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    items(state.alertas) { alerta ->
                        AlertaSanitariaCard(alerta = alerta)
                    }
                }

                // 3. Recomendaciones del Motor IA
                item {
                    Text(
                        text = "Sugerencias de Prevención IA",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Guinda4T,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (state.sugerencias.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Sin sugerencias especiales de prevención por ahora. Registre más métricas para recibir análisis personalizados.",
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(state.sugerencias) { sugerencia ->
                        SugerenciaIACard(sugerencia = sugerencia)
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherCard(clima: ClimaResponse) {
    val aqiColor = if (clima.calidadAireRiesgosa) Color(0xFFC81E1E) else VerdeSalud4T
    val airLabel = if (clima.calidadAireRiesgosa) "RIESGO" else "SALUDABLE"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = "Clima",
                        tint = Guinda4T,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = clima.ciudad,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Guinda4T
                    )
                }

                Surface(
                    color = if (clima.calidadAireRiesgosa) Color(0xFFFDE8E8) else Color(0xFFE6F4EA),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = airLabel,
                        color = aqiColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${clima.temperatura.toInt()}°C",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Guinda4T
                    )
                    Text(
                        text = clima.condicion,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Humedad: ${clima.humedad.toInt()}%",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = clima.calidadAire,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = aqiColor,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
fun AlertaSanitariaCard(alerta: AlertaSanitariaResponse) {
    val containerColor = if (alerta.nivelRiesgo == "ALTA") Color(0xFFFDE8E8) else Color(0xFFFEF3C7)
    val contentColor = if (alerta.nivelRiesgo == "ALTA") Color(0xFFC81E1E) else Dorado4T
    val borderStrokeColor = if (alerta.nivelRiesgo == "ALTA") Color(0xFFF8B4B4) else Color(0xFFFDE047)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderStrokeColor, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = "Alerta",
                tint = contentColor,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alerta: ${alerta.region}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = contentColor
                    )
                    Surface(
                        color = contentColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = alerta.nivelRiesgo,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alerta.descripcion,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun SugerenciaIACard(sugerencia: Recomendacion) {
    val leftBorderColor = when (sugerencia.prioridad?.uppercase()) {
        "ALTA" -> Guinda4T
        "INFO" -> Dorado4T
        else -> VerdeSalud4T // ej. MEDIA/BAJA
    }

    val badgeColor = when (sugerencia.prioridad?.uppercase()) {
        "ALTA" -> Color(0xFFC81E1E)
        "MEDIA" -> Dorado4T
        else -> VerdeSalud4T
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFEDEDED), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF4EB)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Franja de color en el borde izquierdo para indicar prioridad (Estilo 4T)
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically)
                    .background(leftBorderColor)
                    .height(90.dp) // altura de contingencia para forzar dibujo vertical completo
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFFEAEAEA), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (sugerencia.prioridad?.uppercase()) {
                                    "ALTA" -> Icons.Default.HealthAndSafety
                                    "MEDIA" -> Icons.Default.Restaurant
                                    else -> Icons.Default.SelfImprovement
                                },
                                contentDescription = null,
                                tint = leftBorderColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = sugerencia.medicoNombre ?: "Recomendación IA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Guinda4T
                        )
                    }

                    Surface(
                        color = badgeColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = sugerencia.prioridad ?: "BAJA",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = sugerencia.mensaje ?: "",
                    fontSize = 13.sp,
                    color = Color(0xFF2C3E50),
                    lineHeight = 19.sp
                )
            }
        }
    }
}
