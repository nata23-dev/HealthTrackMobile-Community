package com.example.healthtrackmobile.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

// Importar todas las pantallas secundarias para navegación embebida
import com.example.healthtrackmobile.ui.metricas.MetricasPage
import com.example.healthtrackmobile.ui.metas.MetasScreen
import com.example.healthtrackmobile.ui.medicamentos.MedicamentosScreen
import com.example.healthtrackmobile.ui.citas.CitasScreen
import com.example.healthtrackmobile.ui.prevencion.PrevencionScreen
import com.example.healthtrackmobile.ui.tendencias.TendenciasScreen
import com.example.healthtrackmobile.ui.directorio.DirectorioMedicoScreen
import com.example.healthtrackmobile.ui.reportes.ReportesScreen
import com.example.healthtrackmobile.ui.perfil.PerfilClinicoScreen
import com.example.healthtrackmobile.ui.familia.FamiliaScreen

enum class DashboardTab(val title: String, val icon: ImageVector) {
    INICIO("Inicio", Icons.Default.Home),
    METRICAS("Métricas", Icons.Default.AddChart),
    TENDENCIAS("Tendencias", Icons.AutoMirrored.Filled.TrendingUp),
    RECOMENDACIONES("Recomendaciones", Icons.Default.Feedback),
    CITAS("Citas", Icons.Default.DateRange),
    PREVENCION_IA("Prevención IA", Icons.Default.Psychology),
    METAS("Metas", Icons.Default.EmojiEvents),
    MEDICAMENTOS("Mis Medicamentos", Icons.Default.Medication),
    DIRECTORIO("Directorio Médico", Icons.Default.LocalHospital),
    REPORTES("Reportes", Icons.Default.Assessment),
    PERFIL_CLINICO("Mi Perfil Clínico", Icons.Default.Person),
    MI_FAMILIA("Mi Familia", Icons.Default.People)
}

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
    onTendenciasClick: () -> Unit,
    onPrevencionClick: () -> Unit = {},
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
    
    // Control de pestaña actual del Drawer
    var currentTab by remember { mutableStateOf(DashboardTab.INICIO) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Al presionar atrás, si el drawer está abierto lo cierra, si no, cierra la app en Inicio o vuelve a Inicio.
    BackHandler {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (currentTab != DashboardTab.INICIO) {
            currentTab = DashboardTab.INICIO
        } else {
            (context as? ComponentActivity)?.finish()
        }
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Fondo4T,
                modifier = Modifier.width(310.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Institucional de México
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GuindaOficial)
                            .padding(24.dp)
                    ) {
                        Column {
                            Text(
                                text = "GOBIERNO DE MÉXICO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Dorado4T,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = userName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "FOLIO: ${userId.take(8).uppercase()}",
                                fontSize = 12.sp,
                                color = DoradoOficial
                            )
                        }
                    }
                    
                    // Listado Scrollable de Páginas
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 12.dp)
                    ) {
                        val perfilIncompleto = state.perfil == null || (state.perfil?.estatura ?: 0.0) <= 0.0
                        DashboardTab.values().forEach { tab ->
                            val isSelected = tab == currentTab
                            val isLocked = perfilIncompleto && tab != DashboardTab.INICIO && tab != DashboardTab.PERFIL_CLINICO
                            NavigationDrawerItem(
                                icon = { Icon(tab.icon, contentDescription = null, tint = if (isSelected) Color.White else if (isLocked) Color.LightGray else GuindaOficial) },
                                label = { Text(tab.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isLocked) Color.LightGray else Color.Unspecified) },
                                selected = isSelected,
                                onClick = {
                                    if (isLocked) {
                                        Toast.makeText(context, "Para desbloquear tu panel, necesitamos que completes tu Perfil Clínico.", Toast.LENGTH_LONG).show()
                                    } else {
                                        currentTab = tab
                                        scope.launch { drawerState.close() }
                                    }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = GuindaOficial,
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color.White,
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedIconColor = if (isLocked) Color.LightGray else GuindaOficial,
                                    unselectedTextColor = if (isLocked) Color.LightGray else Color.Black
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    
                    // Salida / Logout
                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red) },
                        label = { Text("Cerrar Sesión", color = Color.Red, fontWeight = FontWeight.Bold) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogout()
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
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
                                text = currentTab.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, "Menú", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
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
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.Logout, "Salir", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Guinda4T)
                )
            },
            floatingActionButton = {
                val perfilIncompleto = state.perfil == null || (state.perfil?.estatura ?: 0.0) <= 0.0
                if (currentTab == DashboardTab.INICIO) {
                    FloatingActionButton(
                        onClick = {
                            if (perfilIncompleto) {
                                Toast.makeText(context, "Para desbloquear tu panel, necesitamos que completes tu Perfil Clínico.", Toast.LENGTH_LONG).show()
                            } else {
                                currentTab = DashboardTab.METRICAS
                            }
                        },
                        containerColor = GuindaOficial,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.AddChart, "Registrar Métricas")
                    }
                }
            },
            containerColor = Fondo4T
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (state.isLoading) {
                    DashboardShimmer(PaddingValues(16.dp))
                } else {
                    val perfilIncompleto = state.perfil == null || (state.perfil?.estatura ?: 0.0) <= 0.0
                    when (currentTab) {
                        DashboardTab.INICIO -> InicioTabContent(
                            userName = userName,
                            state = state,
                            onSugerenciaIAClick = {
                                if (perfilIncompleto) {
                                    Toast.makeText(context, "Para desbloquear tu panel, necesitamos que completes tu Perfil Clínico.", Toast.LENGTH_LONG).show()
                                } else {
                                    currentTab = DashboardTab.PREVENCION_IA
                                }
                            },
                            onMedicamentosClick = {
                                if (perfilIncompleto) {
                                    Toast.makeText(context, "Para desbloquear tu panel, necesitamos que completes tu Perfil Clínico.", Toast.LENGTH_LONG).show()
                                } else {
                                    currentTab = DashboardTab.MEDICAMENTOS
                                }
                            },
                            onMetasClick = {
                                if (perfilIncompleto) {
                                    Toast.makeText(context, "Para desbloquear tu panel, necesitamos que completes tu Perfil Clínico.", Toast.LENGTH_LONG).show()
                                } else {
                                    currentTab = DashboardTab.METAS
                                }
                            },
                            onReportesClick = {
                                if (perfilIncompleto) {
                                    Toast.makeText(context, "Para desbloquear tu panel, necesitamos que completes tu Perfil Clínico.", Toast.LENGTH_LONG).show()
                                } else {
                                    currentTab = DashboardTab.REPORTES
                                }
                            },
                            onDirectoryClick = {
                                if (perfilIncompleto) {
                                    Toast.makeText(context, "Para desbloquear tu panel, necesitamos que completes tu Perfil Clínico.", Toast.LENGTH_LONG).show()
                                } else {
                                    currentTab = DashboardTab.DIRECTORIO
                                }
                            },
                            onProfileClick = { currentTab = DashboardTab.PERFIL_CLINICO }
                        )
                        DashboardTab.METRICAS -> MetricasPage(
                            userId = userId
                        )
                        DashboardTab.TENDENCIAS -> TendenciasScreen(
                            userId = userId,
                            onNavigateBack = { currentTab = DashboardTab.INICIO },
                            onInicioClick = { currentTab = DashboardTab.INICIO },
                            onCitasClick = { currentTab = DashboardTab.CITAS },
                            onProfileClick = { currentTab = DashboardTab.PERFIL_CLINICO },
                            isEmbedded = true
                        )
                        DashboardTab.RECOMENDACIONES -> RecomendacionesTabContent(
                            recomendaciones = state.recomendaciones
                        )
                        DashboardTab.CITAS -> CitasScreen(
                            userId = userId,
                            userName = userName,
                            onNavigateBack = { currentTab = DashboardTab.INICIO },
                            isEmbedded = true
                        )
                        DashboardTab.PREVENCION_IA -> PrevencionScreen(
                            userId = userId,
                            onNavigateBack = { currentTab = DashboardTab.INICIO },
                            isEmbedded = true
                        )
                        DashboardTab.METAS -> MetasScreen(
                            userId = userId,
                            onNavigateBack = { currentTab = DashboardTab.INICIO },
                            isEmbedded = true
                        )
                        DashboardTab.MEDICAMENTOS -> MedicamentosScreen(
                            userId = userId,
                            onNavigateBack = { currentTab = DashboardTab.INICIO },
                            isEmbedded = true
                        )
                        DashboardTab.DIRECTORIO -> DirectorioMedicoScreen(
                            onNavigateBack = { currentTab = DashboardTab.INICIO },
                            isEmbedded = true
                        )
                        DashboardTab.REPORTES -> ReportesScreen(
                            userId = userId,
                            onNavigateBack = { currentTab = DashboardTab.INICIO },
                            isEmbedded = true
                        )
                        DashboardTab.PERFIL_CLINICO -> PerfilClinicoScreen(
                            userId = userId,
                            onNavigateBack = { currentTab = DashboardTab.INICIO },
                            isEmbedded = true,
                            onProfileSaved = {
                                viewModel.cargarDatosDashboard(userId)
                            }
                        )
                        DashboardTab.MI_FAMILIA -> FamiliaScreen(
                            userId = userId,
                            onNavigateBack = { currentTab = DashboardTab.INICIO },
                            isEmbedded = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InicioTabContent(
    userName: String,
    state: DashboardState,
    onSugerenciaIAClick: () -> Unit,
    onMedicamentosClick: () -> Unit,
    onMetasClick: () -> Unit,
    onReportesClick: () -> Unit,
    onDirectoryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val perfilIncompleto = state.perfil == null || (state.perfil?.estatura ?: 0.0) <= 0.0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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

        if (perfilIncompleto) {
            item {
                Card(
                    onClick = onProfileClick,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE67E22)), // Naranja para aviso
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Aviso", tint = Color.White)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Para desbloquear tu panel, necesitamos que completes tu Perfil Clínico.",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Toca aquí para completar tus datos clínicos.",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "Ir al perfil", tint = Color.White)
                    }
                }
            }
        }

        // Sugerencia IA Destacado
        item {
            state.sugerenciaIA?.let { sugerencia ->
                Card(
                    onClick = onSugerenciaIAClick,
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

        // Estado de Salud General
        item {
            Text(
                text = "Estado de Salud General",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Guinda4T,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val ultimoPeso = state.ultimoPeso?.valor ?: state.perfil?.pesoInicial ?: 0.0
            val estaturaCm = state.perfil?.estatura ?: 0.0
            val estaturaM = estaturaCm / 100.0
            val imcVal = if (ultimoPeso > 0.0 && estaturaM > 0.0) {
                ultimoPeso / (estaturaM * estaturaM)
            } else 0.0

            val imcText = if (imcVal > 0.0) {
                String.format(java.util.Locale.US, "%.1f", imcVal)
            } else if (ultimoPeso > 0.0) {
                String.format(java.util.Locale.US, "%.1f kg", ultimoPeso)
            } else {
                "--"
            }

            val imcCategory = when {
                imcVal <= 0.0 -> if (ultimoPeso > 0.0) "Registre estatura" else "Sin datos"
                imcVal < 18.5 -> "Bajo peso"
                imcVal < 25.0 -> "Normal"
                imcVal < 30.0 -> "Sobrepeso"
                else -> "Obesidad"
            }

            val imcStyle = when {
                imcVal <= 0.0 -> "muted"
                imcVal < 18.5 -> "warning"
                imcVal < 25.0 -> "normal"
                imcVal < 30.0 -> "warning"
                else -> "alert"
            }

            val ultimaPresion = state.ultimaPresion
            val ultimaFrecuencia = state.ultimaFrecuencia
            var showPresion = true
            if (ultimaPresion != null && ultimaFrecuencia != null) {
                showPresion = ultimaPresion.timestamp >= ultimaFrecuencia.timestamp
            } else if (ultimaPresion == null && ultimaFrecuencia != null) {
                showPresion = false
            }

            val cardioText: String
            val cardioCategory: String
            val cardioStyle: String

            if (ultimaPresion == null && ultimaFrecuencia == null) {
                cardioText = "--"
                cardioCategory = "Sin registro"
                cardioStyle = "muted"
            } else if (showPresion && ultimaPresion != null) {
                val sys = ultimaPresion.valor
                val dia = ultimaPresion.valorSecundario
                cardioText = "${sys.toInt()}/${dia.toInt()} mmHg"
                cardioCategory = when {
                    sys < 90 || dia < 60 -> "Hipotensión"
                    sys > 180 || dia > 120 -> "Crisis"
                    sys >= 140 || dia >= 90 -> "Hipertensión E2"
                    sys >= 130 || dia >= 80 -> "Hipertensión E1"
                    sys >= 120 -> "Elevada"
                    else -> "Normal"
                }
                cardioStyle = when {
                    sys < 90 || dia < 60 || sys > 180 || dia > 120 || sys >= 140 || dia >= 90 -> "alert"
                    sys >= 130 || dia >= 80 || sys >= 120 -> "warning"
                    else -> "normal"
                }
            } else {
                val fc = ultimaFrecuencia?.valor ?: 0.0
                cardioText = "${fc.toInt()} lpm"
                cardioCategory = when {
                    fc >= 130.0 -> "Taquicardia A"
                    fc > 100.0 -> "Taquicardia"
                    fc >= 60.0 -> "Normal"
                    fc >= 50.0 -> "Bradicardia"
                    else -> "Bradicardia B"
                }
                cardioStyle = when {
                    fc >= 130.0 || fc < 50.0 -> "alert"
                    fc > 100.0 || fc < 60.0 -> "warning"
                    else -> "normal"
                }
            }

            val ultimaGlucosa = state.ultimaGlucosa
            val glucoseText: String
            val glucoseCategory: String
            val glucoseStyle: String
            if (ultimaGlucosa != null) {
                val g = ultimaGlucosa.valor
                glucoseText = "${g.toInt()} mg/dL"
                glucoseCategory = when {
                    g > 200.0 -> "Crítico"
                    g > 125.0 -> "Elevada"
                    g >= 100.0 -> "Límite alto"
                    else -> "En objetivo"
                }
                glucoseStyle = when {
                    g > 200.0 -> "alert"
                    g > 125.0 || g >= 100.0 -> "warning"
                    else -> "normal"
                }
            } else {
                glucoseText = "--"
                glucoseCategory = "Sin registro"
                glucoseStyle = "muted"
            }

            val metaActiva = state.metaActiva
            val metaText: String
            val metaSub: String
            val metaProgressoVal: Float?
            val metaStyle: String
            if (metaActiva != null) {
                val p = metaActiva.progresoActual.coerceIn(0.0, 1.0)
                metaText = "${(p * 100).toInt()}%"
                metaSub = metaActiva.titulo ?: "Meta activa"
                metaProgressoVal = p.toFloat()
                metaStyle = when {
                    p >= 1.0 -> "normal"
                    p > 0.5 -> "normal"
                    else -> "warning"
                }
            } else {
                metaText = "0%"
                metaSub = "Sin metas activas"
                metaProgressoVal = null
                metaStyle = "muted"
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KPICard(
                        title = "Masa Corporal (IMC)",
                        value = imcText,
                        category = imcCategory,
                        style = imcStyle,
                        icon = Icons.Default.Scale,
                        modifier = Modifier.weight(1f)
                    )
                    KPICard(
                        title = "Cardiovascular",
                        value = cardioText,
                        category = cardioCategory,
                        style = cardioStyle,
                        icon = Icons.Default.Favorite,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KPICard(
                        title = "Estado Glucémico",
                        value = glucoseText,
                        category = glucoseCategory,
                        style = glucoseStyle,
                        icon = Icons.Default.Bloodtype,
                        modifier = Modifier.weight(1f)
                    )
                    KPICard(
                        title = "Progreso de Meta",
                        value = metaText,
                        category = metaSub,
                        style = metaStyle,
                        icon = Icons.Default.EmojiEvents,
                        progressVal = metaProgressoVal,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Estado Ambiental y de Salud Pública
        item {
            Text(
                text = "Estado Ambiental y Alertas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Guinda4T,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val clima = state.climaActual
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Dorado4T.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = DoradoOficial,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Condiciones Climáticas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = GuindaOficial
                            )
                            val climaStr = if (clima != null && clima.disponible) {
                                "${clima.ciudad}: ${clima.temperatura}°C, ${clima.condicion}"
                            } else {
                                "Buscando información climática o fuera de línea..."
                            }
                            Text(
                                text = climaStr,
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                            if (clima != null && !clima.calidadAire.isNullOrBlank()) {
                                Text(
                                    text = clima.calidadAire,
                                    fontSize = 10.sp,
                                    color = if (clima.calidadAireRiesgosa) Color.Red else Color.Gray,
                                    fontWeight = if (clima.calidadAireRiesgosa) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (state.alertasActivas.any { it.nivelRiesgo == "ALTA" }) Color(0xFFFDE8E8)
                                    else Dorado4T.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = if (state.alertasActivas.any { it.nivelRiesgo == "ALTA" }) Color.Red else DoradoOficial,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Alertas Epidemiológicas y Sanitarias",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = GuindaOficial
                            )
                            if (state.alertasActivas.isEmpty()) {
                                Text(
                                    text = "Situación regional y sanitaria controlada. Sin alertas vigentes.",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            } else {
                                state.alertasActivas.forEach { alerta ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "• ",
                                            fontWeight = FontWeight.Bold,
                                            color = if (alerta.nivelRiesgo == "ALTA") Color.Red else DoradoOficial
                                        )
                                        Text(
                                            text = alerta.descripcion,
                                            fontSize = 11.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Servicios y Herramientas
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

        // Recomendaciones
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

        // Logros
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

@Composable
fun RecomendacionesTabContent(
    recomendaciones: List<Recomendacion>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Recomendaciones Médicas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Guinda4T
            )
            Text(
                text = "Indicaciones oficiales y sugerencias de tratamiento de sus médicos",
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
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (recomendaciones.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tiene recomendaciones registradas por su médico tratante.", color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        } else {
            items(recomendaciones) { rec ->
                RecommendationItem(rec)
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
        modifier = Modifier
            .width(240.dp)
            .height(100.dp),
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

@Composable
fun KPICard(
    title: String,
    value: String,
    category: String,
    style: String,
    icon: ImageVector,
    progressVal: Float? = null,
    modifier: Modifier = Modifier
) {
    val chipBgColor = when (style) {
        "normal" -> Color(0xFFE2F0D9)
        "warning" -> Color(0xFFFFF3E0)
        "alert" -> Color(0xFFFDE8E8)
        else -> Color(0xFFF1F1F1)
    }
    val chipTextColor = when (style) {
        "normal" -> Color(0xFF1B5E20)
        "warning" -> Color(0xFFE65100)
        "alert" -> Color(0xFFC62828)
        else -> Color(0xFF616161)
    }

    Card(
        modifier = modifier.height(115.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = GuindaOficial, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            Column {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = GuindaOficial
                )
                
                if (progressVal != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progressVal },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = GuindaOficial,
                        trackColor = Dorado4T.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = category,
                        fontSize = 10.sp,
                        color = Color.DarkGray,
                        maxLines = 1
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(chipBgColor)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = chipTextColor
                        )
                    }
                }
            }
        }
    }
}
