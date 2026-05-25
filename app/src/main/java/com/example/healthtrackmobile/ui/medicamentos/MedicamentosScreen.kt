package com.example.healthtrackmobile.ui.medicamentos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.os.Build
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.compose.ui.platform.LocalContext
import com.example.healthtrackmobile.model.RecordatorioMedicamento
import com.example.healthtrackmobile.theme.*

import com.example.healthtrackmobile.util.WhatsAppShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicamentosScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    isEmbedded: Boolean = false,
    viewModel: MedicamentosViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var reminderToDelete by remember { mutableStateOf<RecordatorioMedicamento?>(null) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showDialog = true
        }
    }

    LaunchedEffect(userId) {
        viewModel.cargarRecordatorios(userId)
    }

    Scaffold(
        topBar = {
            if (!isEmbedded) {
                TopAppBar(
                    title = { Text("Mis Medicamentos", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Guinda4T)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        showDialog = true
                    }
                },
                containerColor = Guinda4T,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Nuevo Recordatorio")
            }
        },
        containerColor = Fondo4T
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Dorado4T.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Medication, null, tint = Guinda4T, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Gestión de Tratamientos", fontWeight = FontWeight.Bold, color = Guinda4T)
                        Text("Mantén el control de tus tomas diarias", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Guinda4T)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.recordatorios) { recordatorio ->
                        MedicamentoItem(
                            recordatorio = recordatorio,
                            onAvisarToma = {
                                val mensaje = "Hola, acabo de registrar la toma de mi medicamento: ${recordatorio.medicamento} (${recordatorio.dosis})"
                                WhatsAppShareUtils.compartirPorWhatsApp(context, mensaje)
                            },
                            onDelete = {
                                reminderToDelete = recordatorio
                            }
                        )
                    }
                }
            }
        }
    }

    reminderToDelete?.let { reminder ->
        AlertDialog(
            onDismissRequest = { reminderToDelete = null },
            title = { Text("Cancelar Recordatorio", color = Guinda4T, fontWeight = FontWeight.Bold) },
            text = { Text("¿Está seguro de que desea suspender y eliminar el recordatorio de ${reminder.medicamento}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarRecordatorio(context, reminder.id, userId)
                        reminderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Guinda4T)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { reminderToDelete = null }) {
                    Text("Cancelar", color = Guinda4T)
                }
            }
        )
    }

    if (showDialog) {
        NuevoRecordatorioDialog(
            onDismiss = { showDialog = false },
            onSave = { medicamento, dosis, frecuencia, hora, fechaInicio, fechaFin ->
                viewModel.agregarRecordatorio(
                    context = context,
                    recordatorio = RecordatorioMedicamento(
                        pacienteId = userId,
                        medicamento = medicamento,
                        dosis = dosis,
                        frecuencia = frecuencia,
                        horaRecordatorio = hora,
                        fechaInicio = fechaInicio,
                        fechaFin = fechaFin,
                        estado = "activo"
                    )
                )
                showDialog = false
            }
        )
    }
}

@Composable
fun MedicamentoItem(
    recordatorio: RecordatorioMedicamento,
    onAvisarToma: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Guinda4T.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Vaccines, null, tint = Guinda4T)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(recordatorio.medicamento ?: "Medicamento", fontWeight = FontWeight.Bold, color = Guinda4T)
                Text("Dosis: ${recordatorio.dosis ?: "N/A"}", fontSize = 13.sp, color = Color.Gray)
                if (!recordatorio.frecuencia.isNullOrEmpty()) {
                    Text("Frecuencia: ${recordatorio.frecuencia}", fontSize = 12.sp, color = Color.DarkGray)
                }
                if (!recordatorio.fechaFin.isNullOrEmpty()) {
                    Text("Fin: ${recordatorio.fechaFin}", fontSize = 11.sp, color = Color.Gray)
                }
                
                Spacer(Modifier.height(4.dp))
                
                TextButton(
                    onClick = onAvisarToma,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp), tint = Dorado4T)
                    Spacer(Modifier.width(4.dp))
                    Text("AVISAR TOMA", fontSize = 11.sp, color = Dorado4T, fontWeight = FontWeight.Bold)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, tint = Dorado4T, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(recordatorio.horaRecordatorio ?: "--:--", fontWeight = FontWeight.Bold, color = Guinda4T)
                }
                Spacer(Modifier.height(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar recordatorio",
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoRecordatorioDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var dosis by remember { mutableStateOf("") }
    var frecuencia by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var fechaInicio by remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        mutableStateOf(sdf.format(Date()))
    }
    var fechaFin by remember { mutableStateOf("") }

    var frecuenciaExpanded by remember { mutableStateOf(false) }
    val frecuencias = listOf("Cada 4 horas", "Cada 6 horas", "Cada 8 horas", "Cada 12 horas", "Cada 24 horas", "Una vez al día")
    val context = LocalContext.current

    val isValid = remember(nombre, dosis, frecuencia, hora, fechaInicio, fechaFin) {
        nombre.isNotBlank() && dosis.isNotBlank() && frecuencia.isNotBlank() && hora.isNotBlank() && fechaInicio.isNotBlank() && fechaFin.isNotBlank()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Recordatorio", color = Guinda4T, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del Medicamento") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dosis,
                    onValueChange = { dosis = it },
                    label = { Text("Dosis (ej. 1 tableta)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Frecuencia Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = frecuencia,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frecuencia") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { frecuenciaExpanded = true }
                    )
                }
                DropdownMenu(
                    expanded = frecuenciaExpanded,
                    onDismissRequest = { frecuenciaExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    frecuencias.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                frecuencia = opcion
                                frecuenciaExpanded = false
                            }
                        )
                    }
                }

                // Hora TimePicker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = hora,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hora (HH:mm)") },
                        trailingIcon = { Icon(Icons.Default.AccessTime, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                val calendar = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, selectedHour, selectedMinute ->
                                        hora = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true
                                ).show()
                            }
                    )
                }

                // Fecha Inicio DatePicker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = fechaInicio,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha de Inicio") },
                        trailingIcon = { Icon(Icons.Default.CalendarToday, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                val calendar = Calendar.getInstance()
                                val currentParts = fechaInicio.split("-")
                                val cy = currentParts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
                                val cm = (currentParts.getOrNull(1)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
                                val cd = currentParts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)

                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        fechaInicio = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                    },
                                    cy, cm, cd
                                ).show()
                            }
                    )
                }

                // Fecha Fin DatePicker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = fechaFin,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha de Finalización") },
                        trailingIcon = { Icon(Icons.Default.CalendarToday, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                val calendar = Calendar.getInstance()
                                val cy = calendar.get(Calendar.YEAR)
                                val cm = calendar.get(Calendar.MONTH)
                                val cd = calendar.get(Calendar.DAY_OF_MONTH)

                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        fechaFin = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                    },
                                    cy, cm, cd
                                ).show()
                            }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (isValid) onSave(nombre, dosis, frecuencia, hora, fechaInicio, fechaFin) },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = Guinda4T)
            ) {
                Text("Guardar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Guinda4T)
            }
        }
    )
}
