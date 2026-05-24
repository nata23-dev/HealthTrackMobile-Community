package com.example.healthtrackmobile.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtrackmobile.model.HistorialLogro
import com.example.healthtrackmobile.model.Metrica
import com.example.healthtrackmobile.model.Recomendacion
import com.example.healthtrackmobile.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userId: String,
    userName: String,
    onLogout: () -> Unit,
    onAddMetricClick: () -> Unit,
    onProfileClick: () -> Unit,
    onDirectoryClick: () -> Unit,
    onCitasClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Cargar datos al entrar a la pantalla
    LaunchedEffect(userId) {
        viewModel.cargarDatosDashboard(userId)
        com.example.healthtrackmobile.receiver.ReminderSyncManager.syncReminders(context, userId)
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
                            text = "Ficha Clínica de Monitoreo",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCitasClick) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Citas Médicas",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onDirectoryClick) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = "Directorio Médico",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Ficha Clínica",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Guinda4T
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMetricClick,
                containerColor = Guinda4T,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Registrar Métrica"
                )
            }
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
                CircularProgressIndicator(color = Guinda4T)
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
                            text = state.error ?: "Error al conectar con la base de datos",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.cargarDatosDashboard(userId) },
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
                // Bienvenida
                item {
                    Column {
                        Text(
                            text = "Hola, $userName",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Guinda4T
                        )
                        Text(
                            text = "Estrategia Nacional de Monitoreo Crónico",
                            fontSize = 14.sp,
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

                // Sección 1: Métricas de Salud (Grid simulado)
                item {
                    Text(
                        text = "Métricas de Salud Recientes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Guinda4T,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                MetricCard(
                                    title = "Glucosa",
                                    value = state.ultimaGlucosa?.valor?.let { "${it.toInt()} mg/dL" } ?: "Sin datos",
                                    status = state.ultimaGlucosa?.let { 
                                        if (it.valor <= 125.0) "Normal" else "Atención"
                                    } ?: "N/A",
                                    icon = Icons.Default.Bloodtype,
                                    isWarning = state.ultimaGlucosa?.let { it.valor > 125.0 } ?: false
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                val presionSistolica = state.ultimaPresion?.valor ?: 0.0
                                val presionDiastolica = state.ultimaPresion?.valorSecundario ?: 0.0
                                val isHigh = presionSistolica > 140.0 || presionDiastolica > 90.0
                                val hasData = state.ultimaPresion != null
                                
                                MetricCard(
                                    title = "Presión Arterial",
                                    value = if (hasData) "${presionSistolica.toInt()}/${presionDiastolica.toInt()}" else "Sin datos",
                                    status = if (hasData) {
                                        if (isHigh) "Atención" else "Normal"
                                    } else "N/A",
                                    icon = Icons.Default.Speed,
                                    isWarning = hasData && isHigh
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                MetricCard(
                                    title = "Ritmo Cardíaco",
                                    value = state.ultimaFrecuencia?.valor?.let { "${it.toInt()} lpm" } ?: "Sin datos",
                                    status = state.ultimaFrecuencia?.let { 
                                        if (it.valor <= 100.0) "Normal" else "Atención"
                                    } ?: "N/A",
                                    icon = Icons.Default.Favorite,
                                    isWarning = state.ultimaFrecuencia?.let { it.valor > 100.0 } ?: false
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                MetricCard(
                                    title = "Peso Corporal",
                                    value = state.ultimoPeso?.valor?.let { "$it kg" } ?: "Sin datos",
                                    status = if (state.ultimoPeso != null) "Registrado" else "N/A",
                                    icon = Icons.Default.Scale,
                                    isWarning = false
                                )
                            }
                        }
                    }
                }

                // Sección 2: Recomendaciones Médicas
                item {
                    Text(
                        text = "Recomendaciones Médicas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Guinda4T,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                if (state.recomendaciones.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "No tienes recomendaciones médicas pendientes.",
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(state.recomendaciones) { recomendacion ->
                        RecommendationCard(recomendacion)
                    }
                }

                // Sección 3: Logros de Salud
                item {
                    Text(
                        text = "Mis Logros de Salud",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Guinda4T,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                if (state.logros.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Continúa registrando tus métricas para desbloquear logros.",
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(state.logros) { logro ->
                        LogroCard(logro)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    status: String,
    icon: ImageVector,
    isWarning: Boolean
) {
    val containerColor = if (isWarning) Color(0xFFFDE8E8) else Color.White
    val contentColor = if (isWarning) Color(0xFFC81E1E) else Guinda4T
    val statusColor = if (status == "Normal") VerdeSalud4T else if (status == "Atención") Color(0xFFC81E1E) else Color.Gray

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = status,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}

@Composable
fun RecommendationCard(rec: Recomendacion) {
    val formattedDate = remember(rec.fechaEnvio) {
        if (rec.fechaEnvio > 0) {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            sdf.format(Date(rec.fechaEnvio))
        } else ""
    }

    val badgeColor = when (rec.prioridad?.uppercase()) {
        "ALTA" -> Color(0xFFC81E1E)
        "MEDIA" -> Dorado4T
        else -> VerdeSalud4T
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, Guinda4T, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF4EB)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rec.medicoNombre ?: "Recomendación Médica",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Guinda4T
                )
                
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = rec.prioridad ?: "MEDIA",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = rec.mensaje ?: "",
                fontSize = 14.sp,
                color = Color(0xFF2C3E50),
                lineHeight = 20.sp
            )
            if (formattedDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enviado: $formattedDate",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun LogroCard(logro: HistorialLogro) {
    val formattedDate = remember(logro.timestamp) {
        if (logro.timestamp > 0) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(logro.timestamp))
        } else ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFFEF3C7), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Logro",
                    tint = Dorado4T,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = logro.titulo ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Guinda4T
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = logro.descripcion ?: "",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            if (formattedDate.isNotEmpty()) {
                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
