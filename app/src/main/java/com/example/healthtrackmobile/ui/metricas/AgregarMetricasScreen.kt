package com.example.healthtrackmobile.ui.metricas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtrackmobile.model.Metrica
import com.example.healthtrackmobile.theme.Guinda4T
import com.example.healthtrackmobile.theme.Fondo4T
import com.example.healthtrackmobile.util.NetworkUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarMetricasScreen(
    userId: String,
    onNavigateBack: () -> Unit
) {
    val viewModel: AgregarMetricaViewModel = viewModel()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var glucosa by rememberSaveable { mutableStateOf("") }
    var sistolica by rememberSaveable { mutableStateOf("") }
    var diastolica by rememberSaveable { mutableStateOf("") }
    var ritmo by rememberSaveable { mutableStateOf("") }
    var peso by rememberSaveable { mutableStateOf("") }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Métricas de Salud", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Guinda4T)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Fondo4T
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Complete los datos de monitoreo",
                fontWeight = FontWeight.Bold,
                color = Guinda4T,
                fontSize = 18.sp
            )

            OutlinedTextField(
                value = glucosa,
                onValueChange = { if (it.length <= 5) glucosa = it },
                label = { Text("Glucosa (mg/dL)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = glucosaError != null,
                supportingText = { glucosaError?.let { Text(it) } }
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = sistolica,
                    onValueChange = { if (it.length <= 3) sistolica = it },
                    label = { Text("Sistólica (mmHg)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = sistolicaError != null,
                    supportingText = { sistolicaError?.let { Text(it) } }
                )
                OutlinedTextField(
                    value = diastolica,
                    onValueChange = { if (it.length <= 3) diastolica = it },
                    label = { Text("Diastólica (mmHg)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = diastolicaError != null,
                    supportingText = { diastolicaError?.let { Text(it) } }
                )
            }

            OutlinedTextField(
                value = ritmo,
                onValueChange = { if (it.length <= 3) ritmo = it },
                label = { Text("Ritmo Cardíaco (lpm)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = ritmoError != null,
                supportingText = { ritmoError?.let { Text(it) } }
            )

            OutlinedTextField(
                value = peso,
                onValueChange = { if (it.length <= 5) peso = it },
                label = { Text("Peso Corporal (kg)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = pesoError != null,
                supportingText = { pesoError?.let { Text(it) } }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isSaving) {
                CircularProgressIndicator(color = Guinda4T)
            } else {
                Button(
                    onClick = {
                        val list = mutableListOf<Metrica>()
                        if (glucosa.isNotEmpty()) {
                            list.add(Metrica(tipo = "GLUCOSA", valor = glucosa.toDoubleOrNull() ?: 0.0))
                        }
                        if (sistolica.isNotEmpty() && diastolica.isNotEmpty()) {
                            list.add(Metrica(tipo = "PRESION", valor = sistolica.toDoubleOrNull() ?: 0.0, valorSecundario = diastolica.toDoubleOrNull() ?: 0.0))
                        }
                        if (ritmo.isNotEmpty()) {
                            list.add(Metrica(tipo = "FRECUENCIA", valor = ritmo.toDoubleOrNull() ?: 0.0))
                        }
                        if (peso.isNotEmpty()) {
                            list.add(Metrica(tipo = "PESO", valor = peso.toDoubleOrNull() ?: 0.0))
                        }
                        
                        val isOnline = NetworkUtils.isOnline(context)
                        val message = if (isOnline) "Métricas registradas correctamente" else "Guardado localmente. Se sincronizará automáticamente"
                        
                        viewModel.guardarMetricas(userId, list, onSuccess = {
                            scope.launch {
                                snackbarHostState.showSnackbar(message)
                                delay(1000)
                                onNavigateBack()
                            }
                        })
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(containerColor = Guinda4T)
                ) {
                    Text("GUARDAR MÉTRICAS", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
