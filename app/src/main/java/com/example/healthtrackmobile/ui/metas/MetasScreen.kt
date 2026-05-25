package com.example.healthtrackmobile.ui.metas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtrackmobile.model.Meta
import com.example.healthtrackmobile.theme.Guinda4T
import com.example.healthtrackmobile.theme.Fondo4T
import com.example.healthtrackmobile.theme.Dorado4T
import com.example.healthtrackmobile.theme.DoradoOficial
import com.example.healthtrackmobile.util.shimmerEffect
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetasScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    isEmbedded: Boolean = false,
    viewModel: MetasViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val context = LocalContext.current

    // Estados para control de diálogos
    var metaParaCompletar by remember { mutableStateOf<Meta?>(null) }
    var metaParaEliminar by remember { mutableStateOf<Meta?>(null) }

    LaunchedEffect(userId) {
        viewModel.cargarMetas(userId)
    }

    val activeMetas = remember(state.metas) {
        state.metas.filter { it.estado.uppercase().trim() == "ACTIVA" }
    }
    val completedMetas = remember(state.metas) {
        state.metas.filter { it.estado.uppercase().trim() == "CUMPLIDA" }
    }

    Scaffold(
        topBar = {
            if (!isEmbedded) {
                TopAppBar(
                    title = { Text("Mis Metas de Salud", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Guinda4T)
                )
            }
        },
        containerColor = Fondo4T,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Guinda4T,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Guinda4T
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("En Curso", fontWeight = FontWeight.Bold) },
                    selectedContentColor = Guinda4T,
                    unselectedContentColor = Color.Gray
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Historial Logros", fontWeight = FontWeight.Bold) },
                    selectedContentColor = Guinda4T,
                    unselectedContentColor = Color.Gray
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Nueva Meta", fontWeight = FontWeight.Bold) },
                    selectedContentColor = Guinda4T,
                    unselectedContentColor = Color.Gray
                )
            }

            if (state.isLoading) {
                MetasShimmer(PaddingValues(16.dp))
            } else {
                when (selectedTabIndex) {
                    0 -> MetasEnCursoTab(
                        activeMetas = activeMetas,
                        onCompleteClick = { metaParaCompletar = it },
                        onDeleteClick = { metaParaEliminar = it }
                    )
                    1 -> HistorialLogrosTab(
                        completedMetas = completedMetas
                    )
                    2 -> NuevaMetaTab(
                        userId = userId,
                        onSubmit = { meta ->
                            viewModel.crearMeta(meta) { success ->
                                if (success) {
                                    Toast.makeText(context, "Meta guardada con éxito", Toast.LENGTH_SHORT).show()
                                    selectedTabIndex = 0 // Regresar a la primera pestaña
                                } else {
                                    Toast.makeText(context, "Error al guardar meta", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // Diálogo de confirmación para Completar Meta
    metaParaCompletar?.let { meta ->
        AlertDialog(
            onDismissRequest = { metaParaCompletar = null },
            title = { Text("Completar Meta de Salud", fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas marcar la meta \"${meta.titulo}\" como completada?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.actualizarEstadoMeta(meta.id ?: "", "CUMPLIDA", userId)
                        metaParaCompletar = null
                        Toast.makeText(context, "Meta marcada como cumplida", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2E7D32))
                ) {
                    Text("Marcar Cumplida", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { metaParaCompletar = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }

    // Diálogo de confirmación para Eliminar Meta
    metaParaEliminar?.let { meta ->
        AlertDialog(
            onDismissRequest = { metaParaEliminar = null },
            title = { Text("Eliminar Meta", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas eliminar permanentemente la meta \"${meta.titulo}\"? esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarMeta(meta.id ?: "", userId)
                        metaParaEliminar = null
                        Toast.makeText(context, "Meta eliminada", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { metaParaEliminar = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun MetasEnCursoTab(
    activeMetas: List<Meta>,
    onCompleteClick: (Meta) -> Unit,
    onDeleteClick: (Meta) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabecera institucional
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "GOBIERNO DE MÉXICO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Guinda4T,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Expediente Digital de Metas de Salud",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Dorado4T)
                )
            }
        }

        item {
            Text(
                text = "Metas Activas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Guinda4T,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (activeMetas.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No tienes metas activas actualmente.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(activeMetas) { meta ->
                MetaCard(
                    meta = meta,
                    onCompleteClick = { onCompleteClick(meta) },
                    onDeleteClick = { onDeleteClick(meta) }
                )
            }
        }
    }
}

@Composable
fun HistorialLogrosTab(
    completedMetas: List<Meta>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabecera institucional
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "GOBIERNO DE MÉXICO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Guinda4T,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Historial de Logros y Metas Cumplidas",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Dorado4T)
                )
            }
        }

        item {
            Text(
                text = "Metas Cumplidas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Guinda4T,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (completedMetas.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Aún no has completado metas. ¡Sigue adelante!", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(completedMetas) { meta ->
                MetaCard(
                    meta = meta,
                    onCompleteClick = {},
                    onDeleteClick = {}
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaMetaTab(
    userId: String,
    onSubmit: (Meta) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var tipoMetrica by remember { mutableStateOf("PESO") }
    var objetivoNumerico by remember { mutableStateOf("") }
    var objetivoSecundario by remember { mutableStateOf("") }
    var valorInicial by remember { mutableStateOf("") }
    var valorInicialSecundario by remember { mutableStateOf("") }
    var prioridad by remember { mutableStateOf(1) } // 1 = Alta, 2 = Media, 3 = Baja
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    val tipos = listOf(
        "PESO" to "Peso",
        "GLUCOSA" to "Glucosa",
        "PRESION" to "Presión Arterial",
        "FRECUENCIA" to "Frecuencia Cardíaca"
    )
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabecera institucional
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "GOBIERNO DE MÉXICO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Guinda4T,
                letterSpacing = 1.sp
            )
            Text(
                text = "Registro de Nuevo Enfoque de Salud",
                fontSize = 13.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Dorado4T)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorMsg != null) {
                    Text(errorMsg ?: "", color = Color.Red, fontSize = 12.sp)
                }

                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título de la meta") },
                    placeholder = { Text("Ej: Bajar a mi peso ideal") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Guinda4T,
                        focusedLabelColor = Guinda4T
                    )
                )

                // Selector Tipo de Métrica
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = tipos.find { it.first == tipoMetrica }?.second ?: "Peso",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de métrica") },
                        trailingIcon = {
                            IconButton(onClick = { menuExpanded = !menuExpanded }) {
                                Icon(
                                    imageVector = if (menuExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = "Expandir"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { menuExpanded = !menuExpanded },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Guinda4T,
                            focusedLabelColor = Guinda4T
                        )
                    )
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        tipos.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.second) },
                                onClick = {
                                    tipoMetrica = item.first
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Campos dinámicos según tipo
                if (tipoMetrica == "PRESION") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = valorInicial,
                            onValueChange = { valorInicial = it },
                            label = { Text("Sistólica Inicial") },
                            placeholder = { Text("135") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Guinda4T,
                                focusedLabelColor = Guinda4T
                            )
                        )
                        OutlinedTextField(
                            value = valorInicialSecundario,
                            onValueChange = { valorInicialSecundario = it },
                            label = { Text("Diastólica Inicial") },
                            placeholder = { Text("85") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Guinda4T,
                                focusedLabelColor = Guinda4T
                            )
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = objetivoNumerico,
                            onValueChange = { objetivoNumerico = it },
                            label = { Text("Sistólica Objetivo") },
                            placeholder = { Text("120") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Guinda4T,
                                focusedLabelColor = Guinda4T
                            )
                        )
                        OutlinedTextField(
                            value = objetivoSecundario,
                            onValueChange = { objetivoSecundario = it },
                            label = { Text("Diastólica Objetivo") },
                            placeholder = { Text("80") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Guinda4T,
                                focusedLabelColor = Guinda4T
                            )
                        )
                    }
                } else {
                    val labelIni = when(tipoMetrica) {
                        "PESO" -> "Peso Inicial (kg)"
                        "GLUCOSA" -> "Glucosa Inicial (mg/dL)"
                        else -> "Ritmo Inicial (lpm)"
                    }
                    val labelObj = when(tipoMetrica) {
                        "PESO" -> "Peso Objetivo (kg)"
                        "GLUCOSA" -> "Glucosa Objetivo (mg/dL)"
                        else -> "Ritmo Objetivo (lpm)"
                    }
                    OutlinedTextField(
                        value = valorInicial,
                        onValueChange = { valorInicial = it },
                        label = { Text(labelIni) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Guinda4T,
                            focusedLabelColor = Guinda4T
                        )
                    )
                    OutlinedTextField(
                        value = objetivoNumerico,
                        onValueChange = { objetivoNumerico = it },
                        label = { Text(labelObj) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Guinda4T,
                            focusedLabelColor = Guinda4T
                        )
                    )
                }

                // Selector Prioridad
                Text("Prioridad / Enfoque", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(1 to "Alta", 2 to "Media", 3 to "Baja").forEach { p ->
                        val selected = prioridad == p.first
                        Button(
                            onClick = { prioridad = p.first },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) Guinda4T else Color.LightGray.copy(alpha = 0.3f),
                                contentColor = if (selected) Color.White else Color.DarkGray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(p.second, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                if (titulo.isBlank()) {
                    errorMsg = "Por favor ingresa un título descriptivo"
                    return@Button
                }
                val valIni = valorInicial.toDoubleOrNull()
                val valObj = objetivoNumerico.toDoubleOrNull()
                if (valIni == null || valObj == null) {
                    errorMsg = "Por favor ingresa valores iniciales y objetivos válidos"
                    return@Button
                }

                val valIniSec = if (tipoMetrica == "PRESION") valorInicialSecundario.toDoubleOrNull() else 0.0
                val valObjSec = if (tipoMetrica == "PRESION") objetivoSecundario.toDoubleOrNull() else 0.0
                if (tipoMetrica == "PRESION" && (valIniSec == null || valObjSec == null)) {
                    errorMsg = "Por favor ingresa valores de presión arterial secundarios válidos"
                    return@Button
                }

                val unidad = when(tipoMetrica) {
                    "PESO" -> "kg"
                    "GLUCOSA" -> "mg/dL"
                    "PRESION" -> "mmHg"
                    else -> "lpm"
                }

                val meta = Meta(
                    pacienteId = userId,
                    titulo = titulo,
                    tipoMetrica = tipoMetrica,
                    valorInicial = valIni,
                    valorActual = valIni,
                    objetivoNumerico = valObj,
                    valorInicialSecundario = valIniSec ?: 0.0,
                    valorActualSecundario = valIniSec ?: 0.0,
                    objetivoSecundario = valObjSec ?: 0.0,
                    progresoActual = 0.0,
                    estado = "ACTIVA",
                    prioridad = prioridad,
                    unidad = unidad,
                    fechaCumplimiento = System.currentTimeMillis()
                )
                onSubmit(meta)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Guinda4T),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Save, null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("GUARDAR META", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun MetaCard(
    meta: Meta,
    onCompleteClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isCumplida = meta.estado.uppercase().trim() == "CUMPLIDA"
    val progress = remember(meta.valorInicial, meta.valorActual, meta.objetivoNumerico, isCumplida) {
        if (isCumplida) 1f
        else {
            val vIni = meta.valorInicial
            val vAct = meta.valorActual
            val vObj = meta.objetivoNumerico
            
            val totalRange = vObj - vIni
            if (totalRange == 0.0) 0f
            else {
                val progressDone = vAct - vIni
                (progressDone / totalRange).coerceIn(0.0, 1.0).toFloat()
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = meta.titulo ?: "Meta de Salud",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Guinda4T
            )
            Spacer(Modifier.height(12.dp))
            
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Progreso: ${(progress * 100).toInt()}%", fontSize = 14.sp, color = Color.Gray)
                Text("${meta.valorActual} / ${meta.objetivoNumerico} ${meta.unidad ?: ""}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = Guinda4T,
                trackColor = Dorado4T.copy(alpha = 0.3f)
            )
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isCumplida || progress >= 1f) "¡Meta Alcanzada!" else "Sigue así, falta poco para tu objetivo.",
                    fontSize = 12.sp,
                    color = if (isCumplida || progress >= 1f) Color(0xFF1B5E20) else Color.Gray,
                    fontWeight = if (isCumplida || progress >= 1f) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isCumplida) {
                        IconButton(
                            onClick = onCompleteClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completar Meta",
                                tint = Color(0xFF2E7D32)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar Meta",
                            tint = Color(0xFFC62828)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetasShimmer(padding: PaddingValues) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(4) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shimmerEffect()
            )
        }
    }
}
