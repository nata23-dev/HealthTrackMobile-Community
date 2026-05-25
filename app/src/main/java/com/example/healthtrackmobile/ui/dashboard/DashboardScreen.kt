package com.example.healthtrackmobile.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.launch
import android.widget.Toast
import com.example.healthtrackmobile.util.PdfGeneratorUtil
import com.example.healthtrackmobile.model.Metrica
import com.example.healthtrackmobile.model.Recomendacion
import com.example.healthtrackmobile.theme.*
import com.example.healthtrackmobile.util.shimmerEffect
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

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
    onMedicamentosClick: () -> Unit = {},
    onMetasClick: () -> Unit = {},
    onReportesClick: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showDownloadDialog by remember { mutableStateOf(false) }

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
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            (context as? ComponentActivity)?.finish()
        }
    }

    LaunchedEffect(userId) {
        viewModel.cargarDatosDashboard(userId)
    }

    if (showDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            title = { Text("Descargar Reporte PDF") },
            text = { Text("¿Desea generar y descargar su reporte clínico en formato PDF?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDownloadDialog = false
                        val success = PdfGeneratorUtil.generarReporteClinico(
                            context = context,
                            nombrePaciente = userName,
                            folioHT = userId.take(8),
                            metricas = state.metricasCriticas,
                            recomendaciones = state.recomendaciones,
                            alertas = listOf(state.sugerenciaIA?.mensaje ?: "Sin alertas activas")
                        )
                        if (success) {
                            Toast.makeText(context, "Reporte guardado en Descargas", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Error al generar el reporte", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Guinda4T)
                ) {
                    Text("GENERAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadDialog = false }) {
                    Text("CANCELAR", color = Guinda4T)
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        modifier = modifier,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                // Header Institucional
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Guinda4T, Guinda4T.copy(alpha = 0.8f))
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Dorado4T,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = userName,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Folio: ${userId.take(8).uppercase()}",
                            color = Dorado4T,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                NavigationDrawerItem(
                    label = { Text("Registrar Métricas", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onAddMetricClick()
                    },
                    icon = { Icon(Icons.Default.AddChart, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Guinda4T.copy(alpha = 0.05f),
                        unselectedIconColor = Guinda4T,
                        unselectedTextColor = Guinda4T
                    )
                )

                NavigationDrawerItem(
                    label = { Text("Mis Medicamentos") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onMedicamentosClick()
                    },
                    icon = { Icon(Icons.Default.Medication, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Metas de Salud") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onMetasClick()
                    },
                    icon = { Icon(Icons.Default.EmojiEvents, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Reportes Generales") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onReportesClick()
                    },
                    icon = { Icon(Icons.Default.Assessment, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Directorio Médico") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onDirectoryClick()
                    },
                    icon = { Icon(Icons.Default.LocalHospital, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Descargar Reporte PDF") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showDownloadDialog = true
                    },
                    icon = { Icon(Icons.Default.PictureAsPdf, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(Modifier.weight(1f))

                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(unselectedTextColor = Color.Red, unselectedIconColor = Color.Red)
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    ) {
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
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Rounded.Menu, "Menú", tint = Color.White)
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
                        onClick = onPrevencionClick,
                        icon = { Icon(Icons.Default.Psychology, null) },
                        label = { Text("IA") }
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

                    // Pilar 1: Módulo de Prevención IA Destacado
                    item {
                        state.sugerenciaIA?.let { sugerencia ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = "Sugerencia Preventiva IA: ${sugerencia.mensaje}"
                                    },
                                colors = CardDefaults.cardColors(containerColor = Guinda4T),
                                elevation = CardDefaults.cardElevation(8.dp),
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

                    // Pilar 1: Carrusel de Métricas Críticas
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

                    // Otras secciones (Recomendaciones)
                    item {
                        Text(
                            text = "Recomendaciones Médicas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Guinda4T
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
                }
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
        // Título "Hola, Usuario" shimmer
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .shimmerEffect()
        )
        
        // Tarjeta IA shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmerEffect()
        )
        
        // Título "Estado Crítico" shimmer
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )

        // Carrusel de Métricas shimmer
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
        
        // Título "Recomendaciones" shimmer
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )

        // Lista de Recomendaciones shimmer
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
