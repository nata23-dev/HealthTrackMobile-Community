package com.example.healthtrackmobile.ui.metricas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtrackmobile.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarMetricaScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AgregarMetricaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val tipos = listOf(
        Triple("GLUCOSA", "Glucosa", Icons.Default.Bloodtype),
        Triple("PRESION", "Presión Arterial", Icons.Default.Speed),
        Triple("FRECUENCIA_CARDIACA", "Ritmo Cardíaco", Icons.Default.Favorite),
        Triple("PESO", "Peso Corporal", Icons.Default.Scale)
    )

    var tipoSeleccionado by remember { mutableStateOf("GLUCOSA") }
    var valor by remember { mutableStateOf("") }
    var valorSecundario by remember { mutableStateOf("") }
    var comentario by remember { mutableStateOf("") }

    // Resetear campos al cambiar tipo
    LaunchedEffect(tipoSeleccionado) {
        valor = ""
        valorSecundario = ""
        viewModel.clearError()
    }

    // Regresar al dashboard si se guardó con éxito
    LaunchedEffect(uiState) {
        if (uiState is AgregarMetricaUiState.Success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Registrar Medición",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Institucional
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "GOBIERNO DE MÉXICO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Guinda4T,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Estrategia Nacional de Monitoreo Crónico",
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

            // Selector de Tipo de Métrica (Chips Horizontales)
            Text(
                text = "Selecciona el Tipo de Medición",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Guinda4T
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tipos.forEach { (tipoCode, tipoLabel, icon) ->
                    val isSelected = tipoSeleccionado == tipoCode
                    val chipBgColor = if (isSelected) Guinda4T else Color.White
                    val chipTextColor = if (isSelected) Color.White else Color.DarkGray
                    val chipBorderColor = if (isSelected) Guinda4T else Color.LightGray

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(chipBgColor)
                            .clickable { tipoSeleccionado = tipoCode }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = tipoLabel,
                                tint = chipTextColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tipoLabel.split(" ")[0], // Cortar label largo para que quepa
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = chipTextColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Formulario de valores
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (tipoSeleccionado == "PRESION") {
                        // Presión sistólica
                        OutlinedTextField(
                            value = valor,
                            onValueChange = { 
                                valor = it
                                viewModel.clearError()
                            },
                            label = { Text("Presión Sistólica (Alta, ej: 120)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Guinda4T,
                                focusedLabelColor = Guinda4T,
                                cursorColor = Guinda4T
                            )
                        )

                        // Presión diastólica
                        OutlinedTextField(
                            value = valorSecundario,
                            onValueChange = { 
                                valorSecundario = it
                                viewModel.clearError()
                            },
                            label = { Text("Presión Diastólica (Baja, ej: 80)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Guinda4T,
                                focusedLabelColor = Guinda4T,
                                cursorColor = Guinda4T
                            )
                        )
                    } else {
                        // Campo único para Glucosa, Ritmo cardíaco y Peso
                        val labelText = when (tipoSeleccionado) {
                            "GLUCOSA" -> "Nivel de Glucosa (mg/dL)"
                            "FRECUENCIA_CARDIACA" -> "Ritmo Cardíaco (lpm)"
                            else -> "Peso Corporal (kg)"
                        }
                        val placeholderText = when (tipoSeleccionado) {
                            "GLUCOSA" -> "ej: 95"
                            "FRECUENCIA_CARDIACA" -> "ej: 72"
                            else -> "ej: 74.5"
                        }

                        OutlinedTextField(
                            value = valor,
                            onValueChange = { 
                                valor = it
                                viewModel.clearError()
                            },
                            label = { Text(labelText) },
                            placeholder = { Text(placeholderText) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Guinda4T,
                                focusedLabelColor = Guinda4T,
                                cursorColor = Guinda4T
                            )
                        )
                    }

                    // Campo de Comentarios
                    OutlinedTextField(
                        value = comentario,
                        onValueChange = { comentario = it },
                        label = { Text("Comentarios u Observaciones") },
                        placeholder = { Text("ej: Medido en ayunas / Después de comer") },
                        minLines = 3,
                        maxLines = 5,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Guinda4T,
                            focusedLabelColor = Guinda4T,
                            cursorColor = Guinda4T
                        )
                    )
                }
            }

            // Mostrar Errores
            if (uiState is AgregarMetricaUiState.Error) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (uiState as AgregarMetricaUiState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Botón Guardar
            Button(
                onClick = { 
                    viewModel.guardarMetrica(
                        userId = userId,
                        tipo = tipoSeleccionado,
                        valorStr = valor,
                        valorSecundarioStr = valorSecundario,
                        comentario = comentario
                    )
                },
                enabled = uiState !is AgregarMetricaUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Guinda4T,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (uiState is AgregarMetricaUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Guardar",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guardar Registro",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
