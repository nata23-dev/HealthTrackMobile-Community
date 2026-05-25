package com.example.healthtrackmobile.ui.onboarding

import android.app.DatePickerDialog
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
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
import com.example.healthtrackmobile.model.PerfilPaciente
import com.example.healthtrackmobile.theme.Guinda4T
import com.example.healthtrackmobile.theme.Fondo4T
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionInicialScreen(
    userId: String,
    onFinished: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var fechaNac by rememberSaveable { mutableStateOf("") }
    var estatura by rememberSaveable { mutableStateOf("") }
    var peso by rememberSaveable { mutableStateOf("") }
    var grupoSanguineo by rememberSaveable { mutableStateOf("") }
    var alergias by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var grupoSanguineoExpanded by remember { mutableStateOf(false) }
    val gruposSanguineos = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val context = LocalContext.current

    val isValid = remember(fechaNac, estatura, peso, grupoSanguineo) {
        fechaNac.isNotEmpty() &&
                estatura.toDoubleOrNull() != null &&
                peso.toDoubleOrNull() != null &&
                grupoSanguineo.isNotEmpty()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración Inicial", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Guinda4T)
            )
        },
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
                "Datos Clínicos Base",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Guinda4T
            )
            Text(
                "Necesitamos esta información para activar el Motor IA y proteger su salud.",
                color = Color.Gray,
                fontSize = 14.sp
            )

            // Selector nativo de Fecha de Nacimiento
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = fechaNac,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha de Nacimiento (YYYY-MM-DD)") },
                    trailingIcon = { Icon(Icons.Default.CalendarToday, null) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Seleccione su fecha") }
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable {
                            val calendar = Calendar.getInstance()
                            val currentParts = fechaNac.split("-")
                            val cy = currentParts.getOrNull(0)?.toIntOrNull() ?: (calendar.get(Calendar.YEAR) - 30)
                            val cm = (currentParts.getOrNull(1)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
                            val cd = currentParts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)

                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    fechaNac = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                },
                                cy, cm, cd
                            ).show()
                        }
                )
            }

            OutlinedTextField(
                value = estatura,
                onValueChange = { estatura = it },
                label = { Text("Estatura (cm)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = peso,
                onValueChange = { peso = it },
                label = { Text("Peso Inicial (kg)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Selector nativo (Dropdown) de Grupo Sanguíneo
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = grupoSanguineo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Grupo Sanguíneo") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Seleccione su tipo de sangre") }
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { grupoSanguineoExpanded = true }
                )
            }
            DropdownMenu(
                expanded = grupoSanguineoExpanded,
                onDismissRequest = { grupoSanguineoExpanded = false },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                gruposSanguineos.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            grupoSanguineo = opcion
                            grupoSanguineoExpanded = false
                        }
                    )
                }
            }

            OutlinedTextField(
                value = alergias,
                onValueChange = { alergias = it },
                label = { Text("Alergias (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Guinda4T)
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        scope.launch {
                            try {
                                val db = FirebaseFirestore.getInstance()
                                val perfil = PerfilPaciente(
                                    id = userId,
                                    fechaNacimiento = fechaNac,
                                    estatura = estatura.toDoubleOrNull() ?: 0.0,
                                    grupoSanguineo = grupoSanguineo,
                                    alergias = alergias,
                                    pesoInicial = peso.toDoubleOrNull() ?: 0.0
                                )
                                db.collection("perfiles_pacientes").document(userId).set(perfil).await()

                                // Registrar primera métrica de peso para el cálculo de IMC y gráficos
                                val metricaPeso = com.example.healthtrackmobile.model.Metrica(
                                    pacienteId = userId,
                                    tipo = "PESO",
                                    valor = peso.toDoubleOrNull() ?: 0.0,
                                    timestamp = System.currentTimeMillis(),
                                    comentario = "Registro inicial durante configuración de cuenta"
                                )
                                db.collection("metricas").add(metricaPeso).await()

                                onFinished()
                            } catch (e: Exception) {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = isValid,
                    colors = ButtonDefaults.buttonColors(containerColor = Guinda4T)
                ) {
                    Text("ACTIVAR MI CUENTA", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
