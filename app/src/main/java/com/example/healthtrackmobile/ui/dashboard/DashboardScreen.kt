package com.example.healthtrackmobile.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import com.example.healthtrackmobile.model.Metrica
import com.example.healthtrackmobile.model.Recomendacion
import com.example.healthtrackmobile.model.HistorialLogro
import com.example.healthtrackmobile.model.Notificacion
import com.example.healthtrackmobile.theme.*
import com.example.healthtrackmobile.util.shimmerEffect
import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch

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
    onPrevencionClick: () -> Unit,
    onTendenciasClick: () -> Unit,
    onMedicamentosClick: () -> Unit = {},
    onMetasClick: () -> Unit = {},
    onReportesClick: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showNotificationsSheet by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Cerrar la app al presionar atrás en el Dashboard
    BackHandler {
        (context as? ComponentActivity)?.finish()
    }

    LaunchedEffect(userId) {
        viewModel.cargarDatosDashboard(userId)
    }

    // ModalBottomSheet para Centro de Notificaciones
    if (showNotificationsSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showNotificationsSheet = false
                viewModel.marcarNotificacionesComoLeidas(userId)
            },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Notificaciones Oficiales",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GuindaOficial
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (state.notificaciones.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No tienes notificaciones o avisos recientes", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.notificaciones) { notif ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (notif.leida) Color.White else Dorado4T.copy(alpha = 0.05f)
                                ),
                                border = BorderStroke(1.dp, if (notif.leida) Color.LightGray.copy(alpha = 0.5f) else DoradoOficial),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = notif.titulo ?: "Notificación",
                                            fontWeight = FontWeight.Bold,
                                            color = GuindaOficial,
                                            fontSize = 14.sp
                                        )
                                        if (!notif.leida) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(Color.Red, CircleShape)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = notif.mensaje ?: "",
                                        fontSize = 13.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
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
                            text = "HealthTrack",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // Abrir centro de avisos
                        showNotificationsSheet = true
                    }) {
                        BadgedBox(
                            badge = {
                                if (state.notificacionesNoLeidas > 0) {
                                    Badge(containerColor = Color.Red) {
                                        Text(state.notificacionesNoLeidas.toString(), color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, "Notificaciones", tint = Color.White)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Salir", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Guinda4T)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Inicio") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Guinda4T,
                        indicatorColor = Dorado4T.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onTendenciasClick,
                    icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, null) },
                    label = { Text("Tendencias") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onCitasClick,
                    icon = { Icon(Icons.Default.DateRange, null) },
                    label = { Text("Citas") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onProfileClick,
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Perfil") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMetricClick,
                containerColor = GuindaOficial,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.AddChart, "Registrar Métricas")
            }
        },
        containerColor = Fondo4T
    ) { paddingValues ->
        if (state.isLoading) {
            DashboardShimmer(paddingValues)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
            ) {
                item {
                    Text(
                        text = "Hola, $userName",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Guinda4T
                    )
                }

                // Prevención IA Destacado (Al hacer clic, navega a la pantalla completa)
                item {
                    state.sugerenciaIA?.let { sugerencia ->
                        Card(
                            onClick = onPrevencionClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics(mergeDescendants = true) {
                                    contentDescription = "Sugerencia Preventiva IA: ${sugerencia.mensaje}"
                                },
                            colors = CardDefaults.cardColors(containerColor = Guinda4T),
                            elevation = CardDefaults.cardElevation(4.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "IA",
                                    tint = Dorado4T,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Sugerencia Preventiva IA",
                                        color = Dorado4T,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = sugerencia.mensaje ?: "",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Carrusel de Métricas Críticas
                item {
                    Text(
                        text = "Estado de Salud Crítico",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Guinda4T,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        item {
                            MetricMiniCard(
                                title = "Glucosa",
                                value = state.ultimaGlucosa?.valor?.let { "${it.toInt()}" } ?: "--",
                                unit = "mg/dL",
                                icon = Icons.Default.Bloodtype,
                                isWarning = (state.ultimaGlucosa?.valor ?: 0.0) > 125.0
                            )
                        }
                        item {
                            val sis = state.ultimaPresion?.valor?.toInt() ?: 0
                            val dia = state.ultimaPresion?.valorSecundario?.toInt() ?: 0
                            MetricMiniCard(
                                title = "Presión",
                                value = if (sis > 0) "$sis/$dia" else "--",
                                unit = "mmHg",
                                icon = Icons.Default.Speed,
                                isWarning = sis > 135 || dia > 85
                            )
                        }
                        item {
                            MetricMiniCard(
                                title = "Ritmo",
                                value = state.ultimaFrecuencia?.valor?.let { "${it.toInt()}" } ?: "--",
                                unit = "lpm",
                                icon = Icons.Default.Favorite,
                                isWarning = (state.ultimaFrecuencia?.valor ?: 0.0) > 100.0
                            )
                        }
                    }
                }

                // Cuadrícula de Accesos Rápidos
                item {
                    Text(
                        text = "Servicios y Herramientas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Guinda4T,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ServiceCard(
                                title = "Medicamentos",
                                icon = Icons.Default.Medication,
                                onClick = onMedicamentosClick,
                                modifier = Modifier.weight(1f)
                            )
                            ServiceCard(
                                title = "Metas de Salud",
                                icon = Icons.Default.EmojiEvents,
                                onClick = onMetasClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ServiceCard(
                                title = "Reportes Clínicos",
                                icon = Icons.Default.Assessment,
                                onClick = onReportesClick,
                                modifier = Modifier.weight(1f)
                            )
                            ServiceCard(
                                title = "Directorio Médico",
                                icon = Icons.Default.LocalHospital,
                                onClick = onDirectoryClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Otras recomendaciones
                item {
                    Text(
                        text = "Recomendaciones Médicas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Guinda4T,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                if (state.recomendaciones.isEmpty()) {
                    item {
                        Text("No hay recomendaciones nuevas", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    items(state.recomendaciones.take(3)) { rec ->
                        RecommendationItem(rec)
                    }
                }

                // Sección de Logros Obtenidos (Paridad con Desktop)
                if (state.logros.isNotEmpty()) {
                    item {
                        Text(
                            text = "Mis Logros Obtenidos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Guinda4T,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            items(state.logros) { logro ->
                                LogroCard(logro)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Guinda4T.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Guinda4T, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Guinda4T
            )
        }
    }
}

@Composable
fun LogroCard(logro: HistorialLogro) {
    Card(
        modifier = Modifier.width(240.dp).height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Dorado4T.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Dorado4T.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.EmojiEvents, null, tint = DoradoOficial, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = logro.titulo ?: "Logro alcanzado",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = GuindaOficial,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = logro.descripcion ?: "",
                    fontSize = 10.sp,
                    color = Color.DarkGray,
                    maxLines = 3,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
fun DashboardShimmer(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .shimmerEffect()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmerEffect()
        )
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(width = 130.dp, height = 110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerEffect()
                )
            }
        }
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        repeat(2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerEffect()
            )
        }
    }
}

@Composable
fun MetricMiniCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    isWarning: Boolean
) {
    Card(
        modifier = Modifier
            .size(width = 130.dp, height = 110.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "Métrica de $title: $value $unit. ${if (isWarning) "Estado de alerta" else "Estado normal"}"
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning) Color(0xFFFDE8E8) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = if (isWarning) Color.Red else Guinda4T, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(title, fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = if (isWarning) Color.Red else Guinda4T)
            Text(unit, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun RecommendationItem(rec: Recomendacion) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "Recomendación de ${rec.medicoNombre}: ${rec.mensaje}. Prioridad ${rec.prioridad}"
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(4.dp, 40.dp).background(if (rec.prioridad == "ALTA") Color.Red else Dorado4T))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = rec.medicoNombre ?: "Médico",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = rec.mensaje ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )
            }
        }
    }
}
