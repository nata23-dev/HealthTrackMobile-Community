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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.healthtrackmobile.model.Recomendacion
import com.example.healthtrackmobile.service.AlertaSanitariaResponse
import com.example.healthtrackmobile.service.ClimaResponse
import com.example.healthtrackmobile.theme.*
import com.example.healthtrackmobile.util.shimmerEffect

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
            PrevencionShimmer(paddingValues)
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

                // Banner de Modo Offline
                if (state.isOfflineMode) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Dorado4T),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.WifiOff, null, tint = Guinda4T)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = "Modo sin conexión. Mostrando recomendaciones generales estacionales.",
                                    color = Guinda4T,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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
fun PrevencionShimmer(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .shimmerEffect()
        )
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        repeat(2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerEffect()
            )
        }
    }
}

@Composable
fun WeatherCard(clima: ClimaResponse) {
    val aqiColor = when {
        clima.aqiValue <= 50 -> VerdeSalud4T
        clima.aqiValue <= 100 -> Color(0xFFEAB308) // Dorado/Amarillo
        else -> Color(0xFFC81E1E) // Rojo
    }
    val airLabel = when {
        clima.aqiValue <= 50 -> "SALUDABLE"
        clima.aqiValue <= 100 -> "MODERADA"
        else -> "RIESGO"
    }
    val airBgColor = when {
        clima.aqiValue <= 50 -> Color(0xFFE6F4EA)
        clima.aqiValue <= 100 -> Color(0xFFFEF9C3)
        else -> Color(0xFFFDE8E8)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "Clima en ${clima.ciudad}: ${clima.temperatura.toInt()} grados, ${clima.condicion}. Calidad del aire $airLabel."
            },
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
                    color = airBgColor,
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
            .border(1.dp, borderStrokeColor, RoundedCornerShape(8.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = "Alerta Sanitaria en ${alerta.region}: ${alerta.descripcion}. Nivel de riesgo ${alerta.nivelRiesgo}."
            },
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
        "MEDIA" -> Dorado4T
        else -> VerdeSalud4T // ej. BAJA
    }

    val badgeColor = when (sugerencia.prioridad?.uppercase()) {
        "ALTA" -> Color(0xFFC81E1E)
        "MEDIA" -> Dorado4T
        else -> VerdeSalud4T
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFEDEDED), RoundedCornerShape(12.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = "Sugerencia de Prevención IA: ${sugerencia.mensaje}. Prioridad ${sugerencia.prioridad}."
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 19.sp
                )
            }
        }
    }
}
