package com.example.healthtrackmobile.ui.metricas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtrackmobile.model.Metrica
import com.example.healthtrackmobile.theme.*
import com.example.healthtrackmobile.util.NetworkUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricasPage(
    userId: String,
    modifier: Modifier = Modifier,
    viewModel: AgregarMetricaViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableStateOf(0) }
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val metricas by viewModel.metricas.collectAsStateWithLifecycle()
    val isLoadingHistorial by viewModel.isLoadingHistorial.collectAsStateWithLifecycle()

    // Cargar historial de métricas
    LaunchedEffect(userId) {
        viewModel.cargarMetricas(userId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Fondo4T)
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
                text = { Text("Registrar Medición", fontWeight = FontWeight.Bold) },
                selectedContentColor = Guinda4T,
                unselectedContentColor = Color.Gray
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Historial Clínico", fontWeight = FontWeight.Bold) },
                selectedContentColor = Guinda4T,
                unselectedContentColor = Color.Gray
            )
        }

        when (selectedTabIndex) {
            0 -> RegistrarMedicionTab(
                userId = userId,
                isSaving = isSaving,
                error = error,
                onSaveSuccess = {
                    selectedTabIndex = 1 // Ir al historial
                },
                viewModel = viewModel
            )
            1 -> HistorialClinicoTab(
                metricas = metricas,
                isLoading = isLoadingHistorial,
                userId = userId,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun RegistrarMedicionTab(
    userId: String,
    isSaving: Boolean,
    error: String?,
    onSaveSuccess: () -> Unit,
    viewModel: AgregarMetricaViewModel
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var glucosa by rememberSaveable { mutableStateOf("") }
    var sistolica by rememberSaveable { mutableStateOf("") }
    var diastolica by rememberSaveable { mutableStateOf("") }
    var ritmo by rememberSaveable { mutableStateOf("") }
    var peso by rememberSaveable { mutableStateOf("") }
    var comentario by rememberSaveable { mutableStateOf("") }

    val glucosaError = remember(glucosa) {
        val v = glucosa.toDoubleOrNull()
        if (v == null && glucosa.isNotEmpty()) "Valor inválido"
        else if (v != null && v !in 20.0..600.0) "Rango fuera de lo común"
        else null
    }
    
    val sistolicaError = remember(sistolica) {
        val v = sistolica.toDoubleOrNull()
        if (v == null && sistolica.isNotEmpty()) "Valor inválido"
        else if (v != null && v !in 40.0..250.0) "Valor fuera de rango"
        else null
    }

    val diastolicaError = remember(diastolica) {
        val v = diastolica.toDoubleOrNull()
        if (v == null && diastolica.isNotEmpty()) "Valor inválido"
        else if (v != null && v !in 30.0..150.0) "Valor fuera de rango"
        else null
    }

    val ritmoError = remember(ritmo) {
        val v = ritmo.toDoubleOrNull()
        if (v == null && ritmo.isNotEmpty()) "Valor inválido"
        else if (v != null && v !in 30.0..220.0) "Valor fuera de rango"
        else null
    }

    val pesoError = remember(peso) {
        val v = peso.toDoubleOrNull()
        if (v == null && peso.isNotEmpty()) "Valor inválido"
        else if (v != null && v !in 2.0..300.0) "Peso fuera de rango"
        else null
    }

    val canSave = remember(glucosa, sistolica, diastolica, ritmo, peso, glucosaError, sistolicaError, diastolicaError, ritmoError, pesoError) {
        val anyInput = glucosa.isNotEmpty() || (sistolica.isNotEmpty() && diastolica.isNotEmpty()) || ritmo.isNotEmpty() || peso.isNotEmpty()
        val noErrors = glucosaError == null && sistolicaError == null && diastolicaError == null && ritmoError == null && pesoError == null
        anyInput && noErrors
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "CAPTURA DE SIGNOS VITALES",
            fontWeight = FontWeight.Black,
            color = Guinda4T,
            fontSize = 16.sp,
            letterSpacing = 1.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Glucosa
                OutlinedTextField(
                    value = glucosa,
                    onValueChange = { if (it.length <= 5) glucosa = it },
                    label = { Text("Glucosa (mg/dL)") },
                    placeholder = { Text("Ej: 95") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = glucosaError != null,
                    supportingText = { glucosaError?.let { Text(it) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Guinda4T,
                        focusedLabelColor = Guinda4T
                    )
                )

                // Presión Arterial
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = sistolica,
                        onValueChange = { if (it.length <= 3) sistolica = it },
                        label = { Text("Sistólica (mmHg)") },
                        placeholder = { Text("Ej: 120") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = sistolicaError != null,
                        supportingText = { sistolicaError?.let { Text(it) } },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Guinda4T,
                            focusedLabelColor = Guinda4T
                        )
                    )
                    OutlinedTextField(
                        value = diastolica,
                        onValueChange = { if (it.length <= 3) diastolica = it },
                        label = { Text("Diastólica (mmHg)") },
                        placeholder = { Text("Ej: 80") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = diastolicaError != null,
                        supportingText = { diastolicaError?.let { Text(it) } },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Guinda4T,
                            focusedLabelColor = Guinda4T
                        )
                    )
                }

                // Ritmo Cardiaco
                OutlinedTextField(
                    value = ritmo,
                    onValueChange = { if (it.length <= 3) ritmo = it },
                    label = { Text("Ritmo Cardíaco (lpm)") },
                    placeholder = { Text("Ej: 72") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = ritmoError != null,
                    supportingText = { ritmoError?.let { Text(it) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Guinda4T,
                        focusedLabelColor = Guinda4T
                    )
                )

                // Peso
                OutlinedTextField(
                    value = peso,
                    onValueChange = { if (it.length <= 5) peso = it },
                    label = { Text("Peso Corporal (kg)") },
                    placeholder = { Text("Ej: 74.5") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = pesoError != null,
                    supportingText = { pesoError?.let { Text(it) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Guinda4T,
                        focusedLabelColor = Guinda4T
                    )
                )

                // Comentario
                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = { Text("Comentario u Observaciones") },
                    placeholder = { Text("Ej: Ayuno de 8 horas, después de comer, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Guinda4T,
                        focusedLabelColor = Guinda4T
                    )
                )
            }
        }

        if (error != null) {
            Text(
                text = error ?: "",
                color = Color.Red,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isSaving) {
            CircularProgressIndicator(color = Guinda4T)
        } else {
            Button(
                onClick = {
                    val list = mutableListOf<Metrica>()
                    val commentVal = comentario.trim().ifEmpty { "Registro clínico móvil" }
                    
                    if (glucosa.isNotEmpty()) {
                        list.add(Metrica(tipo = "GLUCOSA", valor = glucosa.toDoubleOrNull() ?: 0.0, comentario = commentVal))
                    }
                    if (sistolica.isNotEmpty() && diastolica.isNotEmpty()) {
                        list.add(Metrica(tipo = "PRESION", valor = sistolica.toDoubleOrNull() ?: 0.0, valorSecundario = diastolica.toDoubleOrNull() ?: 0.0, comentario = commentVal))
                    }
                    if (ritmo.isNotEmpty()) {
                        list.add(Metrica(tipo = "FRECUENCIA_CARDIACA", valor = ritmo.toDoubleOrNull() ?: 0.0, comentario = commentVal))
                    }
                    if (peso.isNotEmpty()) {
                        list.add(Metrica(tipo = "PESO", valor = peso.toDoubleOrNull() ?: 0.0, comentario = commentVal))
                    }
                    
                    val isOnline = NetworkUtils.isOnline(context)
                    val message = if (isOnline) "Métricas registradas correctamente" else "Guardado localmente. Se sincronizará automáticamente"
                    
                    viewModel.guardarMetricas(userId, list, onSuccess = {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        // Limpiar formulario
                        glucosa = ""
                        sistolica = ""
                        diastolica = ""
                        ritmo = ""
                        peso = ""
                        comentario = ""
                        onSaveSuccess()
                    })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(containerColor = Guinda4T),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Save, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("REGISTRAR MEDICIONES", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun HistorialClinicoTab(
    metricas: List<Metrica>,
    isLoading: Boolean,
    userId: String,
    viewModel: AgregarMetricaViewModel
) {
    LaunchedEffect(userId) {
        viewModel.cargarMetricas(userId)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Guinda4T)
        }
    } else if (metricas.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "No hay mediciones registradas en el historial.",
                    color = Color.Gray,
                    modifier = Modifier.padding(24.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(metricas) { metrica ->
                val level = evaluarNivelAlerta(metrica)
                val leftBorderColor = when (level) {
                    "CRITICO" -> Color(0xFFC62828) // Rojo crítico
                    "ADVERTENCIA" -> Color(0xFFE67E22) // Naranja advertencia
                    else -> Color.Transparent // Normal
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Indicador de nivel de alerta en la izquierda de la tarjeta
                        if (leftBorderColor != Color.Transparent) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .fillMaxHeight()
                                    .align(Alignment.CenterVertically)
                                    .background(leftBorderColor)
                                    .height(72.dp)
                            )
                        } else {
                            // Franja verde salud para normal
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .fillMaxHeight()
                                    .align(Alignment.CenterVertically)
                                    .background(Color(0xFFE0E0E0))
                                    .height(72.dp)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            color = when (metrica.tipo?.uppercase()?.trim()) {
                                                "GLUCOSA" -> GuindaOficial.copy(alpha = 0.1f)
                                                "PRESION" -> DoradoOficial.copy(alpha = 0.2f)
                                                "FRECUENCIA_CARDIACA" -> Color(0xFFFDE8E8)
                                                else -> Color(0xFFE2F0D9)
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (metrica.tipo?.uppercase()?.trim()) {
                                            "GLUCOSA" -> Icons.Default.Bloodtype
                                            "PRESION" -> Icons.Default.Speed
                                            "FRECUENCIA_CARDIACA" -> Icons.Default.Favorite
                                            else -> Icons.Default.Scale
                                        },
                                        contentDescription = null,
                                        tint = when (metrica.tipo?.uppercase()?.trim()) {
                                            "GLUCOSA" -> GuindaOficial
                                            "PRESION" -> DoradoOficial
                                            "FRECUENCIA_CARDIACA" -> Color(0xFFC62828)
                                            else -> Color(0xFF2E7D32)
                                        },
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    val readableName = when (metrica.tipo?.uppercase()?.trim()) {
                                        "GLUCOSA" -> "Glucosa"
                                        "PRESION" -> "Presión Arterial"
                                        "FRECUENCIA_CARDIACA" -> "Frecuencia Cardíaca"
                                        "PESO" -> "Peso"
                                        else -> metrica.tipo ?: "Signo vital"
                                    }
                                    Text(
                                        text = readableName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Guinda4T
                                    )
                                    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(metrica.timestamp))
                                    Text(dateStr, fontSize = 11.sp, color = Color.Gray)
                                    if (!metrica.comentario.isNullOrBlank() && !metrica.comentario!!.startsWith("goal-update:")) {
                                        Text(metrica.comentario ?: "", fontSize = 11.sp, color = Color.DarkGray, maxLines = 1)
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                val valueText = when (metrica.tipo?.uppercase()?.trim()) {
                                    "GLUCOSA" -> "${metrica.valor.toInt()} mg/dL"
                                    "PRESION" -> "${metrica.valor.toInt()}/${metrica.valorSecundario.toInt()} mmHg"
                                    "FRECUENCIA_CARDIACA" -> "${metrica.valor.toInt()} lpm"
                                    "PESO" -> "${metrica.valor} kg"
                                    else -> "${metrica.valor}"
                                }
                                Text(
                                    text = valueText,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = GuindaOficial
                                )
                                
                                if (level.isNotEmpty()) {
                                    Text(
                                        text = if (level == "CRITICO") "Crítico" else "Advertencia",
                                        color = leftBorderColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun evaluarNivelAlerta(m: Metrica): String {
    val tipo = m.tipo?.uppercase()?.trim() ?: return ""
    return when (tipo) {
        "GLUCOSA", "GLUCOSE" -> {
            if (m.valor > 125) "CRITICO" else ""
        }
        "PRESION", "PRESSURE", "TENSIÓN", "PRESION_ARTERIAL" -> {
            if (m.valor >= 140 || m.valorSecundario >= 90) "CRITICO"
            else if (m.valor >= 130 || m.valorSecundario >= 80) "ADVERTENCIA"
            else ""
        }
        "FRECUENCIA", "HEART_RATE", "RITMO", "FRECUENCIA_CARDIACA" -> {
            if (m.valor > 120) "CRITICO"
            else if (m.valor > 100) "ADVERTENCIA"
            else ""
        }
        else -> ""
    }
}
